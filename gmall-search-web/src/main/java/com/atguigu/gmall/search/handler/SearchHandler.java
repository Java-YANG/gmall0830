package com.atguigu.gmall.search.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrInfoService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Controller
public class SearchHandler {
    @Reference
    private SearchService searchService;

    @Reference
    private AttrInfoService attrInfoService;

    @LoginRequired(ifMustLogin = false)
    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String userId = (String)request.getAttribute("userId");

        return "index";
    }

    @RequestMapping("list.html")
    public String search(PmsSearchParam pmsSearchParam, ModelMap modelMap){
        // 检索
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList =  searchService.search(pmsSearchParam);

        if(pmsSearchSkuInfoList != null && pmsSearchSkuInfoList.size() > 0) {
            // 平台属性和平台属性值
            HashSet<String> set = new HashSet<>();
            for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
                List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
                for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                    String valueId = pmsSkuAttrValue.getValueId();
                    set.add(valueId);
                }
            }

            // 查询平台属性和平台属性值
            List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrInfoService.getAttrValueByValueId(set);
            // 获取valueId集合
            String[] valueIds = pmsSearchParam.getValueId();
            // 判断valueId集合是否有效
            if(valueIds != null && valueIds.length > 0){
                // 加入面包屑功能
                List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
                // 遍历valueId集合
                for (String valueId : valueIds) {
                    // 声明pmsSearchCrumb对象
                    PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                    // 设置valueId属性
                    pmsSearchCrumb.setValueId(valueId);
                    // 设置urlParam属性
                    pmsSearchCrumb.setUrlParam(getUrlAllParam(pmsSearchParam,valueId));
                    // 获取迭代器
                    Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                    // 循环迭代
                    while (iterator.hasNext()) {
                        // 获取PmsBaseAttrInfo对象
                        PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                        // 获取PmsBaseAttrInfo中的attrValueList属性
                        List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                        // 遍历PmsBaseAttrValue集合
                        for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                            // 获取PmsBaseAttrValue中的id属性
                            String id = pmsBaseAttrValue.getId();
                            // 判断id==valueId
                            if (id.equals(valueId)) {
                                // 设置PmsSearchCrumb中的valueName属性
                                pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                                // 移除所选中的平台属性
                                iterator.remove();
                            }
                        }
                    }
                    pmsSearchCrumbs.add(pmsSearchCrumb);
                }
                modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
            }
            // 将查询到的平台属性和平台属性值发送到前端页面
            modelMap.put("attrList",pmsBaseAttrInfos);
            // 将检索信息发送到前端页面
            modelMap.put("skuLsInfoList", pmsSearchSkuInfoList);
            // 将urlParam信息发送发到前端页面
            modelMap.put("urlParam",getUrlAllParam(pmsSearchParam));
        }
        return "list";
    }

    /**
     *  用于获取urlParam
     * @param pmsSearchParam
     * @param valueIdForDelete
     * @return
     */
    private String getUrlAllParam(PmsSearchParam pmsSearchParam, String... valueIdForDelete){
        // 声明urlParam
        String urlParam = "";
        // 获取搜寻条件
        String[] valueIds = pmsSearchParam.getValueId();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();

        // urlParam链接中至小包含关键字和三级Id一个
        if(StringUtils.isNotBlank(catalog3Id)){
            urlParam = "catalog3Id=" + catalog3Id;
        }
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if(valueIds != null && valueIds.length > 0){
            for (String valueId : valueIds) {
                if(!(valueIdForDelete != null && valueIdForDelete.length > 0 && valueIdForDelete[0].equals(valueId))){
                    urlParam = urlParam + "&valueId=" + valueId;
                }
            }
        }
        return  urlParam;
    }
}
