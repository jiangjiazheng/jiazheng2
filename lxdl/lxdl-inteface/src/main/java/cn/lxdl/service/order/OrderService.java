package cn.lxdl.service.order;

import cn.lxdl.pojo.address.Address;
import cn.lxdl.pojo.order.Order;
import cn.lxdl.vo.OrderVO;

import java.util.List;

/**
 * 订单业务接口
 */
public interface OrderService {

    /**
     * 提交订单
     */
    void save(String username,Order order);

    /**
     * 查询用户订单
     */
    List<OrderVO> listUserOrder(String username);

    /**
     * 新增收货地址
     * @param address
     */
    void saveAddress(Address address);
}
