package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.pay.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PayService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class PayServiceImpl implements PayService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void addPaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());
        paymentInfoMapper.updateByExample(paymentInfo,example);
    }

    @Override
    public void sendPaySuccessQueue(PaymentInfo paymentInfo) {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAY_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(testqueue);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());
            mapMessage.setString("status",paymentInfo.getPaymentStatus());
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
            ArrayList<String> llist = new ArrayList<>();
        }
    }

    @Override
    public void sendPayResultCheckQueue(PaymentInfo paymentInfo, Long count) {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAY_RESULT_CHECK_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());
            mapMessage.setLong("count",count);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,10*1000);// 延迟20秒后该消息才正式生效

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);// 持久化
            producer.send(mapMessage);
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PaymentInfo checkPayStatus(String out_trade_no) {
        // 调用支付宝查询接口alipay.trade.query
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        // 请求参数
        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("out_trade_no",out_trade_no);
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 声明支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        if(response.isSuccess()){
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setCallbackContent(response.toString());
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setAlipayTradeNo(response.getTradeNo());
            String status = response.getTradeStatus();
            if(status.equals("TRADE_SUCCESS") || status.equals("TRADE_FINISHED")){
                status = "已支付";
            }else if(StringUtils.isNotBlank(status) || status.equals("WAIT_BUYER_PAY") || status.equals("TRADE_CLOSED")){
                status = "未支付";
            }
            paymentInfo.setPaymentStatus(status);
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return paymentInfo;
    }

    @Override
    public String sechkPayStatus(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(out_trade_no);
        PaymentInfo paymentInfoFromDb = paymentInfoMapper.selectOne(paymentInfo);
        return paymentInfoFromDb.getPaymentStatus();
    }
}
