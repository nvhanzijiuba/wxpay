package com.wxpay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wxpay.sdk.WXPayUtil;
import com.wxpay.config.*;
import com.wxpay.dao.WxPayDao;
import com.wxpay.pojo.InfoDTO;
import com.wxpay.pojo.OAuthJsToken;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weixin4j.WeixinException;
import org.weixin4j.http.HttpsClient;
import org.weixin4j.http.Response;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
@Slf4j
public class WxPayDaoImpl implements WxPayDao {
    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Value("${appsecret}")
    private String appsecret;

    @Value("${notifyurl}")
    private String notifyurl;

    @Value("${grant_type}")
    private String grant_type;

    @Autowired
    private IdWorker idWorker;

    /**
     * 生成二维码
     *
     * @param out_trade_no 订单号
     * @param total_fee    金额
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee, HttpServletRequest request) {
        SortedMap<String, String> map = new TreeMap<>();
        map.put("appid", appid);
        map.put("mch_id", partner);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        RequestHandler reqHandler = new RequestHandler(null, null);
        reqHandler.init(appid, appsecret, partnerkey);
        String sign = reqHandler.createSign(map);
        map.put("sign", sign);
        map.put("sign_type", "MD5");
        map.put("body", "测试购买商品");
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
            return zf;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        SortedMap<String, String> map = new TreeMap<>();
        map.put("appid", appid);
        map.put("mch_id", partner);
        map.put("out_trade_no", out_trade_no);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        RequestHandler reqHandler = new RequestHandler(null, null);
        reqHandler.init(appid, appsecret, partnerkey);
        String sign = reqHandler.createSign(map);
        map.put("sign", sign);
        map.put("sign_type", "MD5");
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        try {
            String signedXml = WXPayUtil.generateSignedXml(map, partnerkey);
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();
            String content = httpClient.getContent();
            Map<String, String> toMap = WXPayUtil.xmlToMap(content);
            System.out.println("toMap = " + toMap);
            return toMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String weixinRefund(InfoDTO product) {
        log.info("订单号：{}微信退款", "6666666666");
        String message = Constants.SUCCESS;
        try {
            // 账号信息
            String mch_id = partner; // 商业号
            String key = partnerkey; // key

            SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
            ConfigUtil.commonParams(packageParams);
            packageParams.put("out_trade_no", "6666666666");// 商户订单号
            packageParams.put("out_refund_no", "6666666666");// 商户退款单号
            String totalFee = product.getTotalFee();
            totalFee = CommonUtil.subZeroAndDot(totalFee);
            packageParams.put("total_fee", totalFee);// 总金额
            packageParams.put("refund_fee", totalFee);// 退款金额
            packageParams.put("op_user_id", mch_id);// 操作员帐号, 默认为商户号
            String sign = PayCommonUtil.createSign("UTF-8", packageParams, key);
            packageParams.put("sign", sign);// 签名
            String requestXML = PayCommonUtil.getRequestXml(packageParams);
            String weixinPost = ClientCustomSSL.doRefund(ConfigUtil.REFUND_URL,
                    requestXML).toString();
            Map map = XMLUtil.doXMLParse(weixinPost);
            String returnCode = (String) map.get("return_code");
            if ("SUCCESS".equals(returnCode)) {
                String resultCode = (String) map.get("result_code");
                if ("SUCCESS".equals(resultCode)) {
                    log.info("订单号：{}微信退款成功并删除二维码", "6666666666");
                } else {
                    String errCodeDes = (String) map.get("err_code_des");
                    log.info("订单号：{}微信退款失败:{}", "6666666666",
                            errCodeDes);
                    message = Constants.FAIL;
                }
            } else {
                String returnMsg = (String) map.get("return_msg");
                log.info("订单号：{}微信退款失败:{}", "6666666666",
                        returnMsg);
                message = Constants.FAIL;
            }
        } catch (Exception e) {
            log.error("订单号：{}微信支付失败(系统异常)", "6666666666", e);
            message = Constants.FAIL;
        }
        return message;
    }

    @Override
    public String weixinCloseorder(InfoDTO product) {
        log.info("订单号：{}微信关闭订单", "6666666666");
        String message = Constants.SUCCESS;
        try {
            String key = ConfigUtil.API_KEY; // key
            SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
            ConfigUtil.commonParams(packageParams);
            packageParams.put("out_trade_no", "6666666666");// 商户订单号
            String sign = PayCommonUtil.createSign("UTF-8", packageParams, key);
            packageParams.put("sign", sign);// 签名
            String requestXML = PayCommonUtil.getRequestXml(packageParams);
            String resXml = HttpUtil.postData(ConfigUtil.CLOSE_ORDER_URL,
                    requestXML);
            Map map = XMLUtil.doXMLParse(resXml);
            String returnCode = (String) map.get("return_code");
            if ("SUCCESS".equals(returnCode)) {
                String resultCode = (String) map.get("result_code");
                if ("SUCCESS".equals(resultCode)) {
                    log.info("订单号：{}微信关闭订单成功", "6666666666");
                } else {
                    String errCode = (String) map.get("err_code");
                    String errCodeDes = (String) map.get("err_code_des");
                    if ("ORDERNOTEXIST".equals(errCode)
                            || "ORDERCLOSED".equals(errCode)) {// 订单不存在或者已经关闭
                        log.info("订单号：{}微信关闭订单:{}", "6666666666",
                                errCodeDes);
                    } else {
                        log.info("订单号：{}微信关闭订单失败:{}",
                                "6666666666", errCodeDes);
                        message = Constants.FAIL;
                    }
                }
            } else {
                String returnMsg = (String) map.get("return_msg");
                log.info("订单号：{}微信关闭订单失败:{}", "6666666666",
                        returnMsg);
                message = Constants.FAIL;
            }
        } catch (Exception e) {
            log.error("订单号：{}微信关闭订单失败(系统异常)", "6666666666", e);
            message = Constants.FAIL;
        }
        return message;
    }

    @Override
    public void saveBill() {
        try {
            String key = ConfigUtil.API_KEY; // key
            // 获取两天以前的账单
            // String billDate = DateUtil.getBeforeDayDate("2");
            SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
            ConfigUtil.commonParams(packageParams);// 公用部分
            packageParams.put("bill_type", "ALL");// ALL，返回当日所有订单信息，默认值SUCCESS，返回当日成功支付的订单REFUND，返回当日退款订单
            // packageParams.put("tar_type", "GZIP");//压缩账单
            packageParams.put("bill_date", "20161206");// 账单日期
            String sign = PayCommonUtil.createSign("UTF-8", packageParams, key);
            packageParams.put("sign", sign);// 签名
            String requestXML = PayCommonUtil.getRequestXml(packageParams);
            String resXml = HttpUtil.postData(ConfigUtil.DOWNLOAD_BILL_URL,
                    requestXML);
            if (resXml.startsWith("<xml>")) {
                Map map = XMLUtil.doXMLParse(resXml);
                String returnMsg = (String) map.get("return_msg");
                log.info("微信查询订单失败:{}", returnMsg);
            } else {
                // 入库
            }
        } catch (Exception e) {
            log.error("微信查询订单异常", e);
        }

    }

    @Override
    public String weixinPayMobile(InfoDTO product) {
        StringBuffer url = new StringBuffer();
        String totalFee = product.getTotalFee();
        // redirect_uri 需要在微信支付端添加认证网址
        totalFee = CommonUtil.subZeroAndDot(totalFee);
        url.append("http://open.weixin.qq.com/connect/oauth2/authorize?");
        url.append("appid=" + ConfigUtil.APP_ID);
        url.append("&redirect_uri=" + "不知道→。。。" + "weixinMobile/dopay?");
        url.append("outTradeNo=" + "6666666666" + "&totalFee="
                + totalFee);
        url.append("&response_type=code&scope=snsapi_base&state=");
        url.append("#wechat_redirect");
        return url.toString();
    }

    @Override
    public String weixinPayH5() {//InfoDTO product
        //log.info("订单号：{}发起H5支付", "6666666666");
        String mweb_url = "";
        try {
            // 账号信息
            String key = partnerkey; // key
            String trade_type = "MWEB";// 交易类型 H5 支付
            SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
            packageParams.put("appid", appid);// 公众账号ID
            packageParams.put("mch_id", partner);// 商户号
            packageParams.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
            packageParams.put("product_id", "1111111111");// 商品ID
            packageParams.put("body", "测试h5");// 商品描述
            packageParams.put("out_trade_no", "6666666666666");// 商户订单号
            //String totalFee = product.getTotalFee();
            // totalFee = CommonUtil.subZeroAndDot(totalFee);
            packageParams.put("total_fee", "1");// 总金额
            // H5支付要求商户在统一下单接口中上传用户真实ip地址 spbill_create_ip
            packageParams.put("spbill_create_ip", "127.0.0.1");// 发起人IP地址
            packageParams.put("notify_url", notifyurl);// 回调地址
            packageParams.put("trade_type", trade_type);// 交易类型
            // H5支付专用
            JSONObject value = new JSONObject();
            value.put("type", "WAP");
            value.put("wap_url", "https://blog.52itstyle.com");// //WAP网站URL地址
            value.put("wap_name", "测试充值");// WAP 网站名
            JSONObject scene_info = new JSONObject();
            scene_info.put("h5_info", value);
            packageParams.put("scene_info", scene_info.toString());

            String sign = PayCommonUtil.createSign("UTF-8", packageParams, key);
            packageParams.put("sign", sign);// 签名

            String requestXML = PayCommonUtil.getRequestXml(packageParams);
            String resXml = HttpUtil.postData(ConfigUtil.UNIFIED_ORDER_URL,
                    requestXML);
            Map map = XMLUtil.doXMLParse(resXml);
            String returnCode = (String) map.get("return_code");
            if ("SUCCESS".equals(returnCode)) {
                String resultCode = (String) map.get("result_code");
                if ("SUCCESS".equals(resultCode)) {
                    log.info("订单号：{}发起H5支付成功", "6666666666666");
                    mweb_url = (String) map.get("mweb_url");
                } else {
                    String errCodeDes = (String) map.get("err_code_des");
                    log.info("订单号：{}发起H5支付(系统)失败:{}",
                            "6666666666666", errCodeDes);
                }
            } else {
                String returnMsg = (String) map.get("return_msg");
                log.info("(订单号：{}发起H5支付(通信)失败:{}", "6666666666666",
                        returnMsg);
            }
        } catch (Exception e) {
            log.error("订单号：{}发起H5支付失败(系统异常))", "6666666666666", e);
        }
        return mweb_url;
    }

    @Override
    public String weixinPayJSAPI(String out_trade_no, String total_fee, String openid) {
        String mweb_url = "";
        try {
            // 账号信息
            SortedMap<String, String> packageParams = new TreeMap<String, String>();
            packageParams.put("appid", appid);//小程序ID
            packageParams.put("mch_id", partner);// 商户号
            packageParams.put("device_info", "WEB");
            packageParams.put("body", "测试小程序");// 商品描述
            packageParams.put("trade_type", "JSAPI");// 交易类型
            packageParams.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
            packageParams.put("notify_url", notifyurl);// 回调地址
            packageParams.put("out_trade_no", out_trade_no);// 商户订单号
            packageParams.put("total_fee", total_fee);// 总金额
            packageParams.put("openid", openid);
            packageParams.put("spbill_create_ip", "127.0.0.1");// 发起人IP地址
            RequestHandler reqHandler = new RequestHandler(null, null);
            String sign = reqHandler.createSign(packageParams);
            packageParams.put("sign", sign);
            reqHandler.init(appid, appsecret, partnerkey);
            // H5支付要求商户在统一下单接口中上传用户真实ip地址 spbill_create_ip
            String signedXml = WXPayUtil.generateSignedXml(packageParams, partnerkey);
            System.out.println("signedXml = " + signedXml);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();
            //获得结果
            String content = httpClient.getContent();
            Map map = XMLUtil.doXMLParse(content);
            String returnCode = (String) map.get("return_code");
            if ("SUCCESS".equals(returnCode)) {
                String resultCode = (String) map.get("result_code");
                if ("SUCCESS".equals(resultCode)) {
                    log.info("订单号：{}发起小程序支付成功", out_trade_no);
                    mweb_url = (String) map.get("mweb_url");
                } else {
                    String errCodeDes = (String) map.get("err_code_des");
                    log.info("订单号：{}发起小程序支付(系统)失败:{}",
                            out_trade_no, errCodeDes);
                }
            } else {
                String returnMsg = (String) map.get("return_msg");
                log.info("(订单号：{}发起小程序支付(通信)失败:{}", out_trade_no,
                        returnMsg);
            }
        } catch (Exception e) {
            log.error("订单号：{}发起H5支付失败(系统异常))", out_trade_no, e);
        }
        return mweb_url;
    }

    @Override
    public Map<String, String> getUserInfo(String code) throws Exception {
        Map<String, String> ret = new HashMap<String, String>();
        //拼接参数
        String param = "?grant_type=" + grant_type + "&appid=" + appid + "&secret=" + appsecret + "&js_code=" + code;
       System.out.println("param22=="+param);
        //创建请求对象
        HttpsClient http = new HttpsClient();
        //调用获取access_token接口
        Response res = http.get("https://api.weixin.qq.com/sns/jscode2session" + param);
        //根据请求结果判定，是否验证成功
        com.alibaba.fastjson.JSONObject jsonObj = res.asJSONObject();
        if (jsonObj != null) {
            Object errcode = jsonObj.get("errcode");
            if (errcode != null) {
                //返回异常信息
                throw new WeixinException(String.valueOf(Integer.parseInt(errcode.toString())));
            }
            ObjectMapper mapper = new ObjectMapper();
            OAuthJsToken oauthJsToken = mapper.readValue(jsonObj.toJSONString(),OAuthJsToken.class);
            ret.put("openid", oauthJsToken.getOpenid());
            System.out.println("openid======="+oauthJsToken.getOpenid());
        }
        return ret;

//        String requestUrl2 = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token + "&openid=" + openid + "&lang=zh_CN";
//        String userInfoStr = new HttpRequestor().doGet(requestUrl2);
//        JSONObject wxUserInfo = JSONObject.fromObject(userInfoStr);
//        System.out.println("wxUserInfo = " + wxUserInfo);
//        return oppidObj;
    }

    @Override
    public Map jsapi2(HttpServletRequest request,String openId) {
        try {
            System.out.println("param--openId=="+openId);
            //拼接统一下单地址参数
            Map<String, String> paraMap = new HashMap<String, String>();
            //获取请求ip地址
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip.indexOf(",") != -1) {
                String[] ips = ip.split(",");
                ip = ips[0].trim();
            }

            paraMap.put("appid", appid);
            paraMap.put("body", "测试-订单结算");
            paraMap.put("mch_id", partner);
            paraMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paraMap.put("openid", openId);
            paraMap.put("out_trade_no", System.currentTimeMillis()+"");//订单号
            paraMap.put("spbill_create_ip", ip);
            paraMap.put("total_fee", "1");
            paraMap.put("trade_type", "JSAPI");
            paraMap.put("notify_url", "https://www.saiyunxi.com/jsapi/jsapipays");// 此路径是微信服务器调用支付结果通知路径随意写
            String sign = WXPayUtil.generateSignature(paraMap, partnerkey);
            paraMap.put("sign", sign);
            String xml = WXPayUtil.mapToXml(paraMap);//将所有参数(map)转xml格式

            // 统一下单地址 https://api.mch.weixin.qq.com/pay/unifiedorder
            String unifiedorder_url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

            String xmlStr = HttpUtil.sendPost(unifiedorder_url, xml);//发送post请求"统一下单接口"返回预支付id:prepay_id
            System.out.println("xmlStr = " + xmlStr);
            //以下内容是返回前端页面的json数据
            String prepay_id = "";//预支付id
            if (xmlStr.indexOf("SUCCESS") != -1) {
                Map<String, String> map = WXPayUtil.xmlToMap(xmlStr);
                prepay_id = map.get("prepay_id");
            }
            Map<String, String> payMap = new HashMap<String, String>();
            payMap.put("appId", appid);
            payMap.put("timeStamp", PayCommonUtil.getCurrTime());
            payMap.put("nonceStr", WXPayUtil.generateNonceStr());
            payMap.put("signType", "MD5");
            payMap.put("package", "prepay_id=" + prepay_id);
            String paySign = WXPayUtil.generateSignature(payMap, partnerkey);
            payMap.put("paySign", paySign);
            return payMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}