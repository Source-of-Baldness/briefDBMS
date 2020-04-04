package com.manager.analysis;

import com.alibaba.fastjson.support.hsf.HSFJSONUtils;
import com.manager.data.TableRecord;
import com.pojo.Primarydata;
import com.pojo.Table;
import com.ui.ManinUI;
import com.util.AnalysisUtil;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Insert {
    public void baseAnalysis(String sql) throws IOException {
        System.out.println(sql);
        //注入SQL的插入正则判断，不带列名，INSERT INTO 表名称 VALUES (值1, 值2,....)
        Pattern pNotColumn = Pattern.compile("^[\\s]*INSERT[\\s]+INTO[\\s]+([A-Z][A-Z]*)+[\\s]+VALUES[\\s]+\\(,*('?((([A-Z][A-Z]*)|[0-9]))'?,*)+\\)$");
        //注入SQL的插入正则判断，带列名，INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
        //Pattern pColumn = Pattern.compile("^[\\s]*INSERT[\\s]+INTO[\\s]+([A-Z][A-Z]*)+[\\s]+VALUES[\\s]+\\(('([A-Z][A-Z]*)',*)+\\)$");
        Matcher mNotColumn = pNotColumn.matcher(sql);
        //Matcher mColumn = pColumn.matcher(sql);
        boolean resultNotColumn = mNotColumn.matches();
        //boolean resultColumn = mColumn.matches();
        System.out.println("不存在列名："+resultNotColumn);
        if(resultNotColumn)
            notExistsColumn(mNotColumn.group(1),sql);//传入表名
        //if(resultColumn)
        //    existsColumn();

    }

    //不带列名
    public void notExistsColumn(String tableName,String sql) throws IOException {
        System.out.println("插入的表名:"+tableName);
        //判断数据库中是否存在该表
        AnalysisUtil analysisUtil = new AnalysisUtil();
        boolean isTableResult = analysisUtil.isHaveTheTable(tableName,ManinUI.currentDatabase.getFilename());
        if(isTableResult){
            System.out.println("该表存在");
            //截取总参数
            String parameters_all = sql.substring((sql.indexOf("(")+1),sql.indexOf(")"));
            //分离参数
            String[] parameters = parameters_all.split(",");
            //循环输出分离的参数
            for(String parameter:parameters){
                System.out.println(parameter);
            }
            //获取主数据中的表结构,当前需要插入的表
            Primarydata primarydata=analysisUtil.getTableStruct(tableName,ManinUI.currentDatabase.getFilename(),ManinUI.currentDatabase.getName());
            System.out.println("该表的结构为:"+primarydata.getAlltable().getAttribute());
            //判断表结构与接收到的参数是否相等
            System.out.println(parameters.length+","+primarydata.getAlltable().getAttribute().size());
            if(parameters.length==primarydata.getAlltable().getAttribute().size()){
                System.out.println("输入数据完成匹配");
                //传入表数据中
                List<String> parametersList = Arrays.asList(parameters);
                //脱壳处理,关键字加壳处理
                for(int i=0;i<parametersList.size();i++){
                    if(parametersList.get(i).equals("NULL") || parametersList.get(i).equals("null")){
                        //关键字加壳处理
                        System.out.println("关键字加壳处理");
                        parametersList.set(i,"");
                        continue;
                    }
                    if(parametersList.get(i).indexOf("'")!=(-1)){
                        String[] parameter_split=parametersList.get(i).split("'");
                        parametersList.set(i,parameter_split[1]);
                    }
                }

                primarydata.getAlltable().setContent(parametersList);
                System.out.println("输入的参数为:"+primarydata.getAlltable().getContent());
                //约束性判断
                if(restrainJudge(primarydata)){
                    System.out.println("约束性判断完成");
                }
                //开始写入表数据中
                TableRecord tableRecord = new TableRecord();
                tableRecord.inputDataWriteTable(primarydata);


            }else{
                System.out.println("列名或所提供值的数目与表定义不匹配。");
            }


        }else{
            System.out.println("不存在这张表");
            return;
        }
    }

    //带列名
    public void existsColumn(){
        //解析表名
    }

    //约束性判断,主键，不为空，类型，字符数,数据最大值截取
    public boolean restrainJudge(Primarydata primarydata){
        Table table = new Table();
        table = primarydata.getAlltable();
        for(int i = 0;i<table.getAttribute().size();i++){
            //输入参数的类型判断
            String dataType = inputDatatype(table.getContent().get(i));
            if(!dataType.equals("NULL")){
                //判断表数据结构是否为整型
                if(table.getDatatype().get(i).equals("INT")){
                    if(table.getDatatype().get(i).indexOf(dataType)==(-1)){
                        System.out.println("在将 varchar 值" +table.getContent().get(i)+" 转换成数据类型 int 时失败。");
                        return false;
                    }else{
                        //进行int 整型大小是否超过最大值，超过进行捕获
                        try{
                            Integer.parseInt(table.getContent().get(i));
                        }catch (Exception e){
                            System.out.println("在插入表 '"+primarydata.getTableName()+"' 中，列 '"+table.getAttribute().get(i)+"' 超过int 类型的最大值 '2147483647',已自动转化为 '2147483647'");
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
                    return false;
                }
            }
        //主键判断是否重复


        }
        return true;

    }

    //解析输入的数据类型 仅支持 varchar、int、空值
    public String inputDatatype(String content){
        System.out.println("传入的参数为："+content);
        if(content == null || content.trim().length() == 0){
            System.out.println("NULL");
            return "NULL";
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(content);
        if(!isNum.matches()){
            System.out.println("VARCHAR");
            return "VARCHAR";
        }else{
            System.out.println("INT");
            return "INT";
        }

        //主键解析

    }



}
