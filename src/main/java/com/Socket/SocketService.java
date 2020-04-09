package com.Socket;

public interface SocketService {
    public String sqlCommand (); //服务端Sql通信整合
    public void sqlResult(String result); //返回需要输出至客户端的数据通信整合
    public void socketConnection(); //建立socket连接
    public String sqlConnect(); //数据交互准备
    public void sqlClose();     //关闭数据交互
    public void socketClose();  //关闭socket连接
}
