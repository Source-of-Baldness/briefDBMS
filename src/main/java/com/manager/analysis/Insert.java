package com.manager.analysis;

import com.Socket.impl.SocketServiceImpl;
import com.manager.data.TableRecord;
import com.pojo.Primarydata;
import com.pojo.Table;
import com.ui.ManinUI;
import com.util.AnalysisUtil;
import com.util.FileUtil;
import org.apache.commons.lang.StringUtils;
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
        Pattern pColumn = Pattern.compile("^[\\s]*INSERT[\\s]+INTO[\\s]+([A-Z][A-Z]*)+[\\s]*\\(,*((([A-Z][A-Z]*)|[0-9]),*)+\\)[\\s]+VALUES[\\s]+\\(,*('?((([A-Z][A-Z]*)|[0-9]))'?,*)+\\)$");
        Matcher mNotColumn = pNotColumn.matcher(sql);
        Matcher mColumn = pColumn.matcher(sql);
        boolean resultNotColumn = mNotColumn.matches();
        boolean resultColumn = mColumn.matches();
        System.out.println("不存在列名："+resultNotColumn);
        System.out.println("存在列名："+resultColumn);
        if(resultNotColumn)
            notExistsColumn(mNotColumn.group(1),sql);//传入表名
        if(resultColumn)
            existsColumn(mColumn.group(1),sql);//传入表名

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
                }else{
                    return ;
                }
                //开始写入表数据中
                TableRecord tableRecord = new TableRecord();
                tableRecord.inputDataWriteTable(primarydata);
                //开始写入主键信息
                tableRecord.primaryKey_record_data(primarydata);
                //开始更新表索引数据
                tableRecord.updata_table_index(primarydata);

            }else{
                System.out.println("");
                SocketServiceImpl socketService = new SocketServiceImpl();
                socketService.sqlResult("1");
                socketService.sqlResult("列名或所提供值的数目与表定义不匹配。");
            }
        }else{
            System.out.println("不存在这张表");
            SocketServiceImpl socketService = new SocketServiceImpl();
            socketService.sqlResult("1");
            socketService.sqlResult("不存在这张表。");
            return;
        }
    }

    //带列名
    public void existsColumn(String tableName,String sql) throws IOException {
        System.out.println("插入的表名:" + tableName);
        //判断数据库中是否存在该表
        AnalysisUtil analysisUtil = new AnalysisUtil();
        boolean isTableResult = analysisUtil.isHaveTheTable(tableName, ManinUI.currentDatabase.getFilename());
        if (isTableResult) {
            System.out.println("该表存在");
            //分离列名和内容的位置
            int att_index_first = sql.indexOf("(") + 1;
            int att_index_end = sql.indexOf(")");
            int content_index_first = sql.indexOf("(", (att_index_end + 1)) + 1;
            int content_index_end = sql.indexOf(")", (att_index_end + 1));
            //截取插入的列名
            String attribution_all = sql.substring(att_index_first, att_index_end);
            //截取插入的内容
            String content_all = sql.substring(content_index_first, content_index_end);

            //分离列名
            String[] attribution_arr = attribution_all.split(",");
            //分离内容
            String[] content_arr = content_all.split(",");

            //循环输出分离的参数
            for (String parameter : attribution_arr) {
                System.out.println("列名" + parameter);
            }
            for (String parameter : content_arr) {
                System.out.println("内容" + parameter);
            }
            //获取主数据中的表结构,当前需要插入的表
            Primarydata primarydata=analysisUtil.getTableStruct(tableName,ManinUI.currentDatabase.getFilename(),ManinUI.currentDatabase.getName());
            System.out.println("该表的结构为:"+primarydata.getAlltable().getAttribute());
            //判断列名是否存在于表结构中
            for(String attribution:attribution_arr){
                int repeat_flag = 0;//判断列名是否重复
                if(!primarydata.getAlltable().getAttribute().contains(attribution)){
                    System.out.println("列名 '"+attribution+"' 无效。");
                    SocketServiceImpl socketService = new SocketServiceImpl();
                    socketService.sqlResult("1");
                    socketService.sqlResult("列名 '"+attribution+"' 无效。");
                    return ;
                }
                //判断列名是否重复
                for(int i=0;i<attribution_arr.length;i++){
                    if(attribution.equals(attribution_arr[i]))
                        repeat_flag++;
                }
                if(repeat_flag>1){
                    System.out.println("在 INSERT 的 SET 子句或列列表中多次指定了列名“"+attribution+"”。在同一子句中不得为一个列分配多个值。请修改该子句，以确保一个列仅更新一次。如果此语句在视图中更新或插入列，列别名可能掩盖您的代码中的重复情况。");
                    SocketServiceImpl socketService = new SocketServiceImpl();
                    socketService.sqlResult("1");
                    socketService.sqlResult("在 INSERT 的 SET 子句或列列表中多次指定了列名“"+attribution+"”。在同一子句中不得为一个列分配多个值。请修改该子句，以确保一个列仅更新一次。如果此语句在视图中更新或插入列，列别名可能掩盖您的代码中的重复情况。");
                    return ;
                }
            }

            //判断属性与内容输入数量是否匹配
            System.out.println(attribution_arr.length+","+content_arr.length);
            if(attribution_arr.length==content_arr.length){
                System.out.println("列名与内容数据完成匹配");


                //传入表数据中
                List<String> parametersList = Arrays.asList(content_arr);
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

                //封装表内容（关键步骤）
                int packing_attribution_index = -1;
                int notPacking_attribution_index = -1;
                for(int i=0;i<primarydata.getAlltable().getAttribute().size();i++){
                    for(int j=0;j<attribution_arr.length;j++){
                        if(attribution_arr[j].equals(primarydata.getAlltable().getAttribute().get(i))){
                            packing_attribution_index= i;
                            notPacking_attribution_index = j;
                            break;
                        }
                    }
                    //开始封装对应的内容
                    if(packing_attribution_index!=(-1) && notPacking_attribution_index!=(-1))
                        primarydata.getAlltable().getContent().set(packing_attribution_index,parametersList.get(notPacking_attribution_index));
                }
                System.out.println("输入的参数为:"+primarydata.getAlltable().getContent());

                //约束性判断
                if(restrainJudge(primarydata)){
                    System.out.println("约束性判断完成");
                }else{
                    return ;
                }
                //开始写入表数据中
                TableRecord tableRecord = new TableRecord();
                tableRecord.inputDataWriteTable(primarydata);
                //开始写入主键信息
                tableRecord.primaryKey_record_data(primarydata);
                //开始更新表索引数据
                tableRecord.updata_table_index(primarydata);


            }else{
                System.out.println("INSERT 语句中列的数目大于 VALUES 子句中指定的值的数目。VALUES 子句中值的数目必须与 INSERT 语句中指定的列的数目匹配。");
                SocketServiceImpl socketService = new SocketServiceImpl();
                socketService.sqlResult("1");
                socketService.sqlResult("INSERT 语句中列的数目大于 VALUES 子句中指定的值的数目。VALUES 子句中值的数目必须与 INSERT 语句中指定的列的数目匹配。");
                return ;
            }
        }else{
            System.out.println("不存在这张表");
            SocketServiceImpl socketService = new SocketServiceImpl();
            socketService.sqlResult("1");
            socketService.sqlResult("不存在这张表");
            return;
        }
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
        //提取插入的主键信息
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
        for(String primary_Key_split:primary_Key){
               if(primaryMerge.equals(primary_Key_split)){
                   System.out.println("违反了 PRIMARY KEY 约束“PK__"+primarydata.getTableName()+"__*”。不能在对象“"+primarydata.getTableName()+"”中插入重复键。重复键值为 ("+primaryMerge+")。");
                   SocketServiceImpl socketService = new SocketServiceImpl();
                   socketService.sqlResult("1");
                   socketService.sqlResult("违反了 PRIMARY KEY 约束“PK__"+primarydata.getTableName()+"__*”。不能在对象“"+primarydata.getTableName()+"”中插入重复键。重复键值为 ("+primaryMerge+")。");
                   return false;
               }
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

    }



}
