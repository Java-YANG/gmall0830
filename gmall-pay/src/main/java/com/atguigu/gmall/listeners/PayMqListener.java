package com.atguigu.gmall.listeners;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.PayService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PayMqListener {

    @Autowired
    PayService payService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAY_SUCCESS_QUEUE")
    public void payConsumer(MapMessage mapMessage) throws JMSException {
        // 监听代码
        String out_trade_no = mapMessage.getString("out_trade_no");
        Long count = mapMessage.getLong("count");
        System.out.println("延迟查询" + out_trade_no + "订单===============================================================，第" + count + "次" );

        // 查询支付状态
        PaymentInfo paymentInfoForCheck = payService.checkPayStatus(out_trade_no);// 调用支付宝查询接口
        count++;
        // 获取支付状态
        String payStutas = paymentInfoForCheck.getPaymentStatus();
        // 判断支付状态
        // 支付状态为空或等待支付状态，需要重新发送消息
        if(StringUtils.isBlank(payStutas) || payStutas.equals("WAIT_BUYER_PAY")){
            // 判断延迟查询的次数是否不大于设置的查询次数(设置查询次数为7次)
            if(count <= 7){
                // 检查结果为未支付，再次发送下一延迟队列
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                payService.sendPayResultCheckQueue(paymentInfo,count);
            }else {
                // 修改支付状态，发送支付队列
                paymentInfoForCheck.setOrderSn(out_trade_no);
                paymentInfoForCheck.setPaymentStatus("用户未支付");
                // 修改支付模块的支付状态
                payService.updatePaymentInfo(paymentInfoForCheck);
                // 发送支付队列
                payService.sendPaySuccessQueue(paymentInfoForCheck);
            }
        }else {
            // 该请求和延迟查询请求重复，需要进行幂等性校验
            // 获取支付状态
            String paymentStutas = payService.sechkPayStatus(out_trade_no);
            // 判断支付状态
            if(StringUtils.isNotBlank(paymentStutas) && !paymentStutas.equals("已支付")){
                payService.updatePaymentInfo(paymentInfoForCheck);
                payService.sendPaySuccessQueue(paymentInfoForCheck);
            }
        }

    }


}
