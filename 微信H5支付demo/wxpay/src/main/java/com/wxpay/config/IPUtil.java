//package com.wxpay.config;
//
//
//import javax.servlet.http.HttpServletRequest;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
//public class IPUtil {
//
//    /**
//     * 获取用户实际ip
//     * @param request
//     * @return
//     */
//    public static String getIpAddr(HttpServletRequest request){
//        String ipAddress = request.getHeader("x-forwarded-for");
//        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("Proxy-Client-IP");
//        }
//        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getRemoteAddr();
//            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){
//                //根据网卡取本机配置的IP
//                InetAddress inet=null;
//                try {
//                    inet = InetAddress.getLocalHost();
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                }
//                ipAddress= inet.getHostAddress();
//            }
//        }
//        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
//        if(ipAddress!=null && ipAddress.length()>15){ //"***.***.***.***".length() = 15
//            if(ipAddress.indexOf(",")>0){
//                ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));
//            }
//        }
//        return ipAddress;
//    }
//
//    public static String getRemoteHost(javax.servlet.http.HttpServletRequest request){
//        String ip = request.getHeader("x-forwarded-for");
//        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
//            ip = request.getHeader("Proxy-Client-IP");
//        }
//        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
//            ip = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
//            ip = request.getRemoteAddr();
//        }
//        return ip.equals("0:0:0:0:0:0:0:1")?"127.0.0.1":ip;
//    }
//}