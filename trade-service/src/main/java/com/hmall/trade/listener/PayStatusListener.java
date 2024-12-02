package com.hmall.trade.listener;

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
 * Create : 2024-11-26 14:55
 **/
@RequiredArgsConstructor
@Component
public class PayStatusListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue", durable = "true"),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId){
        //查询订单状态（幂等性）
        Order order = orderService.getById(orderId);
        if (order.getStatus() == null || order.getStatus() != 1) {
            //不处理
            return;
        }
        //
        orderService.markOrderPaySuccess(orderId);
    }
}
