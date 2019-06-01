package cn.lxdl.controller.cart;

import cn.lxdl.entity.Cart;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.item.Item;
import cn.lxdl.pojo.order.OrderItem;
import cn.lxdl.service.cart.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车控制层
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;


    /**
     * 将商品加入购物车
     *
     * @param itemId 库存id(sku id)
     * @param num    数量
     * @return
     */
    @CrossOrigin(origins = {"http://localhost:8088"}, allowCredentials = "true") // 允许跨域访问
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(String itemId, Integer num,
                                     HttpServletRequest request, HttpServletResponse response) {
        try {
            // 解决服务器端的跨域请求
            // response.setHeader("Access-Control-Allow-Origin", "http://localhost:8088");
            // response.setHeader("Access-Control-Allow-Credentials", "true"); // 支持携带的cookie信息

            // TODO 将商品加入购物车的具体业务行为 (先做:未登录情况-.-)
            // 1.定义一个空购物车集合 (购物车,可能含有多个商家项)
            List<Cart> cartList = null;

            // 设置开关判断本地cookie是否存在购物车
            boolean flag = false;

            // 2.判断本地(cookie)是否有购物车
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if ("BUYER_CART".equals(cookie.getName())) {
                        // 3.有,则取出并且赋值
                        // cookie:key-value(String)
                        String jsonValue = null; // 数据：List<Cart>----json
                        try {
                            jsonValue = URLDecoder.decode(cookie.getValue(), "utf8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        cartList = JSON.parseArray(jsonValue, Cart.class);

                        flag = true; // 表示cookie存在购物车数据
                        break; // 跳出循环
                    }
                }
            }

            // 无,则说明cookie无存储购物车信息
            if (cartList == null) {
                cartList = new ArrayList<>();
            }

            // 创建购物车后,先封装前端传递过来的数据
            Cart cart = new Cart();
            Item item = cartService.findOne(Long.valueOf(itemId));
            cart.setSellerId(item.getSellerId()); // 封装商家id

            List<OrderItem> orderItemList = new ArrayList<>(); // 封装购物项(主要购物项,为存进Cookie的数据瘦身)
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(Long.valueOf(itemId));
            orderItem.setNum(num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            // 5.将商品(cart)装车
            // 5.1判断商品是否属于用一个商家(用--sellerId--进行判断)
            int sellerIndexOf = cartList.indexOf(cart);
            if (sellerIndexOf != -1) {
                // 同一个商家,则继续判断是否同一个商品
                // 获取当前商家下的购物项
                List<OrderItem> oldOrderItemList = cartList.get(sellerIndexOf).getOrderItemList();
                int orderItemIndexOf = oldOrderItemList.indexOf(orderItem);
                if (orderItemIndexOf != -1) {
                    // 同款商品:合并数量
                    OrderItem oldOrderItem = oldOrderItemList.get(orderItemIndexOf);
                    oldOrderItem.setNum(oldOrderItem.getNum() + num);
                } else {
                    // 同商家不同商品：添加到该商家的购物项集中
                    oldOrderItemList.add(orderItem);
                }
            } else {
                // 5.2、不是同一个商家：直接装车
                cartList.add(cart);
            }

            // 判断用户是否登录
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("name = " + username);
            if (!"anonymousUser".equals(username)) {
                // 已登录
                // 将本地的cookie的购物车的数据同步到redis
                cartService.mergeCartList(username, cartList);
                // 清空本地的购物车
                if (flag) {
                    Cookie buyer_cart = new Cookie("BUYER_CART", null);
                    buyer_cart.setMaxAge(0);
                    buyer_cart.setPath("/");  // 设置cookie共享
                    response.addCookie(buyer_cart); // 加入相应返回前端
                }

            } else {
                // 未登录
                // PS：大家注意下，购物车那块如果是tomcat8有坑。字符编码格式的问题。需要手动去处理,处理cookie中非法字符.
                // URLDecoder.decode(value, charset);
                // URLEncoder.encode(cookieValue, encodeString);

                // 6.将购物车保存在本地(cookie)
                Cookie buyer_cart = new Cookie("BUYER_CART", URLEncoder.encode(JSON.toJSONString(cartList), "utf8"));
                buyer_cart.setMaxAge(3600);
                buyer_cart.setPath("/");  // 设置cookie共享
                response.addCookie(buyer_cart); // 加入相应返回前端
            }

            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

    /**
     * 查询购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response) {
        // TODO 判断用户是否登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 1、未登录：从本地（cookie）中获取
        List<Cart> cartList = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if ("BUYER_CART".equals(cookie.getName())) {
                    String jsonValue = null; // 数据：List<Cart>----json
                    try {
                        jsonValue = URLDecoder.decode(cookie.getValue(), "utf8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return cartList;
                    }
                    cartList = JSON.parseArray(jsonValue, Cart.class);
                    break; // 跳出循环
                }
            }
        }

        // 2.已登录
        if (!"anonymousUser".equals(username)) {

            if (cartList != null) {
                // 将本地的cookie的购物车的数据同步到redis
                cartService.mergeCartList(username, cartList);
                // 清空本地cookie
                Cookie buyer_cart = new Cookie("BUYER_CART", null);
                buyer_cart.setMaxAge(0);
                buyer_cart.setPath("/");  // 设置cookie共享
                response.addCookie(buyer_cart); // 加入相应返回前端
            }
            // 从redis中取出购物车
            cartList = cartService.findCartListByRedis(username);
        }

        // 填充购物车的其他次要列表数据
        if (cartList != null) {
            // 填充购物车列表页面需要展示的数据
            cartList = cartService.autoDataToCartList(cartList);
        }

        return cartList;
    }
    /**
     * @author 嘉正
     * @Description 点击结算判断是否登录
     * @Date 11:43 AM 2019/5/29
     * @param
     * @return
     **/
    @RequestMapping("/judgingLogin")
    public Result judgingLogin(){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(username)) {
            return new Result(true,"已登录");
        }
        return new Result(false,"未登录，请先等再结算");
    }
    /**
     * @author 嘉正
     * @Description 处理选中的购物项
     * @Date 4:55 PM 2019/5/29
     * @param
     * @return void
     **/
    @RequestMapping("/handleCart")
    public void handleCart(Long[] ids){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        //ids不为空的话，利用itemId从缓存中取出购物车,然后放入redis的CHECKED_CART
        if (ids!=null) {
            cartService.handleCart(ids, username);
        }
    }
    /**
     * @author 嘉正
     * @Description 结算页面取出放在redis中的选中的购物项
     * @Date 10:45 PM 2019/5/29
     * @param
     * @return java.util.List<cn.lxdl.entity.Cart>
     **/
    @RequestMapping("/getCheckedCart")
    public List<Cart> getCheckedCart(){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        //用户名取出选中的购物项
        List<Cart> checkedCart = cartService.getCheckedCart(username);
        // 填充购物车的其他次要列表数据
        if (checkedCart != null) {
            // 填充购物车列表页面需要展示的数据
            checkedCart = cartService.autoDataToCartList(checkedCart);
        }
        return checkedCart;
    }
}
