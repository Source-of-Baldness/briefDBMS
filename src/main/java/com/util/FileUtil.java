package com.util;

import java.io.*;

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


}
