package com.manager.data;

import com.pojo.Database;
import com.pojo.Primarydata;
import com.sun.tools.javac.Main;
import com.ui.ManinUI;
import com.util.FileUtil;
import com.util.TimeUtil;
import net.sf.json.JSONObject;

import java.io.IOException;

public class PrimaryRecord {

    public void writedPrimaryFile(Database database) throws IOException {
        //开始写入文件
        FileUtil fileUtil = new FileUtil();
        //创建主数据文件
        if(fileUtil.createFile(database.getName()+".txt",database.getFilename()+ "\\" + database.getName()))
            System.out.println("主数据文件 succeed");
        //创建日志文件
        if(fileUtil.createFile(database.getName()+"_LOG.txt",database.getFilename()+ "\\" + database.getName()))
            System.out.println("日志文件 succeed");
        //查找这个用户是否有这个库 skip skip skip skip skip skip skip skip skip skip skip skip
        //skip
        //写入主数据文件信息,讲database pojo对象封装为json
        JSONObject json = JSONObject.fromObject(database);
        String w_data = json.toString();
        System.out.println(w_data);
        if(fileUtil.writeToFile(w_data,database.getFilename() + "\\"  + database.getName()+".txt"))
            System.out.println("主数据文件 writed");
        //写入日志文件信息
        String webTime="Database creation time:"+ TimeUtil.getNetworkTime();
        if(fileUtil.writeToFile(webTime,database.getFilename() + "\\" +  database.getName()+"_LOG.txt"))
            System.out.println("日志文件 writed");
        //写入用户文件
        if(fileUtil.writeToFile("SYS_DATABASE_NAME_UNIQUE@@"+database.getFilename()+"@@"+database.getName(),"F:\\BRIEFDBMS\\account\\"+ ManinUI.serveUser.getId() +".txt"))
            System.out.println("用户文件 writed");
    }

    //表结构写入主数据文件
    public boolean tableToPrimary(Primarydata primarydata) throws IOException {
        FileUtil fileUtil = new FileUtil();
        //转为JSON
        JSONObject json = JSONObject.fromObject(primarydata);
        String w_data = json.toString();
        if(fileUtil.writeToFile(w_data,primarydata.getTablePath()+"/"+ManinUI.currentDatabase+".txt"))
            return true;
        return false;
    }
}
