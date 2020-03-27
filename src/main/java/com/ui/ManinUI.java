package com.ui;

import com.manager.data.UserRecord;
import com.manager.security.AccessControl;
import com.pojo.User;
import com.util.AnalysisUtil;
import com.util.FileUtil;
import com.util.SmallBigChange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class ManinUI {
    //定义全局变量
    public static User serveUser = new User();
    public static String currentDatabase = "master";//默认master数据库
    public static ArrayList<String> databaseNames = new ArrayList<String>();//当前用户下的所有数据库


    public static void main(String[] args) throws Exception {
        System.out.println("hello briefDBMS");
        AccessControl accessControl= new AccessControl();
        FileUtil fileUtil = new FileUtil();
        serveUser = null;
        while(serveUser==null) {
            serveUser = accessControl.ConnectServer();
        }

        while(true){
            Scanner input = new Scanner(System.in);
            UserRecord userRecord = new UserRecord();
            System.out.println("Current Database 'master' ");
            //读取该用户下的数据库
            userRecord.getUserDatabase();
            System.out.println(databaseNames);

            //输入
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String sql = br.readLine();
            //全部转化为大写
            SmallBigChange sctc = new SmallBigChange();
            sql = sctc.toBigchar(sql);
            //跳转语法解析工具，定位语法类型
            AnalysisUtil au= new AnalysisUtil();
            au.grammarPositon(sql);
        }
    }
}
