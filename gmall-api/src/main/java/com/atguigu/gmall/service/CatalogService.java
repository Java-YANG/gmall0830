package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.bean.PmsBaseCatalog2;
import com.atguigu.gmall.bean.PmsBaseCatalog3;

import java.util.List;

public interface CatalogService {
    /**
     *  manage项目的一级查询
     * @return  List<PmsBaseCatalog1>集合
     */
    List<PmsBaseCatalog1> getCatalog1();

    /**
     *  manage项目的二级查询
     * @param catalog1Id 一级查询选中的id
     * @return List<PmsBaseCatalog2>集合
     */
    List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    /**
     *  manage项目的三级查询
     * @param catalog2Id 二级查询选中的id
     * @return List<PmsBaseCatalog3>
     */
    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);
}
