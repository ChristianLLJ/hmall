package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Program : hmall
 * Author : llj
 * Create : 2024-11-26 20:27
 **/

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
                    value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
                    exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME, delayed = "true"),
                    key = MQConstants.DELAY_ORDER_KEY
            ))
    public void listenDelayOrderMessage(Long orderId){
        //查询订单
        Order order = orderService.getById(orderId);
        if(order == null || order.getStatus() != 1){
            return;
        }

        PayOrderDTO payOrder = payClient.queryPayOrderByBizOrderNo(orderId);

        if (payOrder != null && payOrder.getStatus() == 3) {
            orderService.markOrderPaySuccess(orderId);
        }else {
            orderService.cancelOrder(orderId);
        }

    }
}
