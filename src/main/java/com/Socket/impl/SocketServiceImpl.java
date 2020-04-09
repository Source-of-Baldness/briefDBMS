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
		}
	}

	@Override
	public String sqlConnect() {
		try{
			String serviceContent="Service端通信正常";
			String clientContent="接收client端数据";
			//获得 client 端的输入输出流，为进行交互做准备
			in = new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));
			//获得 client 端发送的数据
			clientContent = in.readLine();
			System.out.println("Client message is: " + clientContent);
			out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(),"UTF-8"),true);
			serviceContent="Service端通信正常";
			out.printf(serviceContent);
			return clientContent;
		}catch(Exception e){
			System.out.println(e.toString());
		}
		return null;
	}


	@Override
	public void sqlClose() {
		try{
			out.close();
			in.close();
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}

	@Override
	public void socketClose() {
		try{
			server.close();
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}

	@Override
	public void sqlResult(String result) {
		Socket client;
		String serverAddress = "127.0.0.2";
		int port = 1235;
		BufferedReader in;
		PrintWriter out;
		try {
			// 连接 server 端
			client = new Socket(serverAddress, port);
			// 为发送数据做准备
			in = new BufferedReader(new InputStreamReader(System.in));
			out = new PrintWriter(client.getOutputStream(), true);
			// 向 server 发送数据
			out.printf(result);
			// 接收来自 server 的响应数据
			in = new BufferedReader(new InputStreamReader(client
					.getInputStream()));
			System.out.println("客户端接收数据情况: " + in.readLine());

			// 关闭各个流
			in.close();
			out.close();
			client.close();

		} catch (Exception ignored) {

		}

	}

}