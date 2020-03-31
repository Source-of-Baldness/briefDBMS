package com.manager.analysis;

import com.alibaba.fastjson.JSON;
import com.manager.data.UserRecord;
import com.pojo.Database;
import com.ui.ManinUI;
import com.util.FileUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwitchDatabase {
    public void baseAnalysis(String sql) throws IOException {
        System.out.println(sql);
        Pattern p = Pattern.compile("^[\\s]*SWITCH[\\s]DATABASE[\\s]+[\\w]+[\\s]*$");
        Matcher m = p.matcher(sql);
        boolean result = m.matches();
        //解析切换的数据库名称
        if(result){
            if(parameterSplit(sql)){
                System.out.println("命令成功完成。");
                //封装database
                fillDatabase();
                System.out.println("当前数据库为:"+ManinUI.currentDatabase.getName());

            }else{
                System.out.println("切换的数据库不存在。");
            }
        }else{
            System.out.println("SWITCH 附近有语法错误。");
        }
    }


    //解析参数
    public boolean parameterSplit(String sql){
        String[] dataName= sql.split("\\s+");
        int isExist=ManinUI.databaseNames.indexOf(dataName[(dataName.length-1)]);
        if(isExist!=(-1)){
            ManinUI.currentDatabase.setName(dataName[(dataName.length-1)]) ;
            return true;
        }
        ManinUI.currentDatabase.setName("master");
        return false;
    }

    //封装database
    public void fillDatabase() throws IOException {
        UserRecord userRecord = new UserRecord();
        String filename = userRecord.getDatabasePath();
        //读取内容
        FileUtil fileUtil = new FileUtil();
        ArrayList<String> lines=fileUtil.readLine(filename+"/"+ManinUI.currentDatabase.getName()+".txt");
        Database database = JSON.parseObject(lines.get(0), Database.class); //反序列化
        ManinUI.currentDatabase = database;
    }
}
