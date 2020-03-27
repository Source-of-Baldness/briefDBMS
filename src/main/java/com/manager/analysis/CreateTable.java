package com.manager.analysis;

import com.pojo.Primarydata;
import com.pojo.Table;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateTable {
    public void baseAnalysis(String sql) throws Exception {
        Pattern p = Pattern.compile("^[\\s]*CREATE[\\s]TABLE[\\s]+([A-Z][A-Z]*)([\\s]+)?\\((([\\s]+)?(([\\w]*[\\s]+(INT[\\s]*(NOT[\\s]*NULL|PRIMARY[\\s]*KEY)?,|VARCHAR\\([\\d]+\\)[\\s]*(NOT[\\s]*NULL|PRIMARY[\\s]*KEY)?,))+))*[\\s]*\\)[\\s]*$");
        Matcher m = p.matcher(sql);
        boolean result = m.matches();
        System.out.println("二次验证"+result);
        //解析表属性
        parameterSplit(sql);

    }

    //关键参数定位
    public Table parameterSplit(String sql) throws Exception{
        Table table = new Table();//表结构
        Primarydata primarydata = new Primarydata();//主数据文件
        List<String> attribute = new ArrayList<String>();//表的属性
        List<String> datatype = new ArrayList<String>();//数据类型
        List<Boolean> isNull = new ArrayList<Boolean>();//是否为空
        List<Boolean> isPrimary = new ArrayList<Boolean>();//是否为主键
        String tableName;//表名
        String tablePath;//表的路径

        //取得列名字符串位置,从（后的第一个字母开始定位至结束
        int attribute_indexOf_begin=sql.indexOf("(");
        String attribute_all = sql.substring(attribute_indexOf_begin+1,sql.length()-1);
        System.out.println(attribute_all);

// begin 开始分离table属性

        //二次拆除
        String split_attribute_all[]=attribute_all.split(",");
        for(String one_attribution:split_attribute_all) {
            //三次拆除，分离列名和数据类型
            String split_one_attribution[] = one_attribution.split("\\s+");
            //解决开头连续空格问题
            if(split_one_attribution[0].equals("")){
                attribute.add(split_one_attribution[1]);
                datatype.add(split_one_attribution[2]);
            }else{
                attribute.add(split_one_attribution[0]);
                datatype.add(split_one_attribution[1]);
            }
            //接 NOT NULL OR PRIMARY解析
            isNull.add(isNotNull(split_one_attribution));
            isPrimary.add(isPrimaryKey(split_one_attribution));
        }
        //定位表名
        String table_begin_split = sql.substring(0,attribute_indexOf_begin);
        System.out.println(table_begin_split);
        String table_end_split[] = table_begin_split.split("\\s+");
        tableName=table_end_split[2];//定位表名
        //封装table实体类
        table.setAttribute(attribute);
        table.setDatatype(datatype);
        table.setIsNull(isNull);
        table.setIsPrimary(isPrimary);
        //获取json对象
        JSONObject json = JSONObject.fromObject(table);
        String table_string = json.toString();
        System.out.println(table_string);
        //封装主数据文件实体类
        primarydata.setTableName(tableName);
        //primarydata.setTablePath();
        primarydata.setAlltable(table);

        //属性完整性审核
        if(integrityCheck(table)){
            System.out.println("有完整的列名支撑");
            //存入主数据文件
            //存入表文件
        }else {
            System.out.println("创建表失败");
        }
          return null;
        }

        //解析不为空 NOT NULL
    public boolean isNotNull(String[] content){
        String merge=String.join("@", content);
        if(merge.indexOf("NOT@NULL")==-1)
            return false;
        else
            return true;
    }

        //解析主键PRIMARY KEY
        public boolean isPrimaryKey(String[] content){
            String merge=String.join("@", content);
            if(merge.indexOf("PRIMARY@KEY")==-1)
                return false;
            else
                return true;
        }

        //列名/属性的完整性检验
        public boolean integrityCheck(Table table){
            return false;
       }

    //写入主数据文件

    //写入表结构文件
}

