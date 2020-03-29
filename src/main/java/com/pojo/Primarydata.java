package com.pojo;

import java.security.PrivateKey;

public class Primarydata {
    private String tableName;//表名
    private String tablePath;//表的路径
    private Table alltable;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTablePath() {
        return tablePath;
    }

    public void setTablePath(String tablePath) {
        this.tablePath = tablePath;
    }

    public Table getAlltable() {
        return alltable;
    }

    public void setAlltable(Table alltable) {
        this.alltable = alltable;
    }
}
