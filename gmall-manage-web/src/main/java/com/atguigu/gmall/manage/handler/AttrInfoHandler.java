package com.atguigu.gmall.manage.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.service.AttrInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

@Controller
@CrossOrigin
public class AttrInfoHandler {
    @Reference
    private AttrInfoService attrInfoService;

    @ResponseBody
    @RequestMapping("attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrInfoService.attrInfoList(catalog3Id);

        return pmsBaseAttrInfos;
    }

    @ResponseBody
    @RequestMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){
        try{
            attrInfoService.saveAttrInfo(pmsBaseAttrInfo);

            return "success";
        }catch (Exception e) {
            e.printStackTrace();
            return "failed";
        }
    }

    @ResponseBody
    @RequestMapping("getAttrValueList")
    public PmsBaseAttrInfo getAttrValueList(String attrId){
        PmsBaseAttrInfo pmsBaseAttrInfo = attrInfoService.getAttrValueList(attrId);

        return pmsBaseAttrInfo;
    }


}
