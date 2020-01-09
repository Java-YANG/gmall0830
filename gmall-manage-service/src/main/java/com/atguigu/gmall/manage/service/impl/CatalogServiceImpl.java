package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.bean.PmsBaseCatalog2;
import com.atguigu.gmall.bean.PmsBaseCatalog3;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import com.atguigu.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import com.atguigu.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {
    @Autowired
    private PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;

    @Autowired
    private PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;

    @Autowired
    private PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        // 调用pmsBaseCatalog1Mapper层的查询方法
        List<PmsBaseCatalog1> pmsBaseCatalog1s = pmsBaseCatalog1Mapper.selectAll();

        // 返回查询结果
        return pmsBaseCatalog1s;
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        // 创建PmsBaseCatalog2对象
        PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();

        // 将参数catalog1Id封装到创建PmsBaseCatalog2对象中
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);

        // 调用pmsBaseCatalog2Mapper层中的查询方法
        List<PmsBaseCatalog2> pmsBaseCatalog2s = pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);

        // 返回查询结果
        return pmsBaseCatalog2s;
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        // 创建PmsBaseCatalog3对象
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();

        // 将参数catalog2Id封装到创建PmsBaseCatalog3对象中
        pmsBaseCatalog3.setCatalog2Id(catalog2Id);

        // 调用pmsBaseCatalog3Mapper层中的查询方法
        List<PmsBaseCatalog3> pmsBaseCatalog3s = pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);

        // 返回查询结果
        return pmsBaseCatalog3s;
    }
}
