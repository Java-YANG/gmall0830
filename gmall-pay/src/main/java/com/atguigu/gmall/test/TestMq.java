package com.atguigu.gmall.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class TestMq {
    public static void main(String[] args) {

        // 老板喝水
        drink();
        // 老板开会

    }

    // 队列模式
    public static void drink(){
        ActiveMQConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            // 第一个参数表示是否开启事务，如果选择true，第二个参数值相当等于0；
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue drink = session.createQueue("drink");
            MessageProducer producer = session.createProducer(drink);
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("I am koukele!");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT); // 持久化
            producer.send(textMessage);
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    // 会话模式
    public static void meetting(){
        ActiveMQConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Topic meetting = session.createTopic("meetting");
            MessageProducer producer = session.createProducer(meetting);
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("do everything for the great atguigu");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
