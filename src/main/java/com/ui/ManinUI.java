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
        socketService.sqlCommand("接收client端数据");
        socketService.sqlResult("Service端通信正常");
        socketService.sqlClose();
        socketService.socketClose();



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
        serveUser = null;
        while(serveUser==null) {
            serveUser = accessControl.ConnectServer();
        }

        while(true){
            Scanner input = new Scanner(System.in);
            UserRecord userRecord = new UserRecord();
            System.out.println("Current Database '"+currentDatabase.getName()+"'");
            //读取该用户下的数据库
            userRecord.getUserDatabase();
            System.out.println(databaseNames);

            //get SQL
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String sql = br.readLine();
            //toBigChar
            SmallBigChange sctc = new SmallBigChange();
            sql = sctc.toBigchar(sql);
            //into Analysis ,turn on package analysis
            AnalysisUtil au= new AnalysisUtil();
            au.grammarPositon(sql);
        }
    }




}
