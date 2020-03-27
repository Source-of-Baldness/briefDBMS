package com.pojo;

public class Database {
    private String name;//主数据文件名
    private String filename;//存放的路径
    private int size;//数据库文件及日志文件的大小
    private int tablesize;//每张表的大小
    private String user;//所属用户

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTablesize() {
        return tablesize;
    }

    public void setTablesize(int tablesize) {
        this.tablesize = tablesize;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}