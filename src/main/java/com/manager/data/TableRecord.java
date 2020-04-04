package com.manager.data;

import com.pojo.Primarydata;
import com.pojo.Table;
import com.ui.ManinUI;
import com.util.FileUtil;
import org.apache.commons.lang.StringUtils;

import javax.swing.text.TabExpander;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableRecord {


    //基本表文件格式写入表文件
    public boolean writeBase_Table(Table table,Primarydata primarydata) throws IOException {
        //获取可以存放的数据条数
        int formatInfoSize = 243;
        long dataLines = calculateDataLines(table,formatInfoSize);
        //开始封装txt文件
/*        SYS_TITLE_FORMAT_BEGIN
          SYS_FormatInfo@@2147483647
          SYS_RecordSpace@@2147483647
          SYS_AllRecoeds@@2147483647
          SYS_First@@0
          SYS_End@@0
          SYS_TITLE_FORMAT_END
          SYS_TITLE_RECORD_BEGIN
          SYS_TITLE_RECORD_END
          SYS_TITLE_DATA_BEGIN
          SYS_TITLE_DATA_END
*/
        String formatInfo = "SYS_TITLE_FORMAT_BEGIN\r\n" +
                "SYS_FormatInfo@@"+formatInfoSize+"\r\n" +
                "SYS_RecordSpace@@"+dataLines+"\r\n" +
                "SYS_AllRecoeds@@0\r\n" +
                "SYS_First@@0\r\n" +
                "SYS_End@@0\r\n" +
                "SYS_TITLE_FORMAT_END";
        FileUtil fileUtil = new FileUtil();

        //创建表文件
        String tablePath = "";
        tablePath = ManinUI.currentDatabase.getFilename()+"/TABLE/"+primarydata.getTableName()+".txt";
        fileUtil.createFile(primarydata.getTableName()+".txt",ManinUI.currentDatabase.getFilename()+"/TABLE/");
        fileUtil.writeToFile(formatInfo,tablePath);

        //循环写入recordAllocationTableAndRecordSpace
        long sumDatatype  = calculateDatatype(table);
        String flaSpace = "";
        for(int i= 0;i<sumDatatype;i++){
            flaSpace = flaSpace + "f";
        }
        writeRATAndRS(flaSpace,dataLines,tablePath);
        return true;
    }

    //计算每行的数量
    public long calculateDataLines(Table table,int formatInfoSize) throws IOException {
        long sumWorkspace = 0;
        //记录空间分配大小计算
        //1.解析table数据类型
        sumWorkspace=calculateDatatype(table)+sumWorkspace+2;//+2为换行符，+1为数据分隔符
        //格式信息大小 109字节

        //计算记录分配表与记录空间持有的大小 1Mb=1048576字节
        long record_Space_distribute = ManinUI.currentDatabase.getTablesize()*1048576 - formatInfoSize;
        //计算行数 取整
        long recordAllocations=record_Space_distribute / sumWorkspace;
        //行数作为记录分配表大小 再取一次行数
        record_Space_distribute = record_Space_distribute - recordAllocations;
        long dataLines = record_Space_distribute / sumWorkspace;
        //有盈余空间
        System.out.println("这个表可以存放"+dataLines+"条数据");
        //返回可以存放数据的个数
        return dataLines;
    }

    public int calculateDatatype(Table table){
        //解析数据类型 暂时判断 INT与VARCHAR
        int sumDatatype = 0;
        int flag_num = 0;
        //计算@分割的大小
        for(String datatype:table.getDatatype()){
            flag_num++;
        //整型 MAX 2147483647
        if("INT".equals(datatype))
            sumDatatype = sumDatatype + 11;
        if(datatype.indexOf("VARCHAR")!=(-1)){
            String varcharBit = datatype.substring((datatype.indexOf("(")+1),datatype.indexOf(")"));
            sumDatatype = sumDatatype + Integer.parseInt(varcharBit)+flag_num;
        }
    }
        return sumDatatype;
}

    //循坏写入数据
    public void writeRATAndRS(String flaSpace,long dataLines,String tablePath){
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(tablePath, true)));
            out.write("SYS_TITLE_RECORD_BEGIN\r\n");
            for(int i=0;i<dataLines;i++)
                out.write("0");
            out.write("\r\nSYS_TITLE_RECORD_END\r\n");
            out.write("SYS_TITLE_DATA_BEGIN\r\n");
            for(int i=0;i<dataLines;i++){
                out.write(flaSpace+"\r\n");
            }
            out.write("SYS_TITLE_DATA_END\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //基本表文件格式写入表文件END

    //插入数据至表中
    public boolean inputDataWriteTable(Primarydata primarydata) throws IOException {
        FileUtil fileUtil = new FileUtil();
        //定义空数据空间
        String record = "";
        ArrayList<String> lines = new ArrayList<String>();
        //定位插入位置,并临时更换该位置数据为已有数据
        lines=fileUtil.getlContentLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt","SYS_TITLE_RECORD_BEGIN","SYS_TITLE_RECORD_END");
        int insert_index=1;
        for(String line:lines){
            System.out.println(line);
            String[] line_split = line.split("");
            for(int i=0 ; i < line_split.length ; i++){
                System.out.println(line_split[i]);
                if(line_split[i].equals("0")){
                    line_split[i]="1";
                    record = StringUtils.join(line_split);
                    break;
                }
                else
                    insert_index++;
            }
    }
        //定位到SYS_TITLE_DATA_BEGIN处开始插入
        insert_index=insert_index+11;
        System.out.println("插入位置为："+insert_index);
        //封装插入内容
        String insertContent = "";
        for(int i = 0 ;i< primarydata.getAlltable().getAttribute().size();i++)
            insertContent = insertContent+primarydata.getAlltable().getContent().get(i)+",";
        System.out.println("插入的内容为："+insertContent);
        //字节填充
            //获取原字节
        String orgin_String =  fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",insert_index);
        int orgin_Byte=orgin_String.getBytes().length;
        System.out.println("原位置字节数:"+orgin_Byte);
            //获取插入数据字节
        int insert_Byte=insertContent.getBytes().length;
            //判断是否需要填充，进行字节填充
        if(insert_Byte<orgin_Byte){
            for(int i = insert_Byte;i < orgin_Byte; i++){
                insertContent = insertContent+"f";
            }
        }
        System.out.println("封装之后的插入数据为："+insertContent);
        //执行替换
        if(fileUtil.replaceLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",insert_index,insertContent)){
            System.out.println("数据空间替换完成。");
        }
        //记录数据，0->1
        if(fileUtil.replaceLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",9,record)){
            System.out.println("替换数据记录表完成。");
            System.out.println("命令成功完成。");
        }


        return false;
    }
}
