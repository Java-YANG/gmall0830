package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsSkuInfo;

import java.util.List;

public interface SkuService {
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getItem(String skuId);

    List<PmsSkuInfo> getPmsSkuInfoBySpuId(String productId);

    List<PmsSkuInfo> getAllSku();

    PmsSkuInfo getSkuById(String productSkuId);
}
