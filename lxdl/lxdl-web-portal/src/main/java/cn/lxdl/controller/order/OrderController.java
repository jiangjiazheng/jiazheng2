package cn.lxdl.controller.order;

import cn.lxdl.entity.Result;
import cn.lxdl.pojo.address.Address;
import cn.lxdl.pojo.order.Order;
import cn.lxdl.service.order.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Reference
    private OrderService orderService;

    /**
     * 提交订单
     * @param order
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Order order) {
        try {
            // 获取到当前登录用户账号
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            orderService.save(username, order);
            return new Result(true, "订单提交成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "订单提交失败");
    }

    /**
     * 新增/保存 收货地址
     * @param address
     * @return
     */
    @RequestMapping("/saveAddress.do")
    public Result saveAddress(@RequestBody Address address) {

        try {
            String contact = address.getContact();
            String addr = address.getAddress();
            String mobile = address.getMobile();
            if (contact == null || "".equals(contact)) {
                return new Result(false, "收货人不能为空！");
            }
            if (addr == null || "".equals(addr)) {
                return new Result(false, "详细地址不能为空！");
            }
            if (mobile == null || "".equals(mobile)){
                return new Result(false, "联系电话不能为空！");
            }

            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            address.setUserId(userId);
            orderService.saveAddress(address);
            return new Result(true, "新增成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "新增失败！");
    }
}
