package com.manager.data;

import com.pojo.Database;
import com.ui.ManinUI;
import com.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;

public class UserRecord {

    //获取用户下的所有数据库名
    public Database getUserDatabase() throws IOException {
        FileUtil fileUtil = new FileUtil();
        ArrayList<String> lines =new ArrayList<String>();
        lines = fileUtil.readLine("D:\\BRIEFDBMS\\account\\"+ ManinUI.serveUser.getId()+".txt");
        ManinUI.databaseNames.clear();
        for(String line:lines){
            String[] split_line=line.split("@@");
            ManinUI.databaseNames.add(split_line[2]);
        }

        return null;
    }

    //获取指定数据库的路径
    public String getDatabasePath() throws IOException {
        FileUtil fileUtil = new FileUtil();
        ArrayList<String> lines =new ArrayList<String>();
        lines = fileUtil.readLine("D:\\BRIEFDBMS\\account\\"+ ManinUI.serveUser.getId()+".txt");
        ManinUI.databaseNames.clear();
        for(String line:lines){
            String[] split_line=line.split("@@");
            if(ManinUI.currentDatabase.equals(split_line[2])){
                return split_line[1];
            }
        }
        return null;
    }
}
