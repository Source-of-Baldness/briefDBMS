package com.manager.analysis;

import com.manager.data.PrimaryRecord;
import com.manager.data.TableRecord;
import com.manager.data.UserRecord;
import com.pojo.Primarydata;
import com.pojo.Table;
import com.ui.ManinUI;
import com.util.AnalysisUtil;
import net.sf.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateTable {
    public void baseAnalysis(String sql) throws Exception {
        Pattern p = Pattern.compile("^[\\s]*CREATE[\\s]TABLE[\\s]+([A-Z][A-Z]*)([\\s]+)?\\((([\\s]+)?(([\\w]*[\\s]+(INT[\\s]*(NOT[\\s]*NULL|PRIMARY[\\s]*KEY)?,|VARCHAR\\([\\d]+\\)[\\s]*(NOT[\\s]*NULL|PRIMARY[\\s]*KEY)?,*))+))*[\\s]*\\)[\\s]*$");
        Matcher m = p.matcher(sql);
        boolean result = m.matches();
        System.out.println("二次验证"+result);
        if(!result){
            return;
        }
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
        List<String> content = new ArrayList<String>();//表内容
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
            isPrimary.add(isPrimaryKey(split_one_attribution));
            //若为PRIMARY时，自动添加NOT NULL约束
            if(isPrimaryKey(split_one_attribution)){
                isNull.add(true);
            }else{
                isNull.add(isNotNull(split_one_attribution));
            }
            //为content开辟空间
            content.add("");
        }
        //定位表名
        String table_begin_split = sql.substring(0,attribute_indexOf_begin);
        System.out.println(table_begin_split);
        String table_end_split[] = table_begin_split.split("\\s+");
        tableName=table_end_split[2];//定位表名
        //判断表是否重复
        AnalysisUtil analysisUtil = new AnalysisUtil();
        if(!analysisUtil.isHaveTheTable(tableName,ManinUI.currentDatabase.getFilename())){
            System.out.println("表 '"+tableName+"' 准备就绪。");
        }else{
            System.out.println("当前数据库 '"+ManinUI.currentDatabase.getName()+"' 已存在 表 '"+tableName+"' 创建表结构失败。");
            return null;
        }

        //封装table实体类
        table.setAttribute(attribute);
        table.setDatatype(datatype);
        table.setIsNull(isNull);
        table.setIsPrimary(isPrimary);
        table.setContent(content);
        //获取json对象
        JSONObject json = JSONObject.fromObject(table);
        String table_string = json.toString();
        System.out.println(table_string);
        //封装主数据文件实体类
        primarydata.setTableName(tableName);
        UserRecord userRecord = new UserRecord();
        primarydata.setTablePath(ManinUI.currentDatabase.getFilename()+"/TABLE");
        primarydata.setAlltable(table);

        //属性完整性审核
        PrimaryRecord primaryRecord = new PrimaryRecord();
        if(integrityCheck(table)){
            System.out.println("SQL命令成功完成");
            //存入主数据文件
            if(primaryRecord.tableToPrimary(primarydata)){
                System.out.println("存入主数据文件成功");
            }else {
                System.out.println("存入主数据文件失败");
                return null;
            }
            //基础数据存入表文件
            TableRecord tableRecord = new TableRecord();
            if(tableRecord.writeBase_Table(table,primarydata)){
                System.out.println("存入表文件成功");
            }else{
                System.out.println("存入表文件失败");
                return null;
            }
            //主键信息存入表文件
            if(tableRecord.primaryKey_record(primarydata)){
                System.out.println("主键信息创建成功");
            }else{
                System.out.println("主键信息创建失败");
                return null;
            }



        }else {
            System.out.println("CreateTable类在分离参数时发生未知错误");
            return null;
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
            //检测列名是否完整
            for(String attribute:table.getAttribute()){
                if(attribute==null || "".equals(attribute) || " ".equals(attribute))
                    return false;
            }
            //检测数据类型是否符合规范
            for(String datatype:table.getAttribute()){
                if(datatype.indexOf("INT")==(-1)) {
                    if (datatype.indexOf("VARCHAR(0)") != (-1))
                        return false;
                }
            }
            return true;
       }




}

