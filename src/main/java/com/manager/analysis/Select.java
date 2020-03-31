package com.manager.analysis;

import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.util.FileUtil;
import com.ui.ManinUI;
import com.util.AnalysisUtil;

public class Select {
    //传入sql语句，进行解析语法
    public void baseAnalysis(String sql){
        System.out.println("二次正则判断");
        //实例化工具类
        AnalysisUtil au=new AnalysisUtil();
        boolean result=false;
        //判断输入语句是否符合语法规则

        //1.无筛选条件（查找全表时）
        Pattern p=Pattern.compile("^[\\s]*SELECT[\\s]+(\\*)[\\s]+(FROM)[\\s]+[A-Z](\\w)*[\\s]*$");
        Matcher m=p.matcher(sql);
        result=m.matches();
        System.out.println(result);
        //上式为真 继续判断 判断库中是否含有该表
        String tableName=getSqlTableName(sql);
        String filePath=ManinUI.currentDatabase.getFilename();
        String databaseName=ManinUI.currentDatabase.getName();
        boolean tableBoolean=au.isHaveTheTable(tableName,databaseName,filePath);
        if(tableBoolean=true)//输入表名正确
        {
            System.out.println("正在查找");

        }
        else
        {
            System.out.println("此数据库不含该表！");
        }
    }

    //获取sql语句中的表名字段
    public String getSqlTableName(String sql){
        String tableName="",tn="";
        char t = 't';
        String[] str=new String[sql.length()];
        int i=0;
        for(i=sql.length()-1;i>=0;i--)
        {
            if(t!=' ')
            {
                t=sql.charAt(i);
                tableName+=t;//获得了一个倒序的表名
                //System.out.println(t);
            }
        }
        tn=tableName;
        tableName="";
        for(i=tn.length()-1;i>=0;i--)
        {
            t=tn.charAt(i);
            tableName+=t;
            //System.out.println(t);
        }
        return tableName;
    }


}
