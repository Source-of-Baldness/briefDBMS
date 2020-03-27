package com.manager.analysis;

import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Select {
    //传入sql语句，进行解析语法
    public void baseAnalysis(String sql){
        System.out.println("二次正则判断");
        //二次正则判断
        Pattern p = Pattern.compile("^[\\s]*SELECT[\\s]+(([A-Z][A-Z]*[\\s]*,[\\s]*)*([A-Z][A-Z]*)|\\*)[\\s]+FROM[\\s]+([A-Z][A-Z]*)([\\s]+WHERE[\\s]+([A-Z][A-Z]*=[^\\s]+[\\s]*(OR|AND)[\\s]*)*([A-Z][A-Z]*=[^\\s]+[\\s]*)[\\s]*)?$");
        Matcher m = p.matcher(sql);
        boolean result = m.matches();
        System.out.println(result);

    }
}
