package cn.lxdl.service.order;

import cn.lxdl.dao.address.AddressDao;
import cn.lxdl.dao.item.ItemDao;
import cn.lxdl.dao.log.PayLogDao;
import cn.lxdl.dao.order.OrderDao;
import cn.lxdl.dao.order.OrderItemDao;
import cn.lxdl.dao.seller.SellerDao;
import cn.lxdl.entity.Cart;
import cn.lxdl.pojo.address.Address;
import cn.lxdl.pojo.address.AddressQuery;
import cn.lxdl.entity.OrderDTO;
import cn.lxdl.pojo.item.Item;
import cn.lxdl.pojo.log.PayLog;
import cn.lxdl.pojo.order.Order;
import cn.lxdl.pojo.order.OrderItem;
import cn.lxdl.pojo.order.OrderItemQuery;
import cn.lxdl.pojo.order.OrderQuery;
import cn.lxdl.pojo.seller.Seller;
import cn.lxdl.utils.uniqueuekey.IdWorker;
import cn.lxdl.vo.OrderVO;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderDao orderDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private IdWorker idWorker;
    @Resource
    private ItemDao itemDao;
    @Resource
    private OrderItemDao orderItemDao;
    @Resource
    private PayLogDao payLogDao;
    @Resource
    private AddressDao addressDao;
    @Resource
    private SellerDao sellerDao;


    @Transactional // 加上事务控制
    @Override
    public void save(String username, Order order) {
        // 1.保存订单,以商家为单位,则购物车有几个商家-->则有几个订单记录在tb_order表中

        // 获取redis中的购物车
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);

        if (cartList != null && cartList.size() > 0) {
            // 整个订单的总金额
            double fee = 0f;
            // 订单号的列表
            List<Long> orderList = new ArrayList<>();

            // 遍历购物车,对每个商家项进行处理
            for (Cart cart : cartList) {
                long orderId = idWorker.nextId(); // 生成订单id
                orderList.add(orderId);           // 支付日志表需要订单列表
                order.setOrderId(orderId);

                double payment = 0d;              // 该商家下的订单总金额 = 订单明细的总价
                order.setPaymentType("1");        // 支付类型: 1 在线支付
                order.setStatus("1");             // 订单状态: 1 待付款
                order.setCreateTime(new Date());  // 订单创建时间
                order.setUserId(username);        // 下单用户
                order.setSourceType("1");         // 订单来源: 1 PC端
                order.setSellerId(cart.getSellerId()); // 商家id

                // 2.保存订单明细 tb_order_item
                List<OrderItem> orderItemList = cart.getOrderItemList();
                if (orderItemList != null && orderItemList.size() > 0) {
                    // 遍历商家项,对每个商品项进行处理
                    for (OrderItem orderItem : orderItemList) {

                        orderItem.setId(idWorker.nextId());     // 订单商品信息id
                        // 获取到库存对象
                        Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());

                        orderItem.setOrderId(orderId);          // 订单id
                        orderItem.setTitle(item.getTitle());    // 商品标题
                        orderItem.setPrice(item.getPrice());    // 商品单价
                        orderItem.setPicPath(item.getImage());  // 商品图片路径

                        double totalFee = item.getPrice().doubleValue() * orderItem.getNum(); // 商品项总价格 = 单价 * 数量
                        payment += totalFee;

                        orderItem.setTotalFee(new BigDecimal(totalFee)); // 订单价格

                        orderItem.setSellerId(item.getSeller()); //店铺名称
                        // 保存订单明细
                        orderItemDao.insertSelective(orderItem);
                    }
                }
                // 支付金额=各个商家项订单总金额相加
                fee += payment;

                order.setPayment(new BigDecimal(payment)); // 商家的订单总金额
                // 保存订单
                orderDao.insertSelective(order);
            }

            // 创建支付日志
            PayLog payLog = new PayLog();
            payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));    // 交易日志（流水号）
            payLog.setCreateTime(new Date());                           // 日志创建日期
            payLog.setTotalFee((long) fee * 100);                       // 订单总金额：单位:分
            payLog.setUserId(username);                                 // 当前登录用户名
            payLog.setTradeState("0");                                  // 订单支付状态：0:待支付
            payLog.setOrderList(orderList.toString().replace("[", "").replace("]", ""));     // 订单列表 [32132398989,984923277]
            payLog.setPayType("1");                                     // 在线支付 1:在线支付
            payLogDao.insertSelective(payLog);
            // 将交易日志放到redis中---支付过程中需要改数据
            redisTemplate.boundHashOps("PAY_LOG").put(username, payLog);
        }
        //删除选中的购物项，留下没选择的购物项在购物车
        deleteCheckedRedis(username);
        // 3.删除购物车
        //redisTemplate.boundHashOps("BUYER_CART").delete(username);

    }
    /**
     * @author 嘉正
     * @Description 删除选中的购物项，留下没选择的购物项在购物车
     * @Date 11:34 AM 2019/5/30
     * @param username
     * @return void
     **/
    private void deleteCheckedRedis(String username) {
        //取出全部购物项
        List<Cart> allCartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        //取出选中的购物项
        List<Cart> checkedCartList = (List<Cart>) redisTemplate.boundHashOps("CHECKED_CART").get(username);
        //循环遍历去除选中的购物项
        if (allCartList != null && allCartList.size() > 0) {
            if (checkedCartList != null && checkedCartList.size() > 0) {
                for (int i=0; i<allCartList.size();i++) {
                    List<OrderItem> allOrderItemList = allCartList.get(i).getOrderItemList();
                    for (Cart checkedCart : checkedCartList) {
                        List<OrderItem> checkedOrderItemList = checkedCart.getOrderItemList();
                        for (OrderItem checkedOrderItem : checkedOrderItemList) {
                            int indexOf = allOrderItemList.indexOf(checkedOrderItem);
                            if (indexOf != -1) {
                                //如果有就删除
                                allOrderItemList.remove(i);
                                if (allOrderItemList==null||allOrderItemList.size()==0){
                                    //如果为空就把商家的购物车删除
                                    allCartList.remove(i);
                                }
                            }

                        }
                    }

                }

                redisTemplate.boundHashOps("BUYER_CART").put(username,allCartList);
            }
        }
        redisTemplate.boundHashOps("CHECKED_CART").delete(username);
    }



    /**
     * 新增收货地址
     * @param address
     */
    @Override
    public void saveAddress(Address address) {
        addressDao.insertSelective(address);
    }
    /**
     * 查询用户订单
     */
    @Override
    public List<OrderVO> listUserOrder(String username) {
        // 订单集合
        List<OrderVO> orderVOList = new ArrayList<>();
        OrderQuery query = new OrderQuery();
        query.createCriteria().andUserIdEqualTo(username);
        List<Order> orderList = orderDao.selectByExample(query);
        if (orderList != null && orderList.size() > 0){
            for (Order order : orderList) {
                // 封装订单
                OrderVO orderVO = new OrderVO();
                orderVO.setCreateTime(order.getCreateTime());
                orderVO.setOrderId(order.getOrderId().toString());
                Seller seller = sellerDao.selectByPrimaryKey(order.getSellerId());
                orderVO.setNickName(seller.getNickName());

                OrderItemQuery itemQuery = new OrderItemQuery();
                itemQuery.createCriteria().andOrderIdEqualTo(order.getOrderId());
                List<OrderItem> orderItemList = orderItemDao.selectByExample(itemQuery);
                // 订单项集合
                List<OrderDTO> orderDTOList = new ArrayList<>();
                // 订单总价
                double totalMoney = 0f;
                // 订单运费
                double postFee = 0f;
                // 商品总价git checkout name
                double fee = 0f;
                for (OrderItem orderItem : orderItemList) {
                    // 封装订单项
                    OrderDTO orderDTO = new OrderDTO();
                    orderDTO.setTitle(orderItem.getTitle());
                    // 封装规格,"{"机身内存":"16G","网络":"联通3G"}"
                    Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                    String spec = item.getSpec();
                    // 处理规格字符串
                    String[] split = spec.split(":");
                    String result = "";
                    for (String s : split) {
                        if (s.contains(",")){
                            String[] split1 = s.split(",");
                            for (int i = 0; i < split1.length; i++) {
                                if (i % 2 == 0){
                                    result += split1[i];
                                }
                            }
                        }
                    }
                    result += split[split.length-1];
                    String[] re = result.split("\"");

                    result = "";
                    for (String s : re) {
                        if (!s.equals("") && !s.equals("}")){
                            result += s+",";
                        }
                    }
                    result = result.replace(",", " ");
                    orderDTO.setSpec(result);
                    BigDecimal price = orderItem.getPrice();
                    orderDTO.setPrice(price);
                    orderDTO.setNum(orderItem.getNum());
                    postFee = Double.parseDouble(order.getPostFee());
                    fee = price.doubleValue()*orderItem.getNum();
                    orderDTO.setStatus(order.getStatus());
                    orderDTO.setPicPath(orderItem.getPicPath());
                    orderDTOList.add(orderDTO);
                    totalMoney += fee;
                }
                orderVO.setPostFee(postFee);
                orderVO.setOrderDTOList(orderDTOList);
                orderVO.setTotalMoney(totalMoney+postFee);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }
}
