package cn.lxdl.controller.seller;

import cn.lxdl.entity.Result;
import cn.lxdl.pojo.seller.Seller;
import cn.lxdl.service.seller.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商家管理系统
 */
@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;


    @RequestMapping("/add")
    public Result add(@RequestBody Seller seller){
        try {
            sellerService.save(seller);
            return new Result(true,"注册成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false,"注册失败");
    }

    @RequestMapping("/findOne")
    public Seller getSellerData(){
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        Seller seller = sellerService.getSeller(sellerId);
        return seller;
    }

    @RequestMapping("/updateSeller")
    public Result updateSeller(@RequestBody Seller seller){
        try {
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            seller.setSellerId(sellerId);
            sellerService.updateSeller(seller);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false,"修改失败");
    }

    @RequestMapping("/modifyPassword")
    public Result modifyPassword(@RequestBody Map<String, String> passwordMap) {

        System.out.println(passwordMap);
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            sellerService.modifyPassword(username, passwordMap);
            return new Result(true, "修改正确！");
        } catch (RuntimeException e){
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "修改失败！");
    }

}
