package cn.lxdl.service.pay;

import cn.lxdl.dao.log.PayLogDao;
import cn.lxdl.pojo.log.PayLog;
import cn.lxdl.utils.http.HttpClient;
import cn.lxdl.utils.uniqueuekey.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {
    @Resource
    private IdWorker idWorker;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private PayLogDao payLogDao;

    // 注入配置文件中微信支付所需参数
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;

    @Override
    public Map<String, String> createNative(String username) throws Exception {
        // 取出redis中创建支付日志
        PayLog paylog = (PayLog) redisTemplate.boundHashOps("PAY_LOG").get(username);

        // 调用微信统一下单接口
        // 1、接口地址
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        HashMap<String, String> map = new HashMap<>();
        // 发送过去的数据格式:即入参,出参格式为xml格式
        // 故封装为map数据,调用微信提高转换工具,转为xml格式

        String out_trade_no = paylog.getOutTradeNo(); // 支付流水id

        // 公众账号ID
        map.put("appid", appid);
        // 商户号
        map.put("mch_id", partner);
        // 随机字符串
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        // 商品描述
        map.put("body", "品优购订单支付");
        // 商户订单号
        map.put("out_trade_no", out_trade_no);
        // 标价金额 单位:分
        map.put("total_fee", "1"); // 测试
        // map.put("total_fee", String.valueOf(paylog.getTotalFee())); // 上线
        // 终端IP
        map.put("spbill_create_ip", "123.12.12.123");
        // 通知地址
        map.put("notify_url", notifyurl);
        // 交易类型
        map.put("trade_type", "NATIVE");

        // 将map转成xml 且内部有生成签名.
        String xmlParam = WXPayUtil.generateSignedXml(map, partnerkey);

        // 模拟浏览器发送请求（HttpClient）
        HttpClient httpClient = new HttpClient(url);
        httpClient.setXmlParam(xmlParam);
        httpClient.isHttps();   // https
        httpClient.post();      // post提交

        // 请求完成后响应结果
        String strXML = httpClient.getContent();                     // xml
        Map<String, String> data = WXPayUtil.xmlToMap(strXML);
        data.put("total_fee", String.valueOf(paylog.getTotalFee())); // 展示金额
        data.put("out_trade_no", out_trade_no);                      // 订单号
        // data.put("code_url", "www.lxdl.cn");
        System.out.println(data.get("code_url"));

        return data;
    }

    @Transactional
    @Override
    public Map<String, String> queryPayStatus(String username, String out_trade_no) throws Exception {
        // 查询订单的接口地址
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        // 将接口需要的数据封装到map中
        Map<String, String> map = new HashMap<>();
        // 公众账号ID
        map.put("appid", appid);
        // 商户号
        map.put("mch_id", partner);
        // 商户订单号
        map.put("out_trade_no", out_trade_no);
        // 随机字符串
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        // 将map的数据转成接口需要的数据类型：xml
        String xmlParam = WXPayUtil.generateSignedXml(map, partnerkey);
        // 通过httpclient发送请求
        HttpClient httpClient = new HttpClient(url);
        httpClient.setXmlParam(xmlParam);
        httpClient.isHttps();   // https
        httpClient.post();      // post提交

        // 响应返回结果：xml--->map
        String strXML = httpClient.getContent();
        Map<String, String> data = WXPayUtil.xmlToMap(strXML);

        // 查看返回数据,查看到交易成功,则修改更新日志
        String tradeState = data.get("trade_state");
        if ("SUCCESS".equals(tradeState)) {   // 支付成功
            // 更新日志
            PayLog payLog = new PayLog();
            payLog.setOutTradeNo(out_trade_no);                    // 主键
            payLog.setPayTime(new Date());                         // 支付完成时间
            payLog.setTransactionId(data.get("transaction_id"));   // 第三方提供的交易号码
            payLog.setTradeState("1");                             // 支付成功
            payLogDao.updateByPrimaryKeySelective(payLog);
        }
        // 删除redis日志
        redisTemplate.boundHashOps("PAY_LOG").delete(username);

        return data;
    }
}
