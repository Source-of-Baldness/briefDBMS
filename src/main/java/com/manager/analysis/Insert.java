package com.manager.analysis;

import com.ui.ManinUI;
import com.util.AnalysisUtil;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Insert {
    public void baseAnalysis(String sql){
        System.out.println(sql);
        //注入SQL的插入正则判断，不带列名，INSERT INTO 表名称 VALUES (值1, 值2,....)
        Pattern pNotColumn = Pattern.compile("^[\\s]*INSERT[\\s]+INTO[\\s]+([A-Z][A-Z]*)+[\\s]+VALUES[\\s]+\\(('([A-Z][A-Z]*)',*)+\\)$");
        //注入SQL的插入正则判断，带列名，INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
        //Pattern pColumn = Pattern.compile("^[\\s]*INSERT[\\s]+INTO[\\s]+([A-Z][A-Z]*)+[\\s]+VALUES[\\s]+\\(('([A-Z][A-Z]*)',*)+\\)$");
        Matcher mNotColumn = pNotColumn.matcher(sql);
        //Matcher mColumn = pColumn.matcher(sql);
        boolean resultNotColumn = mNotColumn.matches();
        //boolean resultColumn = mColumn.matches();
        System.out.println("不存在列名："+resultNotColumn);
        if(resultNotColumn)
            notExistsColumn(mNotColumn.group(1));//传入表名
        //if(resultColumn)
        //    existsColumn();
    }

    //不带列名
    public void notExistsColumn(String tableName){
        System.out.println("插入的表名:"+tableName);
        //判断数据库中是否存在该表
        AnalysisUtil analysisUtil = new AnalysisUtil();
        boolean isTableResult = analysisUtil.isHaveTheTable(tableName,ManinUI.currentDatabase.getFilename());
        if(isTableResult){
            System.out.printf("该表存在");
        }else{
            System.out.println("不存在这张表");
            return;
        }
    }

    //带列名
    public void existsColumn(){
        //解析表名
    }

}
