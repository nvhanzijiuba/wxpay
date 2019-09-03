package com.wxpay.web;

import com.wxpay.config.HttpRequestor;
import com.wxpay.dao.WxPayDao;
import com.wxpay.pojo.Result;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/jsapi")
public class JSAPIController {

    @Autowired
    WxPayDao wxPayDao;

    @RequestMapping("/jsapipays")
    public Result jsapipays(HttpServletRequest request,@RequestParam String openId) {
        Map jsapi = wxPayDao.jsapi2(request, openId);
        return new Result(true, jsapi);
    }

    @GetMapping("/getuser/{code}")
    public String getOpenId(@PathVariable String code) throws Exception {
        Map<String, String> userInfo = wxPayDao.getUserInfo(code);
        return userInfo.get("openid");
    }

    @GetMapping("/test/{code}")
    public JSONObject getOpenIda(@PathVariable String code) throws Exception {
        String appid = "wxa0e260c49adec8bd";
        String secret = "315ac666867c12b1efb29bb10a6c91be";
        String requestUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid + "&secret=" + secret + "&code=" + code + "&grant_type=authorization_code";
        //第一次请求 获取access_token 和 openid
        String oppid = new HttpRequestor().doGet(requestUrl);
        JSONObject oppidObj = JSONObject.fromObject(oppid);
        String access_token = (String) oppidObj.get("access_token");
        String openid = (String) oppidObj.get("openid");
        String requestUrl2 = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token + "&openid=" + openid + "&lang=zh_CN";
        String userInfoStr = new HttpRequestor().doGet(requestUrl2);
        JSONObject wxUserInfo = JSONObject.fromObject(userInfoStr);
        System.out.println("wxUserInfo = " + wxUserInfo);
        return oppidObj;
    }
}
