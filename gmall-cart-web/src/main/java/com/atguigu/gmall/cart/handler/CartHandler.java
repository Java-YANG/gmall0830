package com.atguigu.gmall.cart.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.constant.GmallConstant;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartHandler {
    @Reference
    private CartService cartService;

    @Reference
    private SkuService skuService;

    @LoginRequired(ifMustLogin = false)
    @RequestMapping("checkCart")
    public String checkCart(String skuId,String isChecked,HttpServletRequest request,ModelMap modelMap,HttpServletResponse response){

        // 用户id
        String userId = (String)request.getAttribute("userId");
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        // 判断用户是否登录
        if(StringUtils.isNotBlank(userId)){
            // 用户登录,操作DB数据
            // 修改DB数据
            cartService.updateCartChecket(skuId,userId,isChecked);
            // 查询修改后的DB数据
            omsCartItems = cartService.getCartsByUserId(userId);
        }else {
            // 用户未登录，操作Cookie数据
            String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookieStr)){
                omsCartItems = JSON.parseArray(cartListCookieStr,OmsCartItem.class);
                for (OmsCartItem omsCartItem : omsCartItems) {
                    if(omsCartItem.getProductSkuId().equals(skuId)){
                        omsCartItem.setIsChecked(isChecked);
                    }
                }
                // 覆盖Cookie
                CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(omsCartItems),60*60*24,true);
            }
        }
        modelMap.put(GmallConstant.ATTR_CART_LIST,omsCartItems);

        // 计算总价格
        if(omsCartItems != null && omsCartItems.size() > 0){
            BigDecimal sum = getSum(omsCartItems);
            modelMap.put(GmallConstant.ATTR_SUM,sum);
        }
        return "cartListInner";
    }

    @LoginRequired(ifMustLogin = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, ModelMap modelMap){

        // 用户id
        String userId = (String)request.getAttribute("userId");
        // 创建集合
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        // 判断用户是否登录
        if(StringUtils.isNotBlank(userId)){
            // 用户登录
            omsCartItems = cartService.getOmsCartItemByUserId(userId);

        }else{
            // 用户未登录
            String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if(StringUtils.isNotBlank(cartListCookieStr)){
                omsCartItems = JSON.parseArray(cartListCookieStr, OmsCartItem.class);
            }
        }
        modelMap.put(GmallConstant.ATTR_CART_LIST,omsCartItems);

        // 计算总价格
        if(omsCartItems != null && omsCartItems.size() > 0){
            BigDecimal sum = getSum(omsCartItems);

            modelMap.put(GmallConstant.ATTR_SUM,sum);
        }
        return "cartList";
    }

    private BigDecimal getSum(List<OmsCartItem> omsCartItems) {
        BigDecimal sum = new BigDecimal("0");
        if(omsCartItems != null && omsCartItems.size() > 0){
            for (OmsCartItem omsCartItem : omsCartItems) {
                // 判断isChecked是否为1
                if(omsCartItem.getIsChecked().equals("1")){
                    sum = sum.add(omsCartItem.getQuantity().multiply(omsCartItem.getPrice()));
                }
            }
        }
        return sum;
    }

    @LoginRequired(ifMustLogin = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, String productSkuId, BigDecimal quantity){
        // 根据skuId查询商品详情
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(productSkuId);
        // 判断pmsSkuInfo是否有效
        if(pmsSkuInfo != null){
            // 声明OmsCartItem对象
            OmsCartItem omsCartItem = new OmsCartItem();
            // 将pmsSkuInfo转换成omsCartItem
            omsCartItem.setCreateDate(new Date());
            omsCartItem.setIsChecked("1");// 默认选中
            omsCartItem.setPrice(pmsSkuInfo.getPrice());
            omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
            omsCartItem.setProductId(pmsSkuInfo.getProductId());
            omsCartItem.setProductName(pmsSkuInfo.getSkuName());
            omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
            omsCartItem.setProductSkuId(pmsSkuInfo.getId());
            omsCartItem.setQuantity(quantity);
            omsCartItem.setTotalPrice(quantity.multiply(pmsSkuInfo.getPrice()));

            // 声明购物车集合,购物车模块所有的分支都操作该集合
            List<OmsCartItem> omsCartItems = new ArrayList<>();
            // 用户id
            String userId = (String)request.getAttribute("userId");

            // 判断用户是否登录
            if(StringUtils.isNotBlank(userId)){
                // 用户登录
                omsCartItem.setMemberId(userId);
                // 根据userId和productSkuId查询数据库中是否有添加的商品
                OmsCartItem omsCartItemForUser = cartService.isExistOmsCartItem(userId,productSkuId);

                // 判断omsCartItemForUser是否有效
                if(omsCartItemForUser != null){
                    // 更新
                    omsCartItemForUser.setQuantity(omsCartItemForUser.getQuantity().add(quantity));
                    cartService.updateOmsCartItem(omsCartItemForUser);
                }else{
                    // 添加
                    cartService.insertOmsCartItem(omsCartItem);
                }

            }else{
                // 用户未登录,对Cookie进行增删改
                String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);
                // 判断Cookie是否为空
                if(StringUtils.isNotBlank(cartListCookieStr)){
                    // Cookie不为空,判断cartListCookie和要添加的商品是否重复
                    // 将JSON字符串转换成OmsCartItem对象
                    omsCartItems = JSON.parseArray(cartListCookieStr, OmsCartItem.class);
                    boolean newCart = ifNewCart(omsCartItems,omsCartItem);
                    // 判断是否重复
                    if(newCart){
                      // 不重复
                        omsCartItems.add(omsCartItem);
                    }else{
                       // 重复,修改商品数量
                        for (OmsCartItem cartItem : omsCartItems) {
                            if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                                cartItem.setQuantity(cartItem.getQuantity().add(quantity));
                            }
                        }
                    }
                }else{
                    // Cookie为空，添加
                    omsCartItems.add(omsCartItem);
                }
                // 覆盖浏览器上的Cookie
                CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(omsCartItems),60*60*24,true);
            }
        }
        return "redirect:/success.html";
    }

    private boolean ifNewCart(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean result = true;
        for (OmsCartItem cartItem : omsCartItems) {
            if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                result = false;
            }
        }
        return result;
    }
}


