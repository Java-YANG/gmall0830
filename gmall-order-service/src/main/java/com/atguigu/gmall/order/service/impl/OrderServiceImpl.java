package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.constant.GmallConstant;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void addOrder(OmsOrder omsOrder) {
        // 保存订单表数据
        omsOrderMapper.insertSelective(omsOrder);

        // 保存订单详情表数据
        String orderId = omsOrder.getId();
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
        }
    }

    @Override
    public String putTradeCode(String userId) {
        Jedis jedis = null;
        String tradeCode = null;
        try {
            jedis = redisUtil.getJedis();
            // tradeCode的设计影响着用户提交订单的范围的大小，我们的设计说明一个用户在同一时间里系统中只能由一个未提交的订单
            String tradeCodeKey = GmallConstant.ATTR_CART_USER + userId + GmallConstant.ATTR_TRADECODE;
            // 生成交易码
            tradeCode = userId + UUID.randomUUID().toString();
            // 将交易码保存到缓存中
            jedis.setex(tradeCodeKey,60*30,tradeCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null)
                jedis.close();
        }
        return tradeCode;
    }

    @Override
    public boolean getTradeCode(String userId,String tradeCode) {
        boolean result = false;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeCodeKey = GmallConstant.ATTR_CART_USER + userId + GmallConstant.ATTR_TRADECODE;
            // 使用lua脚本实行查询到马上删除缓存中的交易码
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object eval = jedis.eval(script, Collections.singletonList(tradeCodeKey), Collections.singletonList(tradeCode));
            BigDecimal tradeCodeResult = new BigDecimal((Long)eval);
            int munberResult = tradeCodeResult.compareTo(new BigDecimal("1"));
            if(munberResult == 0) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null)
                jedis.close();
        }
        return result;
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String out_trade_no) {
        // 订单表查询
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(out_trade_no);
        OmsOrder omsOrderFromDb = null;
        try {
            omsOrderFromDb = omsOrderMapper.selectOne(omsOrder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 订单详情表查询
        OmsOrderItem omsOrderItem = new OmsOrderItem();
        omsOrderItem.setOrderSn(out_trade_no);
        List<OmsOrderItem> omsOrderItems = null;
        try {
            omsOrderItems = omsOrderItemMapper.select(omsOrderItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
        omsOrderFromDb.setOmsOrderItems(omsOrderItems);

        return omsOrderFromDb;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());
        omsOrderMapper.updateByExample(omsOrder,example);
    }
}
