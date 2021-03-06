package com.util;

import com.alibaba.fastjson.JSON;
import com.ui.ManinUI;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    // 读取指定某一行的文本
    //传入参数filepath精确到文件名 如：（F:/DEMO/123.txt，10）获取第十行内容
    public String getCertainLineOfTxt(String filePath, int lineNumber){
        FileReader fr = null;
        LineNumberReader reader = null;
        String txt = "";

        try{
            File file = new File(filePath);
            fr = new FileReader(file);
            reader = new LineNumberReader(fr);

            int lines = 0;

            while(txt != null){
                lines ++;

                txt = reader.readLine(); // Read a line of text.

                if(lines == lineNumber){
                    //System.out.println( "txt: " + txt + " lines = " + lines );
                    return txt;
                }
            }
            return txt;
        }catch(Exception e){
            e.printStackTrace();

            return txt;
        }finally{
            try{
                reader.close();
            }catch(IOException e){
                e.printStackTrace();
            }

            try{
                fr.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //读取指定行数范围内的数据，按行读取
    //传入参数filepath精确到文件名 如：（F:/DEMO/123.txt，10，20）从第10行开始读到第20行，包括10行，包括20行
    //返回字符的Array类型，若lineEnd为-1，则读取到文本最后一行如，（F:/DEMO/123.txt，10，-1）
    public ArrayList<String> getlLimitsLineOfTxt(String filePath, int lineBegin, int lineEnd){
        if(lineEnd==(-1)){
            lineEnd= Integer.MAX_VALUE;
        }
        FileReader fr = null;
        LineNumberReader reader = null;
        String txt = "";
        ArrayList<String> lines =new ArrayList<String>();

        try{
            File file = new File(filePath);
            fr = new FileReader(file);
            reader = new LineNumberReader(fr);

            int lines_flag = 0;

            while(txt != null){
                lines_flag ++;

                txt = reader.readLine(); // Read a line of text.

                if(lines_flag <lineEnd && lines_flag>=lineBegin && txt!=null){
                    lines.add(txt);
                }
            }
            return lines;
        }catch(Exception e){
            e.printStackTrace();
            return lines;
        }finally{
            try{
                reader.close();
            }catch(IOException e){
                e.printStackTrace();
            }

            try{
                fr.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //指定文本位置读取表格数据至指定文本信息处
    //该方法与范围查找方法类似，详见上个方法，设定开始范围
    //例如从 SYS_TITLE_RECORD_BEGIN 处 查找至 SYS_TITLE_RECORD_END处
    public ArrayList<String> getlContentLineOfTxt(String filePath, String lineBeginContent, String lineEndContent){
        FileReader fr = null;
        LineNumberReader reader = null;
        String txt = "";
        ArrayList<String> lines =new ArrayList<String>();

        try{
            File file = new File(filePath);
            fr = new FileReader(file);
            reader = new LineNumberReader(fr);

            int lines_flag = 0;
            int line_begin_flag=0;

            while(txt != null ){
                txt = reader.readLine(); // Read a line of text.

                if(lineEndContent.contains(txt))
                    return lines;
                lines_flag ++;

                if(line_begin_flag>=1){
                    //System.out.println( "txt: " + txt + " lines = " + lines );
                    lines.add(txt);
                }

                if(lineBeginContent.contains(txt)){
                    line_begin_flag ++;
                }

            }
            return lines;
        }catch(Exception e){
            e.printStackTrace();
            return lines;
        }finally{
            try{
                reader.close();
            }catch(IOException e){
                e.printStackTrace();
            }

            try{
                fr.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //指定位置实现文本替换
    public boolean replaceLineOfTxt(String filePath,int line_index,String replaceContent) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<String> replaced = new ArrayList<>();
        int lines_flag = 1;

        for (String line : lines) {
            if (lines_flag == line_index) {
                replaced.add(replaceContent);
            } else {
                replaced.add(line);
            }
            lines_flag++;
        }
        Files.write(Paths.get(filePath), replaced);
        return true;
    }

    //初始化系统文件，默认在D盘
    public boolean init_SYSTEM_filePath(){
        //建立用户文件
        if(createFile("sa.txt","D:/BRIEFDBMS/ACCOUNT")){
            ManinUI.UserPath= "D:/BRIEFDBMS/ACCOUNT";
            return true;
        }
        else {
            if(createFile("sa.txt","./BRIEFDBMS/ACCOUNT")){
                ManinUI.UserPath= "./BRIEFDBMS/ACCOUNT";
                return true;
            }
        }
            return false;
    }



}
