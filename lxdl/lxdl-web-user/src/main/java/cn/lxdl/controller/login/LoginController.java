package cn.lxdl.controller.login;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 显示当前登录人
     */
    @RequestMapping("/showName")
    public Map<String, String> showName() {
        HashMap<String, String> map = new HashMap<>();
        // 从安全框架spring-security中获取登录的用户名.
        map.put("loginName", SecurityContextHolder.getContext().getAuthentication().getName());
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
        return map;
    }
}
