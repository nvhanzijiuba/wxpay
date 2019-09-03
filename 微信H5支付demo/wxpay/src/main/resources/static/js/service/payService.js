app.service('payService', function ($http) {
    //本地支付

    this.createNative = function (body, money) {
        return $http.get('pay/createNative?body=' + body + "&money=" + money);
    }

    //查询支付状态
    this.queryPayStatus = function (out_trade_no) {
        return $http.get('pay/queryPayStatus?out_trade_no=' + out_trade_no);
    }

    this.payJSAPI = function (out_trade_no, money,openid) {
        return $http.get('jsapi/jsapipays?out_trade_no=' + out_trade_no + '&money=' + money+'&openid='+openid);
    }

    this.getUserInfo = function (code) {
        return $http.get("jsapi/getuser?" + code);
    }

    this.h5Pay=function (ip) {
        return $http.get("h5/h5pay?ip="+ip);
    }
});