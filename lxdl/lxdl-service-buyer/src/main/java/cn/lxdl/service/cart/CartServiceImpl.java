package cn.lxdl.service.cart;

import cn.lxdl.dao.item.ItemDao;
import cn.lxdl.dao.seller.SellerDao;
import cn.lxdl.entity.Cart;
import cn.lxdl.pojo.item.Item;
import cn.lxdl.pojo.order.OrderItem;
import cn.lxdl.pojo.seller.Seller;
import cn.lxdl.service.cart.CartService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service //暴露服务到注册中心
public class CartServiceImpl implements CartService {
    @Resource
    private ItemDao itemDao;
    @Resource
    private SellerDao sellerDao;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Item findOne(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }

    @Override
    public List<Cart> autoDataToCartList(List<Cart> cartList) {
        for (Cart cart : cartList) {
            Seller seller = sellerDao.selectByPrimaryKey(cart.getSellerId());
            cart.setSellerName(seller.getNickName());   // 商家店铺名称
            // 填充购物项的数据
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                orderItem.setPicPath(item.getImage());      // 商品图片
                orderItem.setTitle(item.getTitle());        // 商品标题
                orderItem.setPrice(item.getPrice());        // 商品单价
                BigDecimal totalFee = new BigDecimal(item.getPrice().doubleValue() * orderItem.getNum());
                orderItem.setTotalFee(totalFee);            // 商品小计 = 单价 * 数量
            }
        }
        return cartList;
    }

    @Override
    public void mergeCartList(String username, List<Cart> newCartList) {
        // 判断redis中是否含有购物车
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);

        // 将新的购物车信息合并到之前的购物车中
        oldCartList = mergeNewCartListToOldCartList(newCartList, oldCartList);

        // 保存合并后的购物车
        redisTemplate.boundHashOps("BUYER_CART").put(username, oldCartList);
    }

    /**
     * 将新的购物车信息合并到之前的购物车中
     */
    private List<Cart> mergeNewCartListToOldCartList(List<Cart> newCartList, List<Cart> oldCartList) {
        // 判断新购物车
        if (newCartList != null) {
            // 判断之前的购物车
            if (oldCartList != null) {
                // 旧,新购物车都不为null
                for (Cart newCart : newCartList) {
                    // 判断该购物车商家项是否属于同一个商家的
                    int cartIndexOf = oldCartList.indexOf(newCart);
                    if (cartIndexOf != -1) {
                        // 存在,则为同一商家,则比对是否有同一商品.
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                        List<OrderItem> oldOrderItemList = oldCartList.get(cartIndexOf).getOrderItemList();
                        for (OrderItem newOrderItem : newOrderItemList) {
                            int itemIndexOf = oldOrderItemList.indexOf(newOrderItem);
                            if (itemIndexOf != -1) {
                                // 同商家,同商品,处理:购买数量增加
                                OrderItem oldOrderItem = oldOrderItemList.get(itemIndexOf);
                                oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                            } else {
                                // 同商家,不同商品,处理:加入商品项
                                oldOrderItemList.add(newOrderItem);
                            }
                        }
                    } else {
                        // 不存在
                        oldCartList.add(newCart);
                    }
                }
            } else {
                // 没有旧购物车,则新购物车直接加入返回
                return newCartList;
            }
        } else {
            // 没有增加的新购物车数据
            return oldCartList;
        }
        // 返回处理过的购物车对象
        return oldCartList;
    }

    @Override
    public List<Cart> findCartListByRedis(String username) {
        List<Cart> buyer_cart = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        return buyer_cart;
    }

    /**
     * @param ids
     * @return java.util.List<cn.lxdl.entity.Cart>
     * @author 嘉正
     * @Description 用传过来的itemId从redis中取出购物项
     * @Date 5:19 PM 2019/5/29
     **/
    @Override
    public void handleCart(Long[] ids, String username) {
        //取出全部购物车
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        //定义选择了的将要结算的购物车
        List<Cart> newCartList = new ArrayList<>();
        List<OrderItem> newOrderItem = null;
        //定义一个开关
        boolean isSwitch = false;

        for (Cart cart : oldCartList) {
            isSwitch = false;
            newOrderItem = new ArrayList<>();
            for (Long id : ids) {
                List<OrderItem> orderItemList = cart.getOrderItemList();
                for (OrderItem orderItem : orderItemList) {
                    Long itemId = orderItem.getItemId();
                    if (id.equals(itemId)) {
                        isSwitch = true;
                        newOrderItem.add(orderItem);
                    }
                }
            }
            if (isSwitch) {
                Cart newCart = new Cart();
                newCart.setSellerId(cart.getSellerId());
                newCart.setSellerName(cart.getSellerName());
                newCart.setOrderItemList(newOrderItem);
                newCartList.add(newCart);
            }
        }
        //重新设一个redis，将选中的购物项放入这个redis中
        redisTemplate.boundHashOps("CHECKED_CART").put(username, newCartList);

    }

    @Override
    public List<Cart> getCheckedCart(String username) {
        List<Cart> checkedCart = (List<Cart>) redisTemplate.boundHashOps("CHECKED_CART").get(username);
        return checkedCart;
    }
}
