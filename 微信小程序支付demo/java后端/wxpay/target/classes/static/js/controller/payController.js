app.controller('payController', function ($scope, $location, payService) {


    $scope.createNative = function () {
        payService.createNative("测试商品", "1").success(
            function (response) {

                //显示订单号和金额
                $scope.money = (response.total_fee / 100).toFixed(2);
                $scope.out_trade_no = response.out_trade_no;

                //生成二维码
                var qr = new QRious({
                    element: document.getElementById('qrious'),
                    size: 250,
                    value: response.code_url,
                    level: 'H'
                });

                queryPayStatus();//调用查询
            }
        );
    }

    //调用查询
    queryPayStatus = function () {
        payService.queryPayStatus($scope.out_trade_no).success(
            function (response) {
                if (response.success) {
                    location.href = "paysuccess.html#?money=" + $scope.money;
                } else {
                    if (response.message == '二维码超时') {
                        $scope.createNative();//重新生成二维码
                    } else {
                        location.href = "payfail.html";
                    }
                }
            }
        );
    }

    //获取金额
    $scope.getMoney = function () {
        return $location.search()['money'];
    }


    $scope.payJSapi = function () {
        // getUserInfo();
        payService.payJSAPI("666666", "1",  "oyFo75D5kwCXTFNYt02VTccbB5FE").success(
            function (response) {
                if (response.success) {
                    console.log(response.message)
                    // wx.chooseWXPay({
                    wx.requestPayment({
                        timeStamp: response.message.timeStamp,
                        nonceStr: response.message.nonceStr,
                        package: response.message.package,
                        signType: response.message.signType,
                        paySign: response.message.paySign,
                        success: function (a) {
                            alert(a);
                        },
                        'fail': function (res) {
                            alert(JSON.stringify(res));
                            console.log('支付失败');
                            return;
                        },
                        'complete': function (res) {
                            alert(JSON.stringify(res));
                            console.log('支付完成');
                        }
                    })
                } else {
                    alert("支付出现问题")
                }
            }
        )
    }

    // getUserInfo = function () {
    //     payService.getUserInfo("0236TvYx1j3vba0vq5Zx1OfqYx16TvYo").success(
    //         function (res) {
    //             $scope.openid = res.data;
    //             alert(res.data);
    //         }
    //     )
    // }
});