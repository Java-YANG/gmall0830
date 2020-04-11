package com.atguigu.gmall.order.listeners;

import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;

@Component
public class OrderMqListener {

    @Autowired
    OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAY_SUCCESS_QUEUE")
    public void orderConsumer(MapMessage mapMessage) throws JMSException {
        // 监听代码
        String out_trade_no = mapMessage.getString("out_trade_no");
        String status = mapMessage.getString("status");
        System.out.println(out_trade_no + "号订单支付状态：" + status + "========================================================" );

        // 更新订单状态
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(out_trade_no);
        if(status.equals("用户未支付")){
            status = "5";
        }
        if (status.equals("已支付")) {
            status = "1";
        }
        omsOrder.setStatus(status);
        omsOrder.setPaymentTime(new Date());
        orderService.updateOrder(omsOrder);

    }
}

