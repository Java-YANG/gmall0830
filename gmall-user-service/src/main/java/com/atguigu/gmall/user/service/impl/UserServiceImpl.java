package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.constant.GmallConstant;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMember> getAllUserMember() {

        return umsMemberMapper.selectAll();
    }

    @Override
    public UmsMember login(String username, String password) {
        UmsMember umsMemberParam = new UmsMember();
        umsMemberParam.setUsername(username);
        umsMemberParam.setPassword(password);
        UmsMember umsMemberResult = umsMemberMapper.selectOne(umsMemberParam);
        return umsMemberResult;
    }

    @Override
    public UmsMember verify(String token) {
        UmsMember umsMember = null;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String key = GmallConstant.ATTR_CART_USER + token + GmallConstant.ATTR_TOKEN;
            String value = jedis.get(key);
            if (StringUtils.isNotBlank(value)) {
                umsMember = JSON.parseObject(value, UmsMember.class);
                // 刷新redis中token的过期时间
                jedis.expire(key, 60 * 60 * 24);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return umsMember;
    }


    @Override
    public void putToken(String token, UmsMember umsMemberFromDb) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String key = GmallConstant.ATTR_CART_USER + token + GmallConstant.ATTR_TOKEN;
            String value = JSON.toJSONString(umsMemberFromDb);
            jedis.setex(key, 60 * 60 * 24, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public List<UmsMemberReceiveAddress> ReceiverAddresseByUserId(String userId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(userId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMember addVloginUser(UmsMember umsMember) {
        // 保存前需要考虑用户是否已经使用第三方账号登录过
        String source_uid = umsMember.getSourceUid();
        UmsMember umsMemberParam = new UmsMember();
        umsMemberParam.setSourceUid(source_uid);
        // 查询数据库中是否已存在用户信息
        UmsMember umsMemberResult = umsMemberMapper.selectOne(umsMemberParam);
        // 判断umsMemberResult是否为空
        if(umsMemberResult == null){
            // 保存用户信息
            umsMemberMapper.insertSelective(umsMember);
            // 再查询保存好的用户信息
            umsMemberResult = umsMemberMapper.selectOne(umsMemberParam);
        }
        // 返回查询结果
        return umsMemberResult;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddress(String addressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(addressId);
        UmsMemberReceiveAddress umsMemberReceiveAddressFromDb = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddressFromDb;
    }
}
