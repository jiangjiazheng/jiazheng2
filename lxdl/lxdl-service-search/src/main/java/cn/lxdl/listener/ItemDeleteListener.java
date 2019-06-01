package cn.lxdl.listener;

import cn.lxdl.service.search.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemDeleteListener implements MessageListener {
    @Resource
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            // 获取消息
            ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
            String goodsId = activeMQTextMessage.getText();
            System.out.println("获的商品下架id:" + goodsId);
            // 消费消息
            itemSearchService.removeItemToSolr(Long.valueOf(goodsId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
