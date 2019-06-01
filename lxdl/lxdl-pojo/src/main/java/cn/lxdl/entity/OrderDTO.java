package cn.lxdl.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author shuanhua
 * @Description 封装订单项的类
 * @Date 2019/5/29 14:54
 * @param  * @param null
 * @return
 **/
public class OrderDTO implements Serializable {

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品规格
     */
    //private Map<String,String> spec;
    private String spec;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 商品购买数量
     */
    private Integer num;

    /**
     * 实付款
     */
    private double payMoney;

    /**
     * 状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
     */
    private String status;

    /**
     * 商品图片地址
     */
    private String picPath;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public double getPayMoney() {
        return payMoney;
    }

    public void setPayMoney(double payMoney) {
        this.payMoney = payMoney;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }
}
