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

    //查找txt中的数据内容信息 path为全局变量的路径加上\table\表名.txt
    public ArrayList<String> getAllDataInfo(String path) throws IOException {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            ArrayList<String> arrayList = new ArrayList<String>();//存放读取的数据
            String temp = "",temp1="";// 用于临时保存每次读取的内容

            while (temp != null && br.readLine()!=null)
            {
                temp = br.readLine();
                boolean result = false;
                //先判断SYS_First和SYS_END是否为0 为真表中无数据直接返回空
                if (temp != null && temp.contains("SYS_First")) {
                    System.out.println("第一次匹配" + temp);
                    Pattern p = Pattern.compile("^[\\s]*SYS_First@@0$");
                    Matcher m = p.matcher(temp);
                    result = m.matches();
                    if (result)//上式为真 继续匹配下一行
                    {
                        while (temp != null) {
                            result = false;//重置标识符
                            temp = br.readLine();
                            System.out.println("第二次匹配" + temp);
                            p = Pattern.compile("^[\\s]*SYS_End@@0$");
                            m = p.matcher(temp);
                            result = m.matches();
                            if (result)
                                return arrayList;//上两个字段均为0表中数据为空 直接返回空数组
                            else {
                                System.out.println("表文件出错！");
                                System.exit(-1);
                            }
                        }
                    }
                    else
                    {
                        while(temp!=null)
                        {
                            result=false;
                            temp1=temp;
                            temp = br.readLine();
                            System.out.println("第二次匹配temp1=" + temp1+",temp="+temp);

                            //判断first和end的数字是否相等 相等为空表返回空数组
                            System.out.println("非零表匹配正则"+"^[\\s]*"+temp1+"$");
                                p = Pattern.compile("^[\\s]*"+temp1+"$");
                            m = p.matcher(temp);
                            result = m.matches();
                            if(result)
                            {
                                //读取行数 count移动BufferedReader用
                                int line=temp1.charAt(temp1.length()-1),count=0;
                                System.out.println("行数"+line);
                                //定位到SYS_TITLE_DATA_BEGIN读取数据
                                if (temp!=null && temp.contains("SYS_TITLE_DATA_BEGIN"))
                                {
                                    result=false;
                                    temp=br.readLine();
                                    if(temp!=null && count!=line)
                                    {
                                        temp=br.readLine();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return arrayList;
    }
}
