package cn.lxdl.controller.address;

import cn.lxdl.pojo.address.Address;
import cn.lxdl.service.address.AddressService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Reference
    private AddressService addressService;

    @RequestMapping("/findListByLoginUser")
    public List<Address> findListByLoginUser() {
        // 获取登录用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.getListByLoginUser(username);
    }
}
