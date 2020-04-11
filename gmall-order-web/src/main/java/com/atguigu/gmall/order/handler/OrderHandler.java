package com.atguigu.gmall.order.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class OrderHandler {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;


    @LoginRequired(ifMustLogin = true)
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request, String addressId,String tradeCode){
        String userId = (String)request.getAttribute("userId");
        String nickname = (String)request.getAttribute("nickname");
        // 从缓存中获取交易码
        boolean result = orderService.getTradeCode(userId,tradeCode);

        // 判断页面传递过来的交易码与缓存中的交易码是否一致
        if(result){
            // 订单的其他数据，收货地址
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddress(addressId);

            // 订单的商品数据
            List<OmsCartItem> omsCartItems = cartService.getCartsByUserId(userId);

            // 生成订单信息
            OmsOrder omsOrder = new OmsOrder();
            // 生成外部订单号
            String out_trade_no = "";
            out_trade_no = "atguigu0830";
            long currentTimeMillis = System.currentTimeMillis();// 时间撮
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = simpleDateFormat.format(date);
            out_trade_no = out_trade_no + currentTimeMillis + format;

            // 设置OmsOrder中的属性
            omsOrder.setOrderSn(out_trade_no);
            omsOrder.setStatus("0");
            omsOrder.setNote("硅谷订单");
            omsOrder.setSourceType(0);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,3);
            Date time = calendar.getTime();
            omsOrder.setReceiveTime(time);// 预计送达时间
            omsOrder.setMemberUsername(nickname);
            omsOrder.setMemberId(userId);
            omsOrder.setPayAmount(getSum(omsCartItems));// 实际支付金额
            omsOrder.setTotalAmount(getSum(omsCartItems));// 订单总金额

            // 设置OmsOrder中的omsOrderItems集合
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            for (OmsCartItem omsCartItem : omsCartItems) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                if(omsCartItem.getIsChecked().equals("1")){

                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setOrderSn(out_trade_no);

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);
            // 保存订单信息
            orderService.addOrder(omsOrder);

            // 删除购物车中的选中的商品
            //cartService.deleteBuyCarts(userId); 测试阶段订单提交成功后不删除购物车中的商品数据，上线后启用
            return "redirect:http://pay.gmall.com:8088/index.html?out_trade_no=" + out_trade_no + "&totalAmount=" + getSum(omsCartItems);
        }
        return "tradeFail";
    }

    @LoginRequired(ifMustLogin = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap modelMap){
        String userId = (String) request.getAttribute("userId");

        // 显示结算数据，根据购物车中选择商品生成
        List<OmsCartItem> omsCartItems = cartService.getCartsByUserId(userId);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        // 将购物车数据转换成订单详情数据
        for (OmsCartItem omsCartItem : omsCartItems) {
            OmsOrderItem omsOrderItem = new OmsOrderItem();
            // 判断购物车中的商品是否被选中
            if(omsCartItem.getIsChecked().equals("1")){
                omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                omsOrderItem.setProductId(omsCartItem.getProductId());
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductPrice(omsCartItem.getPrice());
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItem.setProductSkuCode(omsCartItem.getProductSkuCode());
                omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
            }
            omsOrderItems.add(omsOrderItem);
        }
        // 显示其他数据，如收货地址、支付方式、卷、活动等等
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.ReceiverAddresseByUserId(userId);

        modelMap.put("userAddressList",umsMemberReceiveAddresses);
        modelMap.put("orderDetailList",omsOrderItems);
        modelMap.put("totalAmount",getSum(omsCartItems));

        // 生成交易码
        String tradeCode = orderService.putTradeCode(userId);
        modelMap.put("tradeCode",tradeCode);

        return "trade";
    }

    private BigDecimal getSum(List<OmsCartItem> omsCartItems) {

        BigDecimal sum = new BigDecimal("0");
        if (omsCartItems != null && omsCartItems.size() > 0) {
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    sum = sum.add(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
                }
            }
        }
        return sum;
    }
}
