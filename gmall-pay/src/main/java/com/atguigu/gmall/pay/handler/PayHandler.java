package com.atguigu.gmall.pay.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.config.AlipayConfig;
import com.atguigu.gmall.constant.GmallConstant;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PayService;
import com.atguigu.gmall.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Controller
public class PayHandler {
    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PayService payService;


    @LoginRequired(ifMustLogin = true)
    @RequestMapping("wx/submit")
    @ResponseBody
    public String wxSubimit(HttpServletRequest request, String out_trade_no){

        // 由于微信的外部订单号字符串长度最大为32，对out_trade_no进行切割
        if(out_trade_no.length() > 32){
            out_trade_no = out_trade_no.substring(0,31);
        }

        Map aNativeMap = createNative(out_trade_no, "1");
        String code_url = (String)aNativeMap.get("code_url");
        return code_url;
    }

    private Map createNative(String out_trade_no, String total_feel){
        // 1、创建参数
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("out_trad_no",out_trade_no);// 商户订单号
        paramMap.put("appid", GmallConstant.ATTR_APP_ID);// 公众号
        paramMap.put("mch_id",GmallConstant.ATTR_MCH_ID);// 商户号
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
        paramMap.put("body","尚硅谷");// 商品描述
        paramMap.put("total_feel",total_feel);// 总金额（单位分）
        paramMap.put("spbill_create_ip","127.0.0.1");// ip
        paramMap.put("notify_url","http://2z72m78296.wicp.vip/wx/callback/notify");// 回调地址
        paramMap.put("trade_type","NATIVE");

        try {
            // 2、生成要发送的XML
            String xmlParam = WXPayUtil.generateSignedXml(paramMap,GmallConstant.ATTR_PRIVATE_KEY);
            System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();

            // 3、获取结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String,String> map = new HashMap<>();
            map.put("code_url",resultMap.get("code_url"));
            map.put("out_trad_no",out_trade_no);
            map.put("total_feel",total_feel);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @LoginRequired(ifMustLogin = true)
    @RequestMapping("alipay/callback/return")
    public String alipayCallbackReturn(HttpServletRequest request){
        // 解析支付宝回调的参数
        String trade_no = request.getParameter("trade_no"); // 支付宝订单号
        String out_trade_no = request.getParameter("out_trade_no"); // 外部订单号
        String sign = request.getParameter("sign"); // 防伪标签
        String app_id = request.getParameter("app_id");

        // 根据支付宝回调信息跟新PaymentInfo表数据
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAlipayTradeNo(trade_no);
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(request.getQueryString());
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setOrderSn(out_trade_no);

        // 该请求和延迟查询请求重复，要进行幂等性校验
        String paymentStutas = payService.sechkPayStatus(out_trade_no);
        if(StringUtils.isNotBlank(paymentStutas) && !paymentStutas.equals("已支付")){
            payService.updatePaymentInfo(paymentInfo);
            // 给订单模块发送消息，订单模块更新订单状态
            payService.sendPaySuccessQueue(paymentInfo);
        }
        return "finish";
    }

    @LoginRequired(ifMustLogin = true)
    @RequestMapping("alipay/submit")
    @ResponseBody
    public String alipaySubmit(HttpServletRequest request,String out_trade_no){
        String userId = (String)request.getAttribute("userId");
        String nickname = (String)request.getAttribute("nickname");
        // 创建请求接口的对象
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\","+
//                "  }");//填充业务参数
        // 查询数据库中的订单数据
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(out_trade_no);

        Map<String,Object> map = new HashMap<>();
        // 外部订单号
        map.put("out_trade_no",out_trade_no);
        // 支付宝的产品名
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        // 总金额
        //map.put("total_amount",omsOrder.getTotalAmount());
        map.put("total_amount",0.01);
        //订单的商品名称
        map.put("subject",omsOrder.getOmsOrderItems().get(0).getProductName());
        String requestMapJson = JSON.toJSONString(map);
        alipayRequest.setBizContent(requestMapJson);
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 生成支付数据保存到后台db
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(out_trade_no);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setSubject(omsOrder.getOmsOrderItems().get(0).getProductName());
        paymentInfo.setTotalAmount(omsOrder.getTotalAmount());
        payService.addPaymentInfo(paymentInfo);

        // 给支付系统发送延迟队列消息，定时访问支付宝，查询支付状态
        payService.sendPayResultCheckQueue(paymentInfo,1l);
        return form;
    }

    @RequestMapping("index")
    public String index(String out_trade_no, BigDecimal totalAmount, ModelMap modelMap){
        modelMap.put("out_trade_no",out_trade_no);
        modelMap.put("totalAmount",totalAmount);
        return "index";
    }
}
