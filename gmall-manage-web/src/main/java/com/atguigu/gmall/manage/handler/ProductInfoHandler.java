package com.atguigu.gmall.manage.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.constant.GmallConstant;
import com.atguigu.gmall.service.ProductInfoService;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@CrossOrigin
public class ProductInfoHandler {
    @Reference
    private ProductInfoService productInfoService;

    @ResponseBody
    @RequestMapping("spuImageList")
    public List<PmsProductImage> spuImageList(String spuId){

        List<PmsProductImage> pmsProductImageList = productInfoService.spuImageList(spuId);
        return  pmsProductImageList;
    }

    @ResponseBody
    @RequestMapping("spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){
        List<PmsProductSaleAttr> pmsProductSaleAttrList = productInfoService.productInfoService(spuId);
        return pmsProductSaleAttrList;
    }


    @ResponseBody
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        // 通过流获取配置文件
        String tracker_conf = ProductInfoHandler.class.getClassLoader().getResource("tracker_conf").getPath();
        try {
            // 调用引入fastdfs的Mavenz工程中的ClientGlobal对象的init(String conf_filename)初始化方法
            ClientGlobal.init(tracker_conf);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        // 创建TrackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        // 声明TrackerServer对象
        TrackerServer trackerServer = null;

        try {
            // 获取TrackerServer对象
            trackerServer = trackerClient.getTrackerServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 创建StorageClient对象，传入trackerServer，获取具体服务的Storage服务器
        StorageClient storageClient = new StorageClient(trackerServer, null);
        // 声明图片地址前部
        String imageUrl = GmallConstant.ATTR_IMAGE_URL;
        // 获取原文件名称
        String orginalFilename = multipartFile.getOriginalFilename();
        // 获取原文件名称的后缀
        int lastPoint = orginalFilename.lastIndexOf(".");
        String ext = orginalFilename.substring(lastPoint + 1);

        try {
            // 上传文件
            String[] uploadFile = storageClient.upload_file(multipartFile.getBytes(), ext, null);
            // 遍历上传文件返回的数据
            for (String url : uploadFile) {
                // 拼接图片访问的地址
                imageUrl = imageUrl + "/" + url;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        // 返回拼接后的图片地址
        return  imageUrl;
    }

    @ResponseBody
    @RequestMapping("spuList")
    public List<PmsProductInfo> spuList(String catalog3Id){
        List<PmsProductInfo> pmsProductInfoList = productInfoService.spuList(catalog3Id);

        return pmsProductInfoList;
    }

    @ResponseBody
    @RequestMapping("baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        List<PmsBaseSaleAttr> pmsBaseSaleAttrList = productInfoService.baseSaleAttrList();

        return pmsBaseSaleAttrList;
    }


    @ResponseBody
    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        productInfoService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

}
