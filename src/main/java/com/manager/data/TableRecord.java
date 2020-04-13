package com.manager.data;

import com.Socket.impl.SocketServiceImpl;
import com.manager.analysis.Insert;
import com.pojo.Primarydata;
import com.pojo.Table;
import com.ui.ManinUI;
import com.util.AnalysisUtil;
import com.util.FileUtil;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
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
                "SYS_AllRecords@@0\r\n" +
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
            SocketServiceImpl socketService = new SocketServiceImpl();
            socketService.sqlResult("1");
            socketService.sqlResult("命令成功完成。");
        }


        return false;
    }

    //写入主键头部信息，SYS_TABLE_PRIMARY_KEY_INFO
    public boolean primaryKey_record(Primarydata primarydata) throws IOException {
        Table table = new Table();
        int primary_flag = 0;
        table = primarydata.getAlltable();
        //新建文件SYS_TABLE_PRIMARY_KEY_INFO
        FileUtil fileUtil = new FileUtil();
        fileUtil.createFile(primarydata.getTableName()+".txt",ManinUI.currentDatabase.getFilename()+"/SYS_TABLE_PRIMARY_KEY_INFO");
        //封装主键属性
        ArrayList<String> primaryKey_Attribute = new ArrayList<String>();
        for(Boolean primaryKey:table.getIsPrimary()){
            //如果为真，则将主键属性封装进ArrayList中
            if(primaryKey){
                primaryKey_Attribute.add(table.getAttribute().get(primary_flag));
            }
            primary_flag++;
        }
        String primaryMerge = StringUtils.join(primaryKey_Attribute,",");
        //写入基本信息
        if(fileUtil.writeToFile(primaryMerge,ManinUI.currentDatabase.getFilename()+"/SYS_TABLE_PRIMARY_KEY_INFO/"+primarydata.getTableName()+".txt")){
            return true;
        }
        return false;
    }

    //主键信息写入
    public boolean primaryKey_record_data(Primarydata primarydata) throws IOException {
        Table table = new Table();
        table = primarydata.getAlltable();
        //封装主键属性的content
        ArrayList<String> primaryKey_content = new ArrayList<String>();
        int primary_flag = 0;
        for(Boolean primaryKey:table.getIsPrimary()){
            //如果为真，则将主键属性的content封装进ArrayList中
            if(primaryKey){
                primaryKey_content.add(table.getContent().get(primary_flag));
                primary_flag++;
            }
        }
        String primaryMerge = StringUtils.join(primaryKey_content,",");
        //写入基本信息
        FileUtil fileUtil = new FileUtil();
        if(fileUtil.writeToFile(primaryMerge,ManinUI.currentDatabase.getFilename()+"/SYS_TABLE_PRIMARY_KEY_INFO/"+primarydata.getTableName()+".txt")){
            System.out.println("主键信息写入完成。");
            return true;
        }
        return false;
    }

    //更新表索引
    public boolean updata_table_index(Primarydata primarydata) throws IOException {
        FileUtil fileUtil = new FileUtil();
        int first_index = 0;
        int end_index = 0;
        int allRecords = 0;
        int flag_index = 0;
        ArrayList<String> records = new ArrayList<String>();
        records = fileUtil.getlContentLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt","SYS_TITLE_RECORD_BEGIN","SYS_TITLE_RECORD_END");
        String record = records.get(0);
        first_index = record.indexOf("1")+1;
        //end_index = first_index; 用indexOF一直叠加，直到找到最后一个值
        while(end_index !=(-1)){
            end_index = record.indexOf("1",(end_index+1));
            if(end_index!=(-1))
                flag_index = end_index;
        }
        end_index = flag_index+1;
        System.out.println("更新记录空间索引位置");
        System.out.println("first:"+first_index);
        System.out.println("end:"+end_index);
        fileUtil.replaceLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",5,"SYS_First@@"+first_index);
        fileUtil.replaceLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",6,"SYS_End@@"+end_index);
//更新SYS_AllRecoeds@@***
        String[] records_arr = records.get(0).split("");
        for(String record_flag:records_arr){
            if(record_flag.equals("1")){
                allRecords++;
            }
        }
        fileUtil.replaceLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",4,"SYS_AllRecords@@"+allRecords);




        return true;
    }

    //Update语句 ，不带where条件进行更新表文件
    public boolean update_notWhere_replaceFile(Primarydata primarydata,ArrayList<String> update_Attribution,ArrayList<String> update_Content) throws IOException {
        //受影响的条数
        int influence_num=0;
        //循环查询,封装content至table中
        FileUtil fileUtil = new FileUtil();
        AnalysisUtil analysisUtil =new AnalysisUtil();
        String[] record = fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",9).split("");
        for(int i=analysisUtil.getSYS_First(primarydata);i<analysisUtil.getSYS_End(primarydata);i++){
            //如果为1 定位到该处，并封装进table
            if(record[(i-1)].equals("1")){
                String tableDateLine = fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",(11+i));
                String[] tableDate = tableDateLine.split(",");
                //封装
                for(int j=0;j<primarydata.getAlltable().getAttribute().size();j++){
                    primarydata.getAlltable().getContent().set(j,tableDate[j]);
                }
                //已得到一个封装完成的tableContent，下个循环会覆盖Content
                System.out.println("表数据内容为："+primarydata.getAlltable().getContent());
                influence_num++;
                //替换Content
                    //匹配属性值
                for(int k=0;k<primarydata.getAlltable().getAttribute().size();k++){
                    int update_flag = 0;
                    for(String update_att_one:update_Attribution){
                        if(update_att_one.equals(primarydata.getAlltable().getAttribute().get(k))){
                            primarydata.getAlltable().getContent().set(k,update_Content.get(update_flag));
                        }
                        update_flag++;
                    }
                }
                //完成修改
                //进行约束性判断,调用insert命令
                System.out.println("进行约束性判断");
                Insert insert = new Insert();
                if(insert.restrainJudge(primarydata)){
                    System.out.println("约束性判断成功.");
                    //进行表数据的更新

                    //开始更新表文件-  -- -  -
                    //封装插入内容
                    int update_index=11+i;
                    String insertContent = "";
                    for(int l = 0 ;l< primarydata.getAlltable().getAttribute().size();l++)
                        insertContent = insertContent+primarydata.getAlltable().getContent().get(l)+",";
                    System.out.println("插入的内容为："+insertContent);
                    //字节填充
                    //获取原字节
                    String orgin_String =  fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",update_index);
                    int orgin_Byte=orgin_String.getBytes().length;
                    System.out.println("原位置字节数:"+orgin_Byte);
                    //获取插入数据字节
                    int insert_Byte=insertContent.getBytes().length;
                    //判断是否需要填充，进行字节填充
                    if(insert_Byte<orgin_Byte){
                        for(int m = insert_Byte;m < orgin_Byte; m++){
                            insertContent = insertContent+"f";
                        }
                    }
                    System.out.println("封装之后的更新数据为："+insertContent);
                    //执行替换
                    if(fileUtil.replaceLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",update_index,insertContent)){
                        System.out.println("数据空间替换完成。");
                    }
                    //    更新表文件 完成 - - - - - -




                }
                else{
                    System.out.println("约束性判断失败.");
                    return false;
                }

                //打印更新后的内容
                System.out.println("更新后的内容为:"+primarydata.getAlltable().getContent());
            }
        }
        System.out.println("受影响的行数("+influence_num+")");
        SocketServiceImpl socketService = new SocketServiceImpl();
        socketService.sqlResult("1");
        socketService.sqlResult("受影响的行数("+influence_num+")");
        return false;
    }


    public boolean update_Where_replaceFile(Primarydata primarydata, ArrayList<String> update_Attribution, ArrayList<String> update_Content,ArrayList<Integer> update_line) throws IOException {
        //循环查询,封装content至table中
        FileUtil fileUtil = new FileUtil();
        AnalysisUtil analysisUtil =new AnalysisUtil();

        for(int i:update_line){
            System.out.println("正在操作第："+i+"行");
                String tableDateLine = fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",i);
                String[] tableDate = tableDateLine.split(",");
                //封装
                for(int j=0;j<primarydata.getAlltable().getAttribute().size();j++){
                    primarydata.getAlltable().getContent().set(j,tableDate[j]);
                }
                //已得到一个封装完成的tableContent，下个循环会覆盖Content
                System.out.println("表数据内容为："+primarydata.getAlltable().getContent());
                //替换Content
                //匹配属性值
                for(int k=0;k<primarydata.getAlltable().getAttribute().size();k++){
                    int update_flag = 0;
                    for(String update_att_one:update_Attribution){
                        if(update_att_one.equals(primarydata.getAlltable().getAttribute().get(k))){
                            primarydata.getAlltable().getContent().set(k,update_Content.get(update_flag));
                        }
                        update_flag++;
                    }
                }
                //完成修改
                //进行约束性判断,调用insert命令
                System.out.println("进行约束性判断");
                Insert insert = new Insert();
                if(insert.restrainJudge(primarydata)){


                    System.out.println("约束性判断成功.");
                    //进行表数据的更新

                    //开始更新表文件-  -- -  -
                    //封装插入内容
                    String insertContent = "";
                    for(int l = 0 ;l< primarydata.getAlltable().getAttribute().size();l++)
                        insertContent = insertContent+primarydata.getAlltable().getContent().get(l)+",";
                    System.out.println("插入的内容为："+insertContent);
                    //字节填充
                    //获取原字节
                    String orgin_String =  fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",i);
                    int orgin_Byte=orgin_String.getBytes().length;
                    System.out.println("原位置字节数:"+orgin_Byte);
                    //获取插入数据字节
                    int insert_Byte=insertContent.getBytes().length;
                    //判断是否需要填充，进行字节填充
                    if(insert_Byte<orgin_Byte){
                        for(int m = insert_Byte;m < orgin_Byte; m++){
                            insertContent = insertContent+"f";
                        }
                    }
                    System.out.println("封装之后的更新数据为："+insertContent);
                    //执行替换
                    if(fileUtil.replaceLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",i,insertContent)){
                        System.out.println("数据空间替换完成。");
                    }
                    //    更新表文件 完成 - - - - - -

                }
                else{
                    System.out.println("约束性判断失败.");
                    return false;
                }
                //打印更新后的内容
                System.out.println("更新后的内容为:"+primarydata.getAlltable().getContent());
        }
        System.out.println("受影响的行数("+update_line.size()+")");
        SocketServiceImpl socketService = new SocketServiceImpl();
        socketService.sqlResult("1");
        socketService.sqlResult("受影响的行数("+update_line.size()+")");
        return false;

    }


    public boolean updatePrimary(Primarydata primarydata){
        Table table = new Table();
        table = primarydata.getAlltable();
        for(int i = 0;i<table.getAttribute().size();i++){
            //输入参数的类型判断
            Insert insert = new Insert();
            String dataType = insert.inputDatatype(table.getContent().get(i));
            if(!dataType.equals("NULL")){
                //判断表数据结构是否为整型
                if(table.getDatatype().get(i).equals("INT")){
                    if(table.getDatatype().get(i).indexOf(dataType)==(-1)){
                        System.out.println("在将 varchar 值" +table.getContent().get(i)+" 转换成数据类型 int 时失败。");
                        SocketServiceImpl socketService = new SocketServiceImpl();
                        socketService.sqlResult("1");
                        socketService.sqlResult("在将 varchar 值" +table.getContent().get(i)+" 转换成数据类型 int 时失败。");
                        return false;
                    }else{
                        //进行int 整型大小是否超过最大值，超过进行捕获
                        try{
                            Integer.parseInt(table.getContent().get(i));
                        }catch (Exception e){
                            System.out.println("在插入表 '"+primarydata.getTableName()+"' 中，列 '"+table.getAttribute().get(i)+"' 超过int 类型的最大值 '2147483647',已自动转化为 '2147483647'");
                            SocketServiceImpl socketService = new SocketServiceImpl();
                            socketService.sqlResult("1");
                            socketService.sqlResult("在插入表 '"+primarydata.getTableName()+"' 中，列 '"+table.getAttribute().get(i)+"' 超过int 类型的最大值 '2147483647',已自动转化为 '2147483647'");
                            table.getContent().set(i,"2147483647");
                        }
                    }
                }
                //计算字符类型数据长度是否合理，不合理进行截取
                else {//不是整型则为字符型
                    //读取表结构varchar类型的字节最大值
                    int dataLength = Integer.parseInt(table.getDatatype().get(i).substring((table.getDatatype().get(i).indexOf("(")+1),table.getDatatype().get(i).indexOf(")")));
                    System.out.println("表结构的参数字节大小为："+dataLength);
                    //读取输入的字符字节大小
                    String input_String = table.getContent().get(i);
                    int input_Bytes = input_String.getBytes().length;
                    System.out.println("传入的参数字节大小为："+input_Bytes);
                    //进行判断,输入>规定，进行截取，否则不做操作
                    if(input_Bytes>dataLength){
                        //进行截取
                        input_String = input_String.substring(0,dataLength);
                        System.out.println("截取后的内容为："+input_String);
                        //封装截取内容至表结构
                        table.getContent().set(i,input_String);
                        System.out.println("封装之后的内容为："+table.getContent().get(i));
                    }
                }
            }else{
                //NOT NULL 约束
                if(table.getIsNull().get(i)){
                    System.out.println("不能将值 NULL 插入列'"+ table.getAttribute().get(i)+"'表 '"+primarydata.getTableName()+"'；列不允许有 Null 值。INSERT 失败。");
                    SocketServiceImpl socketService = new SocketServiceImpl();
                    socketService.sqlResult("1");
                    socketService.sqlResult("不能将值 NULL 插入列'"+ table.getAttribute().get(i)+"'表 '"+primarydata.getTableName()+"'；列不允许有 Null 值。INSERT 失败。");
                    return false;
                }
            }

        }
        //主键判断是否重复
        //提取主键信息
        FileUtil fileUtil = new FileUtil();
        ArrayList<String> primary_Key = new ArrayList<String>();
        primary_Key = fileUtil.getlLimitsLineOfTxt(ManinUI.currentDatabase.getFilename()+"/SYS_TABLE_PRIMARY_KEY_INFO/"+primarydata.getTableName()+".txt",2,-1);

        return true;
    }



}