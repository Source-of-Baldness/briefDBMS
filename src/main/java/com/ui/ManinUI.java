package com.ui;

import com.Socket.SocketService;
import com.Socket.impl.SocketServiceImpl;
import com.manager.data.UserRecord;
import com.manager.security.AccessControl;
import com.pojo.Database;
import com.pojo.User;
import com.util.AnalysisUtil;
import com.util.FileUtil;
import com.util.SmallBigChange;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;

public class ManinUI {
    //定义全局变量
    public static User serveUser = new User();
    public static Database currentDatabase = new Database();//默认master数据库
    public static ArrayList<String> databaseNames = new ArrayList<String>();//当前用户下的所有数据库
    public static String UserPath = "";
    private ServerSocket serverSocket;

    public static void main(String[] args) throws Exception {
        //建立Socket通信
        SocketServiceImpl socketService = new SocketServiceImpl();
        System.out.println("正在建立通信,");
        socketService.socketConnection();
        socketService.sqlCommand();


        FileUtil fileUtil = new FileUtil();
        System.out.println("<----hello briefDBMS---->");
        System.out.println("正在初始化系统文件.....");
        if(fileUtil.init_SYSTEM_filePath())
            System.out.println("briefDBMS 就绪");
        else{
            System.out.println("用户权限设置出现未知错误");
            System.exit(-1);
        }

        currentDatabase.setName("master");
        AccessControl accessControl= new AccessControl();
        serveUser.setId(null);serveUser.setPwd(null);
        while(serveUser.getId()==null || serveUser.getId().equals("")) {
            System.out.println("未连接到Brief SQL 服务");
            serveUser = accessControl.ConnectServer();
        }

        //返回客户机连接成功提示
        socketService.sqlResult("true");

        while(true){
            Scanner input = new Scanner(System.in);
            UserRecord userRecord = new UserRecord();
            System.out.println("Current Database '"+currentDatabase.getName()+"'");
            //读取该用户下的数据库
            userRecord.getUserDatabase();
            System.out.println(databaseNames);

            //get SQL
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            //读取用户输入的sql语句
            //String sql = br.readLine();
            String sql = socketService.sqlCommand();

            //toBigChar
            SmallBigChange sctc = new SmallBigChange();
            sql = sctc.toBigchar(sql);
            //into Analysis ,turn on package analysis
            AnalysisUtil au= new AnalysisUtil();
            au.grammarPositon(sql);
        }
    }




}
