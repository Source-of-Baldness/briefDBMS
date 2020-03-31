package com.util;

import com.alibaba.fastjson.JSON;
import com.manager.analysis.*;
import com.util.FileUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalysisUtil {

    //语法解析工具，会自动识别增删改查操作，返回到指定的查询管理器类
    public void grammarPositon(String sql) throws Exception {
        System.out.println("注入的SQL语句："+sql);
        //初始化对象
        Select select = new Select();
        Update update = new Update();
        Delete delete = new Delete();
        Insert insert = new Insert();
        CreateDatabase createDatabase = new CreateDatabase();
        CreateTable createTable = new CreateTable();
        SwitchDatabase switchDatabase = new SwitchDatabase();
        //注入正则表达式
        Pattern p = Pattern.compile("^[\\s]*(INSERT|SELECT|ALTER|DELETE|UPDATE|CREATE DATABASE|CREATE TABLE|SWITCH DATABASE)[\\s](.|\\n)+$");
        Matcher m = p.matcher(sql);
        boolean result = m.matches();
        System.out.println(result);
        if (result){
            String split_sql[]=sql.split(" ");
            for(int i=0;i<split_sql.length;i++){
                if(split_sql[i]!=" ") //检测到第一个数组不会空，定位到指定语法解析器中
                {
                    if("SELECT".equals(split_sql[i]))
                        select.baseAnalysis(sql);
                    if("UPDATE".equals(split_sql[i]))
                        update.baseAnalysis(sql);
                    if("DELETE".equals(split_sql[i]))
                        delete.baseAnalysis(sql);
                    if("INSERT".equals(split_sql[i]))
                        insert.baseAnalysis(sql);
                    if("CREATE".equals(split_sql[i])){
                        if(split_sql[(i+1)].equals("DATABASE")){
                            createDatabase.baseAnalysis(sql);
                        }
                        if(split_sql[(i+1)].equals("TABLE")){
                            createTable.baseAnalysis(sql);
                        }
                    }
                    if("SWITCH".equals(split_sql[i])){
                        switchDatabase.baseAnalysis(sql);
                    }
                }
                break;
            }
        }
    }


    //限定符判断，建立限定符库
    public boolean qualifier(String sql){
        return false;
    }

    //判断数据库中是否含有一张表
    public boolean isHaveTheTable(String tableName,String databaseName,String filePath)
    {
        //获取目录下所有文件名
        tableName = tableName + ".txt";
        filePath = filePath+"/TABLE";
        FileUtil fu=new FileUtil();
        ArrayList<String> allfile=fu.getDirName(filePath);
        String allfiletext= JSON.toJSONString(allfile);
        Pattern p=Pattern.compile(tableName);
        Matcher m=p.matcher(allfiletext);
        boolean result;
        result=m.matches();
        return  result;
    }

    //读取表中数据 返回全表
//    public void getAllTableData(String tableName,String filePath)
//    {
//        FileUtil fu=new FileUtil();
//        //拼接完整路径
//        String file=filePath+"\\TABLE\\"+tableName+"\\.txt";
//
//    }
}
