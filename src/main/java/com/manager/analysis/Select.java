package com.manager.analysis;

import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Select {
    //传入sql语句，进行解析语法
    public void baseAnalysis(String sql){
        System.out.println("二次正则判断");
        //二次正则判断
//        Pattern p = Pattern.compile("^[\\s]*SELECT[\\s]+(([A-Z][A-Z]*[\\s]*,[\\s]*)*([A-Z][A-Z]*)|\\*)[\\s]+FROM[\\s]+([A-Z][A-Z]*)([\\s]+WHERE[\\s]+([A-Z][A-Z]*=[^\\s]+[\\s]*(OR|AND)[\\s]*)*([A-Z][A-Z]*=[^\\s]+[\\s]*)[\\s]*)?$");
//        Matcher m = p.matcher(sql);
        //boolean result = m.matches();
//        System.out.println(result);
        boolean result=false;
        //判断输入语句是否符合语法规则
        //1.无筛选条件（查找全表时）判断是否含关键字
        Pattern p=Pattern.compile("(select+\\s){2,}|(from+\\s){2,}|(where+\\s){2,}");
        Matcher m=p.matcher(sql);
        result=m.matches();
        System.out.println(result);
        //2.有筛选条件判断是否含关键字

    }
}
