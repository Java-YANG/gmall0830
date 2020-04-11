package com.atguigu.gmall.item.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.ProductInfoService;
import com.atguigu.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class ItemHandler {

    @Reference
    private SkuService skuService;

    @Reference
    private ProductInfoService productInfoService;


    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap map){
        // 查询PmsSkuInfo信息
        PmsSkuInfo pmsSkuInfo = skuService.getItem(skuId);
        // 判断PmsSkuInfp是否有效
        if(pmsSkuInfo == null){
            // 无效
            return "item";
        }
        // 有效，获取spuId
        String productId = pmsSkuInfo.getProductId();
        // 将pmsSkuInfo存入对象域中
        map.put("skuInfo",pmsSkuInfo);
        // 查询PmsProductSaleAttr信息，根据skuId和productId
        List<PmsProductSaleAttr> pmsProductSaleAttrList =  productInfoService.spuSaleAttrListCheckBySku(skuId,productId);
        map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrList);

        // 创建Map集合封装当前sup下的skuId和销售属性的对应关系
        Map<String,String> skuIdMap = new HashMap<>();
        // 查询当前sup下的销售属性和skuId的对应关系
        // 根据spuId查询PmsSkuInfo信息
        List<PmsSkuInfo> pmsSkuInfoList = skuService.getPmsSkuInfoBySpuId(productId);
        // 遍历pmsSkuInfoList
        for (PmsSkuInfo skuInfo : pmsSkuInfoList) {
            // 获取skuId，做skuIdMap的value值
            String value = skuInfo.getId();
            // 声明skuIdMap的key
            String key = "";
            // 根据PmsSkuInfo获取PmsSkuSaleAttrValue，并遍历
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                // 拼接skuIdMap的key
                key = key + "|" +  pmsSkuSaleAttrValue.getSaleAttrValueId();
            }
            // 将获得的key和value值添加到skuIdMap集合中
            skuIdMap.put(key,value);
        }
        // 将封装好的集合转换成JSON
        String json = JSON.toJSONString(skuIdMap);
        // 将JSON存入request域中，发送到页面
        map.put("skuIdMap",json);
        return "item";
    }

}
