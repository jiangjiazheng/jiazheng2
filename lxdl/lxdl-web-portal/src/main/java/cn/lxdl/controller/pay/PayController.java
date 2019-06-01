package cn.lxdl.controller.pay;

import cn.lxdl.entity.Result;
import cn.lxdl.service.pay.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付控制类
 */
@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private PayService payService;

    /**
     * 生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map<String, String> createNative() throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return payService.createNative(username);
    }

    /**
     * 查询微信支付
     *
     * @param out_trade_no 支付id
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        try {
            // 计时器
            int i = 0;
            // 登录用户名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            while (true) {   // 不能频繁刷,休息一会儿
                Map<String, String> map = payService.queryPayStatus(username, out_trade_no);
                String tradeState = map.get("trade_state");
                if ("SUCCESS".equals(tradeState)) { // 支付成功
                    return new Result(true, "支付成功");
                } else {
                    // 等待支付,支付中,休息5秒
                    Thread.sleep(5000);
                    i++;
                }
                if (i > 360) {
                    return new Result(false, "二维码超时");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "支付失败");
        }

    }
}
