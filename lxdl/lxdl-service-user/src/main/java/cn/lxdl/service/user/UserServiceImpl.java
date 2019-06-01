package cn.lxdl.service.user;

import cn.lxdl.dao.user.UserDao;
import cn.lxdl.pojo.user.User;
import cn.lxdl.pojo.user.UserQuery;
import cn.lxdl.utils.md5.MD5Util;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private JmsTemplate jmsTemplate;
    @Resource
    private ActiveMQQueue smsDestination;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserDao userDao;

    @Override
    public void sendCode(String phone) {
        // 生成6位验证码
        String code = RandomStringUtils.randomNumeric(6);
        System.out.println("code" + code);

        // 使用redis去模拟session存储验证码,使用 key:phone --> value:code方式存储验证码
        redisTemplate.boundValueOps(phone).set(code);
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);// 并且设置redis存储数据的有效时间

        // 发送数据到消息队列(mq)
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                //map消息体
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("phoneNumbers", phone);
                mapMessage.setString("signName", "阮文");
                mapMessage.setString("templateCode", "SMS_140720901");
                mapMessage.setString("templateParam", "{\"code\":\"" + code + "\"}");
                return mapMessage;
            }
        });
    }

    @Transactional
    @Override
    public void save(User user, String code) {
        // 需要校验用户输入的验证码与发送的验证码是否一致
        String smsCode = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        if (!StringUtils.isEmpty(code) && !StringUtils.isEmpty(smsCode) && smsCode.equals(code)) {

            // 用户密码需要加密
            String encode = MD5Util.MD5Encode(user.getPassword(), "utf-8");
            user.setPassword(encode);

            user.setCreated(new Date());
            user.setUpdated(new Date());

            // 用户保存
            userDao.insertSelective(user);
        } else {
            // 抛出运行时异常传递controller,处理成友好信息返回给用户观看
            throw new RuntimeException("您输入的验证码不正确");
        }
    }

    @Override
    public User getUserInfo(String username) {
        UserQuery query = new UserQuery();
        query.createCriteria().andUsernameEqualTo(username);
        List<User> userList = userDao.selectByExample(query);
        if (userList == null || userList.size() != 1) {
            throw new RuntimeException("用户信息异常");
        }
        return userList.get(0);
    }

    @Transactional
    @Override
    public void saveUserInfo(User user) {
        StringBuilder builder = new StringBuilder();
        if (user.getYear() != null) {
            builder.append(user.getYear()).append("-");
        }
        if (user.getMonth() != null) {
            builder.append(user.getMonth()).append("-");
        }
        if (user.getDay() != null) {
            builder.append(user.getDay());
        }
        String string = builder.toString();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println(parse);

        userDao.updateByPrimaryKeySelective(user);
    }

    @Transactional
    @Override
    public void updateNickNameAndPassword(String nickName, String newPassword, String username) {
        if (username == null) {
            throw new RuntimeException("用户登录状态异常");
        }
        if (newPassword == null || "".equals(newPassword)) {
            throw new RuntimeException("填入密码异常");
        }

        UserQuery query = new UserQuery();
        query.createCriteria().andUsernameEqualTo(username);
        List<User> userList = userDao.selectByExample(query);
        if (userList != null && userList.size() == 1) {
            User user = userList.get(0);
            if (nickName != null) {
                user.setNickName(nickName);
            }

            // 用户密码需要加密
            String encode = MD5Util.MD5Encode(newPassword, "utf-8");
            user.setPassword(encode);

            // 进行更新
            userDao.updateByPrimaryKeySelective(user);
        } else {
            throw new RuntimeException("系统繁忙");
        }
    }

    @Override
    public Boolean oldPhoneMsgcodeVerification(String msgCode, String username) {
        String phone = null;
        UserQuery query = new UserQuery();
        query.createCriteria().andUsernameEqualTo(username);
        List<User> userList = userDao.selectByExample(query);
        if (userList != null && userList.size() == 1) {
            User user = userList.get(0);
            // 从数据库中获取到用户的原电话号码
            phone = user.getPhone();
        }

        // 需要校验用户输入的验证码与发送的验证码是否一致
        String smsCode = (String) redisTemplate.boundValueOps(phone).get();
        if (!StringUtils.isEmpty(msgCode) && !StringUtils.isEmpty(smsCode) && smsCode.equals(msgCode)) {
            // 旧手机号码验证成功后,增加标志
            redisTemplate.boundValueOps("oldCodeVerFlag").set("12580");
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public Boolean newPhoneMsgcodeVerification(String msgCode, String newPhone, String username) {

        UserQuery query = new UserQuery();
        query.createCriteria().andUsernameEqualTo(username);
        List<User> userList = userDao.selectByExample(query);
        User user = null;
        if (userList != null && userList.size() == 1) {
            user = userList.get(0);
        }
        if (user == null) {
            throw new RuntimeException("服务器忙...");
        }
        if (user.getPhone().equals(newPhone)) {
            throw new RuntimeException("不能与原手机号相同");
        }
        String oldCodeVerFlag = (String) redisTemplate.boundValueOps("oldCodeVerFlag").get();
        if (!"12580".equals(oldCodeVerFlag)) {
            throw new RuntimeException("异常跳转状态");
        }

        String smsCode = (String) redisTemplate.boundValueOps(newPhone).get();
        if (!StringUtils.isEmpty(msgCode) && !StringUtils.isEmpty(smsCode) && smsCode.equals(msgCode)) {
            // 验证成功
            // 1.绑定新手机号码
            user.setPhone(newPhone);
            userDao.updateByPrimaryKeySelective(user);
            // 2.删除标志
            redisTemplate.delete("oldCodeVerFlag");
            return true;
        }

        return false;
    }

    @Override
    public void checkOldPhoneVer() {
        String oldCodeVerFlag = (String) redisTemplate.boundValueOps("oldCodeVerFlag").get();
        if (!"12580".equals(oldCodeVerFlag)) {
            throw new RuntimeException("异常跳转状态");
        }
    }
}
