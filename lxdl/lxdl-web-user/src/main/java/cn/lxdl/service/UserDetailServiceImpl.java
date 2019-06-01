package cn.lxdl.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义认证类,需要实现接口UserDetailsService,且重写loadUserByUsername(String username)方法.
 */
public class UserDetailServiceImpl implements UserDetailsService {

    /**
     * 若之前spring-security功能为认证+授权
     * 然后使用CAS作为单点登录认证,则这样只进行授权.
     *
     * @param username 用户账号
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Set<GrantedAuthority> authorities = new HashSet<>();
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        authorities.add(grantedAuthority);
        // 只进行授权,则password则填空字符串"".
        User user = new User(username, "", authorities);
        return user;
    }
}
