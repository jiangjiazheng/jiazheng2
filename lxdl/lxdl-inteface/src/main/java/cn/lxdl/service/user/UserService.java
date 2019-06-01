package cn.lxdl.service.user;

import cn.lxdl.pojo.user.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户业务接口
 */
public interface UserService {

    /**
     * 发送短信验证码
     */
    void sendCode(String phone);

    /**
     * 用户注册
     */
    void save(User user, String code);

    /**
     * 查找用户个人信息
     *
     * @param username 登录用户名
     * @return User
     * @Author 练志达
     */
    User getUserInfo(String username);

    /**
     * 保存用户个人信息
     *
     * @param user 用户个人信息
     * @Author 练志达
     */
    void saveUserInfo(User user);

    /**
     * 用户更新昵称和密码
     * @param nickName 昵称
     * @param newPassword 新密码
     */
    void updateNickNameAndPassword(String nickName, String newPassword, String username);

    /**
     * 校验用户输入的短信验证码(旧手机号)
     * @param msgCode 短信验证码
     * @param username 登录用户用户名
     */
    Boolean oldPhoneMsgcodeVerification(String msgCode, String username);

    /**
     * 校验用户输入的短信验证码(新手机号)
     * @param msgCode 短信验证码
     * @param newPhone 手机号码
     * @param username 登录用户用户名
     */
    Boolean newPhoneMsgcodeVerification(String msgCode, String newPhone,String username);

    /**
     * 跳转至新手机绑定页面进行标志判断是否有无
     */
    void checkOldPhoneVer();
}
