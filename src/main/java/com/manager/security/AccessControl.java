package com.manager.security;

import com.pojo.User;

import java.util.Scanner;

public class AccessControl {
    public User ConnectServer(){
        User user = new User();
        Scanner input = new Scanner(System.in);
        System.out.println("连接到briefSQL Server 身份验证");
        System.out.println("用户名:");
        String id = input.next();
        System.out.println("密码:");
        String pwd = input.next();
        //如果账号密码正确,返回User类
        user.setId(id);
        user.setPwd(pwd);
        return user;
    }
}
