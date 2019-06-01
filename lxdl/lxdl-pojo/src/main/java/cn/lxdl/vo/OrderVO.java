package cn.lxdl.vo;

import cn.lxdl.entity.OrderDTO;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @author shuanhua
 * @Description 封装用户订单
 * @Date 2019/5/29 14:59
 * @param  * @param null
 * @return
 **/
public class OrderVO implements Serializable {
    /**
     * 订单创建时间
     */
    private Date createTime;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 店铺名称
     */
    private String nickName;

    /**
     * 订单列表
     */
    private List<OrderDTO> orderDTOList;

    /**
     * 订单总价
     */
    private double totalMoney;

    /**
     * 运费
     */
    private double postFee;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public List<OrderDTO> getOrderDTOList() {
        return orderDTOList;
    }

    public void setOrderDTOList(List<OrderDTO> orderDTOList) {
        this.orderDTOList = orderDTOList;
    }

    public double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }

    public double getPostFee() {
        return postFee;
    }

    public void setPostFee(double postFee) {
        this.postFee = postFee;
    }

    @Override
    public String toString() {
        return "OrderVO{" +
                "createTime=" + createTime +
                ", orderId=" + orderId +
                ", nickName='" + nickName + '\'' +
                ", orderDTOList=" + orderDTOList +
                ", totalMoney=" + totalMoney +
                ", postFee=" + postFee +
                '}';
    }
}
