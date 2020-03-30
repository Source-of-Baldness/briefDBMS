package com.manager.data;

import com.pojo.Primarydata;
import com.pojo.Table;
import com.ui.ManinUI;
import com.util.FileUtil;

import javax.swing.text.TabExpander;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class TableRecord {
    //基本表文件格式写入主数据文件
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
        sumWorkspace=calculateDatatype(table)+sumWorkspace;
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
            out.write("SYS_TITLE_RECORD_END\r\n");
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

}
