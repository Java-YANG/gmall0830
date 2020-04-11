package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PayService {

    void addPaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void sendPaySuccessQueue(PaymentInfo paymentInfo);

    void sendPayResultCheckQueue(PaymentInfo paymentInfo, Long count);

    PaymentInfo checkPayStatus(String out_trade_no);

    String sechkPayStatus(String out_trade_no);
}
