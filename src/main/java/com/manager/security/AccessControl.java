package com.manager.security;

import com.Socket.impl.SocketServiceImpl;
import com.pojo.User;

import java.util.Scanner;

public class AccessControl {
    public User ConnectServer(){
        SocketServiceImpl socketService = new SocketServiceImpl();
        User user = new User();
        Scanner input = new Scanner(System.in);
        System.out.println("连接到briefSQL Server 身份验证");
        System.out.println("用户名:");
        //String id = input.next();
        String id = socketService.sqlCommand();



        System.out.println("密码:");
        //String pwd = input.next();
        String pwd = socketService.sqlCommand();



        //如果账号密码正确,返回User类
        user.setId(id);
        user.setPwd(pwd);
        return user;
    }
}
