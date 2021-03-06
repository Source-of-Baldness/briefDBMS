package com.util;

import com.Socket.impl.SocketServiceImpl;
import com.alibaba.fastjson.JSON;
import com.manager.analysis.*;
import com.pojo.Database;
import com.pojo.Primarydata;
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
        RefreshDatabase refreshDatabase = new RefreshDatabase();
        BreakConnect breakConnect = new BreakConnect();
        //注入正则表达式
        Pattern p = Pattern.compile("^[\\s]*(INSERT|SELECT|ALTER|DELETE|UPDATE|CREATE DATABASE|CREATE TABLE|SWITCH DATABASE|REFRESH DATABASE SYSTEM|BREAK CONNECT SYSTEM)[\\s](.|\\n)+$");
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
                    if("REFRESH".equals(split_sql[i])){
                        refreshDatabase.baseAnalysis(sql);
                    }
                    if("BREAK".equals(split_sql[i])){
                        breakConnect.baseAnalysis(sql);
                    }
                }
                break;
            }
        }else{
            SocketServiceImpl socketService = new SocketServiceImpl();
            socketService.sqlResult("1");
            socketService.sqlResult("SQL 命令出现语法错误。");
        }
    }


    //限定符判断，建立限定符库
    public boolean qualifier(String sql){
        return false;
    }

    //判断数据库中是否含有一张表
    //传入的参数 tableName为表名，filePath为当前数据库的路径 直接调用全局变量即可传入
    public boolean isHaveTheTable(String tableName,String filePath)
    {
        //获取目录下所有文件名
        tableName = tableName + ".txt";
        filePath = filePath+"/TABLE";
        FileUtil fu=new FileUtil();
        ArrayList<String> allfile=fu.getDirName(filePath);
        String allfiletext= JSON.toJSONString(allfile);
        if(allfiletext.indexOf(tableName)!=(-1))
            return true;
        else
            return false;
    }

    //获取指定数据库下的表名
    public String allTableName(String filePath)
    {
        //获取目录下所有文件名
        filePath = filePath+"/TABLE";
        FileUtil fu=new FileUtil();
        ArrayList<String> allfile=fu.getDirName(filePath);
        String allfiletext= allfile.toString();
        return allfiletext;
    }

    //获取指定表名的表结构
    //传入参数getTableStruct(tableName,ManinUI.currentDatabase.getFilename(),ManinUI.currentDatabase.getName());
    //只需要传入表名，其他两个可以调用全局变量，若想指定其他数据库，则调用全局变量中的databaseNames
    public Primarydata getTableStruct(String tableName,String filePath,String databaseName) throws IOException {
        //传入参数封装,定位到主数据文件
        filePath = filePath+"/"+databaseName+".txt";
        //按行读取txt内容
        FileUtil fileUtil= new FileUtil();
        ArrayList<String> lines=fileUtil.getlLimitsLineOfTxt(filePath,2,-1);
        for(String line:lines){
            Primarydata primarydata = JSON.parseObject(line, Primarydata.class); //反序列化
            System.out.println(primarydata.getTableName());
            if(primarydata.getTableName().equals(tableName))
                return primarydata;
        }
        return null;
    }

    //获取SYS_First@@****
    public int getSYS_First(Primarydata primarydata){
        FileUtil fileUtil = new FileUtil();
        String SYS_First_String = fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",5);
        String[] SYS_First = SYS_First_String.split("@@");
        return Integer.parseInt(SYS_First[1]);
    }
    //获取SYS_End@@****
    public int getSYS_End(Primarydata primarydata){
        FileUtil fileUtil = new FileUtil();
        String SYS_End_String = fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",6);
        String[] SYS_End = SYS_End_String.split("@@");
        return Integer.parseInt(SYS_End[1]);
    }
    //获取SYS_RecordSpace@@****
    public int getSYS_RecordSpace(Primarydata primarydata){
        FileUtil fileUtil = new FileUtil();
        String SYS_RecordSpace_String = fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",3);
        String[] SYS_RecordSpace = SYS_RecordSpace_String.split("@@");
        return Integer.parseInt(SYS_RecordSpace[1]);
    }


}
