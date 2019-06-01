package cn.lxdl.listener;

import cn.lxdl.service.staticPage.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 自定义消息监听器：生成静态页
 */
public class PageListener implements MessageListener {

    @Resource
    private StaticPageService staticPageService;

    @Override
    public void onMessage(Message message) {
        try {
            // 获取消息
            ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
            String goodsId = activeMQTextMessage.getText();
            System.out.println("page消费者获取的商品id：" + goodsId);
            // 消费消息
            staticPageService.getHtml(Long.parseLong(goodsId));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
