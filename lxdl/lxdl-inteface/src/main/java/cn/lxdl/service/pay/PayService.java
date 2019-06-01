package cn.lxdl.service.pay;

import java.util.Map;

/**
 * 支付功能接口
 */
public interface PayService {
    /**
     * 生成二维码
     */
    Map<String, String> createNative(String username) throws Exception;

    /**
     * 查询微信支付
     *
     * @param out_trade_no 支付id
     */
    Map<String, String> queryPayStatus(String username,String out_trade_no) throws Exception;
}
