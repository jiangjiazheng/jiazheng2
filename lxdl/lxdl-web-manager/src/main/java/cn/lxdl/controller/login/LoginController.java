package cn.lxdl.controller.login;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 显示当前的登录人
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/showName")
    public Map<String, String> showName(){
        Map<String, String> map = new HashMap<>();
        // 登录人：在springsecurity容器中,绑定在当前线程了,
        // ps:注意安全框架是应用在系统中,没有应用在服务中,
        // 故从安全框架的上下文中拿取用户名(登录账号)需要在系统中进行,不能再服务进行.
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username", username);
        return map;
    }
}
