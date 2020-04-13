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
import java.util.List;
import java.util.Scanner;

public class ManinUI {
    //定义全局变量
    public static User serveUser = new User();
    public static Database currentDatabase = new Database();//默认master数据库
    public static ArrayList<String> databaseNames = new ArrayList<String>();//当前用户下的所有数据库
    public static String UserPath = "";
    private ServerSocket serverSocket;
    public static int initSucceed = 0;

    public static void main(String[] args) throws Exception {
        //建立Socket通信
        SocketServiceImpl socketService = new SocketServiceImpl();
        System.out.println("正在建立通信,");
        socketService.socketConnection();
        socketService.sqlCommand();



        AnalysisUtil au= new AnalysisUtil();
        FileUtil fileUtil = new FileUtil();
        SmallBigChange sctc = new SmallBigChange();
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
            //返回数据库名称->客户机
            socketService.sqlResult(databaseNames.toString().replaceAll(" +", "").substring(1, (databaseNames.toString().replaceAll(" +", "").length() - 1)));
            socketService.sqlResult(currentDatabase.getName());
            int initNum = databaseNames.size();
            //返回表->客户机
            if(initSucceed==0) {
                for (int i = 0; i<initNum; i++) {
                    userRecord.getUserDatabase();
                    System.out.println(databaseNames.get(i));
                    String sql = "switch database " + databaseNames.get(i);
                    sql = sctc.toBigchar(sql);
                    au.grammarPositon(sql);
                    String allTableName = au.allTableName(currentDatabase.getFilename());
                    System.out.println("sd:"+allTableName);
                    if(allTableName.equals("[]"))
                        allTableName = "[null]";
                    socketService.sqlResult(allTableName.substring(1, (allTableName.length() - 1)));
                }
                au.grammarPositon("SWITCH DATABASE SYSTEM_CHANGE_MASTER_INIT_CLIENT_CONNECTING");
                initSucceed ++ ;
                continue;
            }





            //get SQL
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            //读取用户输入的sql语句,使用Socket通信
            //String sql = br.readLine();
            String sql = socketService.sqlCommand();
            //toBigChar
            sql = sctc.toBigchar(sql);
            //into Analysis ,turn on package analysis
            au.grammarPositon(sql);
        }
    }




}
