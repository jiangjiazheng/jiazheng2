package cn.lxdl.controller.user;

import cn.lxdl.entity.Result;
import cn.lxdl.pojo.user.User;
import cn.lxdl.service.user.UserService;
import cn.lxdl.utils.checkPhone.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    /**
     * 发送短信验证码
     *
     * @param phone 手机号码
     * @return
     */
    @RequestMapping("/sendCode")
    public Result sendCode(String phone) {
        try {
            // 对手机号码进行校验
            boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(phone);
            if (!phoneLegal) {
                return new Result(false, "手机号不合法");
            }
            userService.sendCode(phone);
            return new Result(true, "发送成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "发送失败");
    }

    /**
     * 用户注册
     */
    @RequestMapping("/add")
    public Result add(@RequestBody User user, String smscode) {
        try {
            userService.save(user, smscode);
            return new Result(true, "注册成功");
        } catch (RuntimeException e) {
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "注册失败");
        }

    }

    /**
     * 查询用户个人信息
     *
     * @Author 练志达
     */
    @RequestMapping("/findUserInfo")
    public User findUserInfo() {
        // 获取登录用户用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserInfo(username);
    }

    /**
     * 保存用户个人信息
     *
     * @Author 练志达
     */
    @RequestMapping("/saveUserInfo")
    public Result saveUserInfo(@RequestBody User user) {
        System.out.println(user);
        try {
            userService.saveUserInfo(user);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();

        }
        return new Result(true, "保存失败");
    }


}
