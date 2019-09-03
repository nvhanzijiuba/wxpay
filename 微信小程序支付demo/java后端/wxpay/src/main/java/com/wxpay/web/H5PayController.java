package com.wxpay.web;

import com.github.wxpay.sdk.WXPayUtil;
import com.wxpay.config.HttpClient;
import com.wxpay.config.IdWorker;
import com.wxpay.config.RequestHandler;
import com.wxpay.dao.WxPayDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Controller
@RequestMapping("/h5")
@Slf4j
public class H5PayController {

    @Autowired
    WxPayDao wxPayDao;
    @Autowired
    IdWorker idWorker;
    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Value("${appsecret}")
    private String appsecret;

    @RequestMapping(value = "/h5pay", method = RequestMethod.GET)
    public String h5pay() {//InfoDTO product, ModelMap map
        // mweb_url为拉起微信支付收银台的中间页面，可通过访问该url来拉起微信客户端，完成支付,mweb_url的有效期为5分钟。
        String mweb_url = wxPayDao.weixinPayH5();
        if (StringUtils.isNotBlank(mweb_url)) {
            return "redirect:" + mweb_url;
        } else {
            return "redirect:https://blog.52itstyle.com";// 自定义错误页面
        }
    }

    @RequestMapping(value = "/h5pays", method = RequestMethod.GET)
    public String h5spay() {//InfoDTO product, ModelMap map
        // mweb_url为拉起微信支付收银台的中间页面，可通过访问该url来拉起微信客户端，完成支付,mweb_url的有效期为5分钟。

        String aNative = wxPayDao.weixinPayH5();
        if (StringUtils.isNotBlank(aNative)) {
            return "redirect:" + aNative;
        } else {
            return "redirect:https://blog.52itstyle.com";// 自定义错误页面
        }
    }

    public String createNative(String out_trade_no, String total_fee) {
        SortedMap<String, String> map = new TreeMap<>();
        map.put("appid", appid);
        map.put("mch_id", partner);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        RequestHandler reqHandler = new RequestHandler(null, null);
        reqHandler.init(appid, appsecret, partnerkey);
        String sign = reqHandler.createSign(map);
        map.put("sign", sign);
        map.put("sign_type", "MD5");
        map.put("body", "测试H5支付");
        map.put("out_trade_no", out_trade_no);
        map.put("total_fee", total_fee);
        map.put("spbill_create_ip", "127.0.0.1");
        map.put("notify_url", "http://test.com");
        map.put("trade_type", "NATIVE");
        try {
            String signedXml = WXPayUtil.generateSignedXml(map, partnerkey);
            System.out.println("signedXml = " + signedXml);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();
            //获得结果
            String content = httpClient.getContent();
            System.out.println("content = " + content);
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
            Map<String, String> zf = new HashMap<>();
            zf.put("code_url", xmlToMap.get("code_url"));//支付地址
            zf.put("total_fee", total_fee);
            zf.put("out_trade_no", out_trade_no);
            return xmlToMap.get("code_url");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
