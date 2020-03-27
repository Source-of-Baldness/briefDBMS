package com.manager.analysis;

import com.pojo.Table;
import com.ui.ManinUI;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwitchDatabase {
    public void baseAnalysis(String sql){
        System.out.println(sql);
        Pattern p = Pattern.compile("^[\\s]*SWITCH[\\s]DATABASE[\\s]+[\\w]+[\\s]*$");
        Matcher m = p.matcher(sql);
        boolean result = m.matches();
        //解析切换的数据库名称
        if(result){
            if(parameterSplit(sql)){
                System.out.println("命令成功完成。");
                System.out.println("当前数据库为:"+ManinUI.currentDatabase);

            }else{
                System.out.println("SWITCH 附近有语法错误。");
            }
        }else{
            System.out.println("切换的数据库不存在。");

        }
    }


    //解析参数
    public boolean parameterSplit(String sql){
        String[] dataName= sql.split("\\s+");
        int isExist=ManinUI.databaseNames.indexOf(dataName[(dataName.length-1)]);
        if(isExist!=(-1)){
            ManinUI.currentDatabase = dataName[(dataName.length-1)];
            return true;
        }
        ManinUI.currentDatabase = "master";
        return false;
    }
}
