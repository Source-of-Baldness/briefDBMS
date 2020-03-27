package com.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    /*
     * 在网络上获取当前网络时间，不需要传入参数，时间格式为YYYY-MM-dd HH:mm
     * 直接调用即可返回一个Strig类型的日期时间
     */
    public static String getNetworkTime() {
        String webUrl="http://www.baidu.com";
        try {
            URL url=new URL(webUrl);
            URLConnection conn=url.openConnection();
            conn.connect();
            long dateL=conn.getDate();
            Date date=new Date(dateL);
            SimpleDateFormat dateFormat=new SimpleDateFormat("YYYY-MM-dd HH:mm");
            return dateFormat.format(date);
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return "";
    }
}
