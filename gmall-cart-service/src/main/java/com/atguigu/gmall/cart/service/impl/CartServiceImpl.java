package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.constant.GmallConstant;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem isExistOmsCartItem(String userId, String productSkuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductSkuId(productSkuId);
        omsCartItem.setMemberId(userId);
        OmsCartItem omsCartItemFromUser = omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItemFromUser;
    }

    @Override
    public void updateOmsCartItem(OmsCartItem omsCartItemForUser) {
        Jedis jedis = null;
        try {
            // 修改DB中的商品数量
            Example example = new Example(OmsCartItem.class);
            example.createCriteria().andEqualTo("id",omsCartItemForUser.getId());
            omsCartItemMapper.updateByExampleSelective(omsCartItemForUser,example);

            // 同步缓存
            String key = GmallConstant.ATTR_CART_USER + omsCartItemForUser.getMemberId() + GmallConstant.ATTR_CART;
            jedis = redisUtil.getJedis();
            jedis.hset(key,omsCartItemForUser.getProductSkuId(), JSON.toJSONString(omsCartItemForUser));

            // 购物车缓存整体同步(目前为学习消息队列等技，暂时写死)
            flushUserCartsCache(omsCartItemForUser.getMemberId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null)
                jedis.close();
        }

    }

    @Override
    public void insertOmsCartItem(OmsCartItem omsCartItem) {
        Jedis jedis = null;
        try {
            // 保存商品到DB中
            omsCartItemMapper.insertSelective(omsCartItem);

            // 同步缓存
            String key = GmallConstant.ATTR_CART_USER + omsCartItem.getMemberId() + GmallConstant.ATTR_CART;
            jedis = redisUtil.getJedis();
            jedis.hset(key,omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));

            // 购物车缓存整体同步(目前为学习消息队列等技，暂时写死)
            flushUserCartsCache(omsCartItem.getMemberId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null)
                jedis.close();
        }
    }

    @Override
    public List<OmsCartItem> getOmsCartItemByUserId(String userId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(userId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
        return omsCartItems;
    }

    @Override
    public void updateCartChecket(String skuId, String userId, String isChecked) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("productSkuId",skuId).andEqualTo("memberId",userId);
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setIsChecked(isChecked);
        omsCartItemMapper.updateByExampleSelective(omsCartItem,example);

        Jedis jedis = null;
        try {
            // 同步缓存
            jedis = redisUtil.getJedis();

            // 购物车缓存整体同步(目前为学习消息队列等技，暂时写死)
            flushUserCartsCache(userId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null)
                jedis.close();
        }
    }

    @Override
    public List<OmsCartItem> getCartsByUserId(String userId) {
        // 查询Cache中的数据
        List<OmsCartItem> omsCartItems = getUserCartCache(userId);
        return omsCartItems;
    }

    @Override
    public void deleteBuyCarts(String userId) {
        // 删除购物车中被选中的商品
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",userId).andEqualTo("isChecked","1");
        omsCartItemMapper.deleteByExample(example);

        // 同步缓存
        flushUserCartsCache(userId);
    }

    private List<OmsCartItem> getUserCartCache(String userId) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            // 获取Cache中所有数据
            List<String> hvals = jedis.hvals(GmallConstant.ATTR_CART_USER + userId + GmallConstant.ATTR_CART);
            // 判断hvals是否有效
            if(hvals != null && hvals.size() > 0){
                for (String hval : hvals) {
                    // 将为字符串类型的JSON数据hval转换成OmsCartItem对象
                    OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                    omsCartItems.add(omsCartItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null)
                jedis.close();
        }
        return omsCartItems;
    }

    private void flushUserCartsCache(String userId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(userId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            // 判断omsCartItems是否有效
            if(omsCartItems != null && omsCartItems.size() > 0){
                // 声明Map集合用来存储购物车中的商品信息
                Map<String,String> map = new HashMap<>();
                for (OmsCartItem cartItem : omsCartItems) {
                    String key = cartItem.getProductSkuId();
                    String value = JSON.toJSONString(cartItem);
                    map.put(key,value);
                }
                // 同步缓存
                // 批量设置缓存数据
                jedis.hmset(GmallConstant.ATTR_CART_USER + userId + GmallConstant.ATTR_CART,map);

            }else {
                // 购物车中没有数据，删除缓存中的key
                jedis.del(GmallConstant.ATTR_CART_USER + userId + GmallConstant.ATTR_CART);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis != null)
                jedis.close();
        }
    }


}
