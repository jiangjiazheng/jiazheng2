package cn.lxdl.controller.safe;

import cn.lxdl.entity.Result;
import cn.lxdl.service.user.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息安全控制器
 */
@RestController
@RequestMapping("/safe")
public class SafeController {

    @Reference
    private UserService userService;

    /**
     * 用户更新昵称和密码
     */
    @RequestMapping("/updateNickNameAndPassword")
    public Result updateNickNameAndPassword(String nickName, String newPassword, String confirmPassword) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                return new Result(false, "密码不一致");
            }
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            userService.updateNickNameAndPassword(nickName, newPassword, username);
            return new Result(true, "更改成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "更改失败");
    }

    /**
     * 旧手机号验证码校验
     *
     * @param msgCode 短信验证码
     * @param phone   电话号码
     * @return
     */
    @RequestMapping("/oldPhoneMsgcodeVerification")
    private Result oldPhoneMsgcodeVerification(String msgCode, String phone) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Boolean verification = userService.oldPhoneMsgcodeVerification(msgCode, username);
            if (verification) {
                return new Result(true, "校验成功");
            }
            return new Result(false, "校验失败");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "检验异常");
    }

    /**
     * 新电话号码验证码验证及绑定
     *
     * @param msgCode
     * @param phone
     * @return
     */
    @RequestMapping("/newPhoneMsgcodeVerification")
    private Result newPhoneMsgcodeVerification(String msgCode, String phone) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Boolean verification = userService.newPhoneMsgcodeVerification(msgCode, phone, username);
            if (verification) {
                return new Result(true, "校验成功");
            }
            return new Result(false, "校验失败");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "检验异常");
    }

    /**
     * 跳转至新手机绑定页面进行标志判断是否有无
     *
     * @return
     */
    @RequestMapping("/checkOldPhoneVer")
    public Result checkOldPhoneVer() {
        try {
            userService.checkOldPhoneVer();
            return new Result(true, "还行大佬");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "校验失败,非法跳转");
        }
    }


}
