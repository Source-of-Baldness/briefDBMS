package com.manager.analysis;

import com.Socket.impl.SocketServiceImpl;
import com.manager.data.PrimaryRecord;
import com.pojo.Database;
import com.ui.ManinUI;
import com.util.FileUtil;
import com.util.TimeUtil;
import net.sf.json.JSONObject;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateDatabase {
    public void baseAnalysis(String sql) throws IOException {
        Database database = new Database();

        //二次验证
        Pattern p = Pattern.compile("^[\\s]*CREATE[\\s]+DATABASE[\\s]+([A-Z][A-Z]*)([\\s]+)?\\(([\\s]+)?([\\s]*(NAME=|FILENAME=|SIZE=|MAXSIZE=|FILEGROWTH=)(.*),*)*[\\s]*\\)[\\s]*$");
        Matcher m = p.matcher(sql);
        boolean result = m.matches();
        System.out.println("二次验证"+result);
        //绑定关键参数，并实例化数据库实体对象
        try {
            if(result)
               database = parameterSplit(sql);
            else{
                SocketServiceImpl socketService = new SocketServiceImpl();
                socketService.sqlResult("1");
                socketService.sqlResult("CREATE DATABASE ( 附近有语法错误; ");
            }

        }catch (Exception e){
            System.out.println("参数绑定错误，CREATE DATABASE ( 附近有语法错误; ");
        }
        //开始写入文件，这里假设路径没有错误，测试路径为F:/BRIEFDBMS/database
        PrimaryRecord primaryRecord = new PrimaryRecord();
        primaryRecord.writedPrimaryFile(database);
    }

    //关键参数定位
    public Database parameterSplit(String sql) throws Exception{
        Database database = new Database();
        int name = -1;
        int filename = -1;
        int size = -1;
        int filegrowth = -1;
        String split_sql[]=sql.split(",");
// begin
        for(String temp_split:split_sql){
            name=temp_split.indexOf("NAME");
            filename= temp_split.indexOf("FILENAME");
            size=temp_split.indexOf("SIZE");
            filegrowth=temp_split.indexOf("FILEGROWTH");
            //name
            if(name!=(-1)) {
                char temp[] = temp_split.toCharArray();
                if (temp[(name - 1)] != 'E') {
                    //截取
                    database.setName(temp_split.substring(temp_split.indexOf('=') + 1));
                    System.out.println("name:" + database.getName());
                    //建立相同库的判断
                    for(String databaseName:ManinUI.databaseNames){
                        if(database.getName().equals(databaseName)){
                            SocketServiceImpl socketService = new SocketServiceImpl();
                            socketService.sqlResult("1");
                            socketService.sqlResult("数据库建立失败，已经存在 '"+database.getName()+"' 数据库。");
                            System.out.println("数据库建立失败，已经存在 '"+database.getName()+"' 数据库。");
                        }
                    }
                }
            }
            //filename
            if(filename!=(-1)) {
                char temp[] = temp_split.toCharArray();
                    //截取
                    //路径是否正确判断
                    database.setFilename(temp_split.substring(temp_split.indexOf('=') + 1)+"/"+database.getName());
                    System.out.println("filename:" + database.getFilename());
            }
            //size
            if(size!=(-1)) {
                char temp[] = temp_split.toCharArray();
                //截取
                database.setSize(Integer.parseInt(temp_split.substring(temp_split.indexOf('=') + 1)));
                System.out.println("size:" + database.getSize());
            }
            //tablesize
            if(filegrowth!=(-1)) {
                char temp[] = temp_split.toCharArray();
                //截取
                database.setTablesize(Integer.parseInt(temp_split.substring(temp_split.indexOf('=') + 1)));
                System.out.println("filegrowth:" + database.getTablesize());
            }
        }
        //end
        database.setUser(ManinUI.serveUser.getId());
        //参数完整性检验
        int flag=0;
        if(database.getName()!=null){
            flag++;
            if(database.getTablesize()!=0){
                flag++;
                if(database.getSize()!=0){
                    flag++;
                    if(database.getFilename()!=null){
                        flag++;
                    }
                }
            }
        }
        System.out.println("checked");
        if(flag!=4) {
            System.out.println("参数绑定错误，CREATE DATABASE ( 附近有语法错误; ");
            SocketServiceImpl socketService = new SocketServiceImpl();
            socketService.sqlResult("1");
            socketService.sqlResult("参数绑定错误，CREATE DATABASE ( 附近有语法错误; ");
        }
        else
            return database;
        return null;
    }

    //相同库判断

}
