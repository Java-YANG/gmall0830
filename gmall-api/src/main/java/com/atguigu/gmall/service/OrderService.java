package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsOrder;

public interface OrderService {

    void addOrder(OmsOrder omsOrder);

    String putTradeCode(String userId);

    boolean getTradeCode(String userId,String tradeCode);

    OmsOrder getOrderByOutTradeNo(String out_trade_no);

    void updateOrder(OmsOrder omsOrder);
}
