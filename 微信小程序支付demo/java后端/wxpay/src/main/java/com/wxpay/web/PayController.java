package com.wxpay.web;

import com.wxpay.config.IdWorker;
import com.wxpay.dao.WxPayDao;
import com.wxpay.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 *微信扫二维码支付
 */
@CrossOrigin
@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private WxPayDao wxPayDao;

    @Autowired
    private IdWorker idWorker;

    //生成二维码
    @RequestMapping("/createNative")
    public Map createNative(@RequestParam String body, @RequestParam String money, HttpServletRequest request) {
        long nextId = idWorker.nextId();
        Map map = wxPayDao.createNative(idWorker.nextId() + "", money,request);
        return map;
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(@RequestParam String out_trade_no) {
        Result result = null;
        int x = 0;
        while (true) {
            //调用查询接口
            Map payStatus = wxPayDao.queryPayStatus(out_trade_no);
            if (payStatus == null) {
                result = new Result(false, "支付失败");
                break;
            }
            if (payStatus.get("trade_state").equals("SUCCESS")) {
                result = new Result(true, "支付失败");
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒钟
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x >= 100) {
                result = new Result(false, "二维码超时");
                break;
            }
        }
        return result;
    }
}
