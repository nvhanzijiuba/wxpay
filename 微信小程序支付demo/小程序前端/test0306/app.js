//app.js
App({
  onLaunch: function () {
    
    // 展示本地存储能力
    var logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 登录
    wx.login({
     
      success: res => {
        // 发送 res.code 到后台换取 openId, sessionKey, unionId
        // if (this.data.timer) {
          wx.login({
            success: function (res) {
              var that = this;
              if (res.code) {
                console.log("11=wwqwq4q3==" + res.code);
                wx.request({
                   url: "http://192.168.0.109:8011/jsapi/getuser/" + res.code,                  
                  // method:"POST",
                  header: {
                    "content-type": "application/json"
                  },          
                  success: function (res2) {
                    console.log("openid===="+res2.data);
                    if (res2.statusCode == 200) {
                      // pay(res2.data.openid);  
                      //调用支付方法，并把openid作为参数   
                      // that.pay(res2.data);
                      wx.request({
                        url: 'http://192.168.0.109:8011/jsapi/jsapipays/',
                        method: 'GET',
                        // data: { code: res2.data },
                        header: {
                          "content-type": "application/json"
                        },
                        success: (res2) => {
                          console.log(res2.data.message);
                          // if (res.data.code == 0) {          
                          wx.requestPayment({
                            timeStamp: res2.data.message.timeStamp,
                            nonceStr: res2.data.message.nonceStr,
                            package: res2.data.message.package,
                            signType: 'MD5',
                            paySign: res2.data.message.paySign,
                            success: function (res3) {
                              console.log(res3.data);
                            },
                            fail: (res3) => {
                              if (res3.errMsg == "requestPayment:fail cancel") {
                                console.log("111111失败！！！");
                                // wx.request({
                                //   url: base_url.base_url + 'tOrderController.do?listdh',
                                //   method: 'GET',
                                //   data: { id: that.data.orderId },
                                //   header: {
                                //     "content-type": "application/json"
                                //   },
                                //   success: (res) => {
                                //     console.log(JSON.stringify(res));
                                //     wx.showToast({
                                //       title: '支付取消',
                                //       icon: 'none',
                                //       duration: 1200
                                //     });
                                //     that.setData({
                                //       orderId: null
                                //     });
                                //   }
                                // })

                              } else {
                                wx.showToast({
                                  title: '支付失败',
                                  icon: 'none',
                                  duration: 1200
                                })
                              }
                              //支付失败
                              console.log(res);
                            }
                          })
                        }
                      }
                      )
                    } else {
                      wx.showToast({
                        title: '网路错误！',
                        icon: 'none'
                      })
                    }
                  }
                })
              }
            }
          })
        // } else {
        //   wx.showToast({
        //     title: '请选择时间及相对金额',
        //     icon: 'none'
        //   })

        // }
      }
    })
    // 获取用户信息
    wx.getSetting({
      success: res => {
        if (res.authSetting['scope.userInfo']) {
          // 已经授权，可以直接调用 getUserInfo 获取头像昵称，不会弹框
          wx.getUserInfo({
            success: res => {
              // 可以将 res 发送给后台解码出 unionId
              this.globalData.userInfo = res.userInfo

              // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
              // 所以此处加入 callback 以防止这种情况
              if (this.userInfoReadyCallback) {
                this.userInfoReadyCallback(res)
              }
            }
          })
        }
      },
      //支付方法
      // // pay:function(opid) {
      //   wx.request({
      //     url: '192.168.0.109::8011/jsapi/jsapipays?out_trade_no=12345678988888' + '&money=' + 1 + '&openid=' + opid,
      //     method: 'GET',
      //     // data: { id: that.data.orderId },
      //     header: {
      //       "content-type": "application/json"
      //     },
      //     success: (res2) => {
      //       console.log(res2.data);
      //       // if (res.data.code == 0) {          
      //       wx.requestPayment({
      //         timeStamp: res2.data.timeStamp,
      //         nonceStr: res2.data.nonceStr,
      //         package: res2.data.package,
      //         signType: 'MD5',
      //         paySign: res2.data.paySign,
      //         success: function (res3) {
      //           console.log(res.data);
      //         },
      //         fail: (res2) => {
      //           if (res.errMsg == "requestPayment:fail cancel") {
      //             console.log("111111失败！！！");
      //             // wx.request({
      //             //   url: base_url.base_url + 'tOrderController.do?listdh',
      //             //   method: 'GET',
      //             //   data: { id: that.data.orderId },
      //             //   header: {
      //             //     "content-type": "application/json"
      //             //   },
      //             //   success: (res) => {
      //             //     console.log(JSON.stringify(res));
      //             //     wx.showToast({
      //             //       title: '支付取消',
      //             //       icon: 'none',
      //             //       duration: 1200
      //             //     });
      //             //     that.setData({
      //             //       orderId: null
      //             //     });
      //             //   }
      //             // })

      //           } else {
      //             wx.showToast({
      //               title: '支付失败',
      //               icon: 'none',
      //               duration: 1200
      //             })
      //           }
      //           //支付失败
      //           console.log(res);
      //         }
      //       })
      //     }
      //   }
      //   )
      // }
    })

  },
  globalData: {
    userInfo: null
  }
})