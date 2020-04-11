package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuImage;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.constant.GmallConstant;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        // 保存Sku信息表
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        // 获取sku保存的主键id
        String skuId = pmsSkuInfo.getId();

        // 保存图片表
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        // 遍历图片集合
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            // 根基sku的id保存图片
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        // 保存平台属性
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        // 遍历平台属性集合
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            // 根据sku的id保存平台属性(sku与平台属性通过中间表关联)
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        // 保存销售属性
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        // 遍历销售属性
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            // 根据sku的id保存销售属性(sku与销售属性通过中间表关联)
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

    }

    public PmsSkuInfo getSkuInfoByDB(String skuId){
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        if(skuInfo != null){
            PmsSkuImage skuImage = new PmsSkuImage();
            skuImage.setSkuId(skuId);

            List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(skuImage);

            skuInfo.setSkuImageList(pmsSkuImages);
            return skuInfo;
        }else{
            return  null;
        }
    }
    @Override
    public PmsSkuInfo getItem(String skuId) {
        Jedis jedis = null;
        PmsSkuInfo pmsSkuInfo = null;
        try {
            // 获取redis连接
            jedis = redisUtil.getJedis();
            // 拼接Key
            String skuKey = GmallConstant.ATTR_SKU + skuId + GmallConstant.ATTR_INFO;
            // 查询缓存是否有数据
            String skuInfoJson = jedis.get(skuKey);
            // 判断skuInfoJson是否有效
            if(StringUtils.isNotBlank(skuInfoJson)){
                // skuInfoJson有效
                pmsSkuInfo = JSON.parseObject(skuInfoJson, PmsSkuInfo.class);
            }else{
                //  skuInfoJson无效，生成Redis锁值value
                String value = UUID.randomUUID().toString();
                //  设置redis锁
                String skuLock = GmallConstant.ATTR_SKU + skuId + GmallConstant.ATTR_LOCK;
                String OK = jedis.set(skuLock,value,"nx","ex",10);
                if(StringUtils.isNotBlank(OK)){
                    //查数据库
                    pmsSkuInfo = getSkuInfoByDB(skuId);
                    // 判断pmsSkuInfo是否有效
                    if(pmsSkuInfo != null){
                        // 有效，存入到缓存中
                        jedis.set(skuKey,JSON.toJSONString(pmsSkuInfo));
                        // 删除自己的redis锁,使用lua脚本，防止redis与java代码传输的时间，出现误删其他线程的redis锁
                        String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                        jedis.eval(script, Collections.singletonList(skuLock),Collections.singletonList(value));
                    }else {
                        return null; // 数据库中无数据
                    }
                }else{
                    Thread.sleep(3000);
                    // 没有拿到redis(分布式)锁的人,当第一个将信息存入缓存中后，其他线程就可以直接从缓存中获取信息了，不用再访问DB
                    return getItem(skuId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关流
            if(jedis != null)
                jedis.close();
        }
        return  pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getPmsSkuInfoBySpuId(String productId) {
        // 根据productId查询PmsSkuInfo信息
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setProductId(productId);
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.select(pmsSkuInfo);

        // 根据sku的id查询sku销售值信息
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            PmsSkuSaleAttrValue pmsSkuSaleAttrValue = new PmsSkuSaleAttrValue();
            pmsSkuSaleAttrValue.setSkuId(skuInfo.getId());
            List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuSaleAttrValueMapper.select(pmsSkuSaleAttrValue);

            skuInfo.setSkuSaleAttrValueList(pmsSkuSaleAttrValues);

        }
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());

            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }
        return pmsSkuInfos;
    }

    @Override
    public PmsSkuInfo getSkuById(String productSkuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        return skuInfo;
    }

}
