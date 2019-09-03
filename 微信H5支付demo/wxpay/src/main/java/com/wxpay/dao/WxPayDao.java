package com.wxpay.dao;

import com.wxpay.pojo.InfoDTO;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

public interface WxPayDao {

    /**
     * 生成微信支付二维码
     * @param out_trade_no 订单号
     * @param total_fee    金额
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee, HttpServletRequest request);

    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 微信支付退款
     */
    String weixinRefund(InfoDTO infoDTO);

    /**
     * 关闭订单
     */
    String weixinCloseorder(InfoDTO infoDTO);

    /**
     * 下载微信账单
     */
    void saveBill();

    /**
     * 微信公众号支付返回一个url地址
     */
    String weixinPayMobile(InfoDTO infoDTO);

    /**
     * H5支付 唤醒 微信APP 进行支付 申请入口：登录商户平台-->产品中心-->我的产品-->支付产品-->H5支付
     */
    String weixinPayH5(String ip,HttpServletRequest request);//InfoDTO infoDTO

    /**
     * 小程序支付
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    String weixinPayJSAPI(String out_trade_no, String total_fee, String openid);

    /**
     * 获取用户openid和用户tocken等登陆信息
     * @param code
     * @return
     */
    Map<String, String> getUserInfo(String code) throws IOException, ParseException, Exception;

    //微信小程序支付
    Map jsapi2(HttpServletRequest request, String openId);

}
