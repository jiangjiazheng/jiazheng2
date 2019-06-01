package cn.lxdl.controller.order;

import cn.lxdl.service.order.OrderService;
import cn.lxdl.vo.OrderVO;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Reference
    private OrderService orderService;

    /**
     * 查询用户订单
     */
    @RequestMapping("/findOrderList")
    public List<OrderVO> findOrderList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(orderService);
        List<OrderVO> orderVOList = orderService.listUserOrder(username);
        return orderVOList;
    }
}
