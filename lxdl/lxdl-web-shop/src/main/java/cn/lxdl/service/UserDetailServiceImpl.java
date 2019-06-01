package cn.lxdl.service;

import cn.lxdl.pojo.seller.Seller;
import cn.lxdl.service.seller.SellerService;
import cn.lxdl.service.seller.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义认证类
 */
public class UserDetailServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //商家id为输入的账户名,故可以使用id来查询
        Seller seller = sellerService.getById(username);

        //认证
        if (seller != null) {
            //授权
            Set<GrantedAuthority> authorities = new HashSet<>();
            SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_SELLER");
            authorities.add(grantedAuthority);
            //这里返回的user中,里面的密码会解密后跟用户输入的进行比对.
            User user = new User(seller.getSellerId(),seller.getPassword(),authorities);
            return user;
        }
        return null;
    }
}
