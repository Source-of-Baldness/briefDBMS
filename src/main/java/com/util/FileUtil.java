package com.util;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {

    //给定文件名和指定路径，创建文件夹和文件,若文件不存在会自动创建，
    // 如(123.txt,F:\DEMO)
    //会自动在F:\DEMO\ 下创建 123.txt文件
    public boolean createFile(String filename,String filepath){
        File file = new File(filepath);
        //如果文件夹不存在  就创建一个空的文件夹
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(filepath, filename);
        //如果文件不存在  就创建一个空的文件
        if (!file2.exists()) {
            try {
                file2.createNewFile();
            } catch (IOException e) {
                System.out.println("创建数据库失败；失败原因：找不到指定路径");
                return false;
            }
        }
        return true;
    }

    //给定文件位置和字符串，写入文件，追加写入，在33行增加了换行，每次增加一次会自动换行
    public boolean writeToFile(String conent,String file) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));
            out.write(conent+"\r\n");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    //获取一个目录下的所有的文件或文件夹名称
    public ArrayList<String> getDirName(String path){
        File dirFile = new File(path);
        ArrayList<String> databaseNames = new ArrayList<String>();
        if (dirFile.exists()) {

            File[] files = dirFile.listFiles();
            if (files != null) {
                for (File fileChildDir : files) {
                    //输出文件名或者文件夹名
                    databaseNames.add(fileChildDir.getName());
                    System.out.println(fileChildDir.getName());
                    if (fileChildDir.isDirectory()) {
                        //System.out.println("");
                        //通过递归的方式,可以把目录中的所有文件全部遍历出来
                        //getDirName(fileChildDir.getAbsolutePath());
                    }
//                    if (fileChildDir.isFile()) {
//                        System.out.println(" :  此为文件名");
//                    }
                }
            }
        }else{
            System.out.println("你想查找的文件不存在");
        }
        return databaseNames;
    }

//按行读取txt内容
    public ArrayList<String> readLine(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = null;
        ArrayList<String> lines =new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        br.close();
        return lines;
    }

    //判断数据库中是否含有一张表
    public boolean isHaveTheTable(String tableName,String databaseName,String filePath)
    {
        //获取目录下所有文件名
        ArrayList<String> allfile=getDirName(filePath);
        String allfiletext= JSON.toJSONString(allfile);
        Pattern p=Pattern.compile(tableName);
        Matcher m=p.matcher(allfiletext);
        boolean result;
        result=m.matches();
        return  result;
    }
}
