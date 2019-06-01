package cn.lxdl.listener;

import cn.lxdl.service.search.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 消费者,mq监听(订阅)
 * 自定义消息监听器：商品上架,商品加入到索引库中
 * 将商品加入到索引库的业务抽离到接口中,注入接口,调用方法.
 */
public class ItemSearchListener implements MessageListener {

    @Resource
    private ItemSearchService itemSearchService;

    /**
     * 获取消费并且进行消费
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        try {
            // 获取消息
            ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
            String goodsId = activeMQTextMessage.getText();
            System.out.println("search消费者获取的商品id：" + goodsId);
            // 消费消息
            itemSearchService.saveItemToSolr(Long.parseLong(goodsId));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
