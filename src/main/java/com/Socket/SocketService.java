package com.Socket;

public interface SocketService {
    public void socketConnection();
    public void  sqlCommand(String clientContent);
    public void sqlResult(String serviceContent);
    public void sqlClose();
    public void socketClose();
}
