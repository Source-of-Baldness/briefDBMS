package com.Socket.impl;

import com.Socket.SocketService;

import java.net.*;
import java.io.*;

public class SocketServiceImpl implements SocketService {
	private ServerSocket server;
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;

	public SocketServiceImpl() {
	}

	public String sqlCommand (){
		String clientContent = "";
		socketConnection();
		clientContent = sqlConnect();
		sqlResult();
		sqlClose();
		socketClose();
		return clientContent;
	}

	@Override
	public void socketConnection() {
		try {
			server = new ServerSocket(1234);
			System.out.println("等待客户端(Client)建立通信....");
			//获得客户端连接
			client = server.accept();
			//获得客户端的IP和端口
			String remoteIP = client.getInetAddress().getHostAddress();
			int remotePort = client.getLocalPort();
			System.out.println("C# Client connected succeed. IP:" + remoteIP + ", Port: " + remotePort);


		}catch(Exception e){
			System.out.println(e.toString());
			out.println("与客户端连接发生异常。!");
		}
	}

	@Override
	public String sqlConnect() {
		try{
			String clientContent="接收client端数据";
			//获得 client 端的输入输出流，为进行交互做准备
			in = new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));
			out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(),"UTF-8"),true);
			//获得 client 端发送的数据
			clientContent = in.readLine();
			System.out.println("Client message is: " + clientContent);
			return clientContent;
		}catch(Exception e){
			System.out.println(e.toString());
			out.println("服务端传输数据发生异常");
		}
		return null;
	}

	@Override
	public void sqlResult() {
		// 向 client 端发送响应数据
		String serviceContent="Service端通信正常";
		out.println(serviceContent);
	}

	@Override
	public void sqlClose() {
		try{
			out.close();
			in.close();
		}catch(Exception e){
			System.out.println(e.toString());
			out.println("关闭数据传输连接失败");
		}
	}

	@Override
	public void socketClose() {
		try{
			server.close();
		}catch(Exception e){
			System.out.println(e.toString());
			out.println("关闭Socket服务失败");
		}
	}

}