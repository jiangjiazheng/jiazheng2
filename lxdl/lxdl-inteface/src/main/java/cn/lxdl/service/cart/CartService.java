package cn.lxdl.service.cart;

import cn.lxdl.entity.Cart;
import cn.lxdl.pojo.item.Item;

import java.util.List;

/**
 * 购物车业务接口
 */
public interface CartService {
    /**
     * 获取商家id
     */
    Item findOne(Long itemId);

    /**
     * 填充购物车列表页面需要展示的数据
     */
    List<Cart> autoDataToCartList(List<Cart> cartList);

    /**
     * 登录状态下:将购物车的数据保存到服务端的redis
     *
     * @param username    用户名
     * @param newCartList 未登录状态下cookie或者新增商品到购物车
     */
    void mergeCartList(String username, List<Cart> newCartList);

    /**
     * 从redis中取出购物车
     *
     * @param username
     * @return
     */
    List<Cart> findCartListByRedis(String username);

    void handleCart(Long[] ids,String username);

    List<Cart> getCheckedCart(String username);
}
