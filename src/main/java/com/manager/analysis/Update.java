package com.manager.analysis;

import com.manager.data.TableRecord;
import com.pojo.Primarydata;
import com.pojo.Table;
import com.ui.ManinUI;
import com.util.AnalysisUtil;
import com.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Update {
    public void baseAnalysis(String sql) throws IOException {
        System.out.println(sql);
        Pattern p = Pattern.compile("^[\\s]*UPDATE[\\s]+([A-Z][A-Z]*|[0-9])+[\\s]+SET[\\s]+(([\\s]*([A-Z][A-Z]*|[0-9])+[\\s]*=[\\s]*'?([A-Z][A-Z]*|[0-9])+'?[\\s]*,?)*)[\\s]*(WHERE[\\s]+(([A-Z][A-Z]*|[0-9])+[\\s]*=[\\s]*'?([A-Z][A-Z]*|[0-9])+'?[\\s]*(AND[\\s]+([A-Z][A-Z]*|[0-9])+[\\s]*=[\\s]*'?([A-Z][A-Z]*|[0-9])+'?[\\s]*)*))*$");
        Matcher m = p.matcher(sql);
        boolean result = m.matches();
        System.out.println("二次判断："+result);
        if(result){
            //分离列名，表名，值
            Primarydata primarydata = new Primarydata();
            String tableName ="";
            ArrayList<String> update_Attribution = new ArrayList<String>(); //待修改的列名
            ArrayList<String> update_Content = new ArrayList<String>(); //修改的内容
            ArrayList<String> where_Attribution = new ArrayList<String>(); //列名条件
            ArrayList<String> where_Content = new ArrayList<String>(); //内容条件
            //分离表名
            tableName=m.group(1);
            System.out.println("传入表结构："+tableName);
            //获取表结构
            AnalysisUtil analysisUtil = new AnalysisUtil();
            primarydata = analysisUtil.getTableStruct(tableName, ManinUI.currentDatabase.getFilename(),ManinUI.currentDatabase.getName());
            System.out.println("该表的结构为:"+primarydata.getAlltable().getAttribute());
            //分离待修改列名，内容
            String[] update_ALL=  m.group(2).split("[\\s]*,[\\s]*");
            for(String update_String:update_ALL){
                String[] update = update_String.split("[\\s]*=[\\s]*");
                for(int i=0;i<update.length;i++){
                    if(i%2==0)
                        update_Attribution.add(update[i]);
                    else
                        update_Content.add(update[i]);
                }
            }
            //分离条件列名，内容
            try {
                 update_ALL = m.group(7).split("[\\s]*AND[\\s]*");
            }
            catch (Exception e){
                update_ALL = null;
            }
            try {
                for(String update_String:update_ALL){
                    String[] update = update_String.split("[\\s]*=[\\s]*");
                    for(int i=0;i<update.length;i++){
                        if(i%2==0)
                            where_Attribution.add(update[i]);
                        else
                            where_Content.add(update[i]);
                    }
                }
            }catch (Exception e){
                System.out.println("未解析到where条件");
            }

            //分离完成 END - - - - -

            //update content脱壳处理,关键字加壳处理
            for(int i=0;i<update_Content.size();i++){
                if(update_Content.get(i).equals("NULL") || update_Content.get(i).equals("null")){
                    //关键字加壳处理
                    System.out.println("关键字加壳处理");
                    update_Content.set(i,"");
                    continue;
                }
                if(update_Content.get(i).indexOf("'")!=(-1)){
                    String[] parameter_split=update_Content.get(i).split("'");
                    update_Content.set(i,parameter_split[1]);
                }
            }
            //where content脱壳处理,关键字加壳处理
            for(int i=0;i<where_Content.size();i++){
                if(where_Content.get(i).equals("NULL") || where_Content.get(i).equals("null")){
                    //关键字加壳处理
                    System.out.println("关键字加壳处理");
                    where_Content.set(i,"");
                    continue;
                }
                if(where_Content.get(i).indexOf("'")!=(-1)){
                    String[] parameter_split=where_Content.get(i).split("'");
                    where_Content.set(i,parameter_split[1]);
                }
            }

            //判断列名是否存在于表结构中
            for(String attribution:update_Attribution){
                int repeat_flag = 0;//判断列名是否重复
                if(!primarydata.getAlltable().getAttribute().contains(attribution)){
                    System.out.println("列名 '"+attribution+"' 无效。");
                    return ;
                }
                //判断列名是否重复
                for(int i=0;i<update_Attribution.size();i++){
                    if(attribution.equals(update_Attribution.get(i)))
                        repeat_flag++;
                }
                if(repeat_flag>1){
                    System.out.println("在 UPDATE 的 SET 子句或列列表中多次指定了列名“"+attribution+"”。在同一子句中不得为一个列分配多个值。请修改该子句，以确保一个列仅更新一次。如果此语句在视图中更新或插入列，列别名可能掩盖您的代码中的重复情况。");
                    return ;
                }
            }

            //判断条件列名是否存在于表结构中
            for(String attribution:where_Attribution){
                int repeat_flag = 0;//判断列名是否重复
                if(!primarydata.getAlltable().getAttribute().contains(attribution)){
                    System.out.println("列名 '"+attribution+"' 无效。");
                    return ;
                }
                //判断列名是否重复
                for(int i=0;i<where_Attribution.size();i++){
                    if(attribution.equals(where_Attribution.get(i)))
                        repeat_flag++;
                }
                if(repeat_flag>1){
                    System.out.println("在 UPDATE 的 WHERE 子句或列列表中多次指定了列名“"+attribution+"”。在同一子句中不得为一个列分配多个值。请修改该子句，以确保一个列仅更新一次。如果此语句在视图中更新或插入列，列别名可能掩盖您的代码中的重复情况。");
                    return ;
                }
            }

            //分叉
            System.out.println(where_Attribution.size());
            if(where_Attribution.size()==0)
                notExistsWhere(primarydata,update_Attribution,update_Content);
            else
                existsWhere(primarydata,update_Attribution,update_Content,where_Attribution,where_Content);
        }else{
            System.out.println("UPDATE 附近有语法错误。");
        }
    }


    //带where 条件的语句
    public void existsWhere(Primarydata primarydata,ArrayList<String> update_Attribution,ArrayList<String> update_Content,ArrayList<String> where_Attribution,ArrayList<String> where_Content) throws IOException {
        System.out.println("该sql命令解析到了where条件");
        ArrayList<Integer> update_line=new ArrayList<Integer>();//记录需要改变
        update_line = isFitUpdatePrimary_where(primarydata,update_Attribution,where_Attribution,where_Content);
        if(update_line==null){
            System.out.println("更新表失败。");
        }else{
            //调用tableRecord，进行数据更新
            TableRecord tableRecord = new TableRecord();
            tableRecord.update_Where_replaceFile(primarydata,update_Attribution,update_Content,update_line);
        }

    }
    //不带where 条件的语句
    public void notExistsWhere(Primarydata primarydata,ArrayList<String> update_Attribution,ArrayList<String> update_Content) throws IOException {
        System.out.println("该sql命令未解析到where条件");
        if(isFitUpdatePrimary(primarydata,update_Attribution)){
            System.out.println("更新表失败。");
        }else{
            //调用tableRecord，进行数据更新
            TableRecord tableRecord = new TableRecord();
            tableRecord.update_notWhere_replaceFile(primarydata,update_Attribution,update_Content);
        }

    }

    //不带where情况涉及主键更新
    public boolean isFitUpdatePrimary(Primarydata primarydata,ArrayList<String> update_Attribution){
        int primary_flag=0;
        for(String attribution:primarydata.getAlltable().getAttribute()){
            for(String update:update_Attribution){
                if(update.equals(attribution)){
                    //判断是否为主键
                    if(primarydata.getAlltable().getIsPrimary().get(primary_flag)){
                        System.out.println("修改的内容涉及到主键");
                        //若SYS_First@@ 与SYS_End 不同，则表有两条以上的数据，若涉及到主键的更改，则拒绝 return
                        FileUtil fileUtil = new FileUtil();
                        String SYS_First_String = fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",5);
                        String SYS_End_String = fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",6);
                        String[] SYS_First = SYS_First_String.split("@@");
                        String[] SYS_End = SYS_End_String.split("@@");
                        System.out.println("first:"+SYS_First[1]+" end:"+SYS_End[1]);
                        if(!SYS_First[1].equals(SYS_End[1])){
                            System.out.println("拒绝访问");
                            return true;
                        }else{
                            System.out.println("该表无数据或者仅有一条数据");
                            return false;
                        }
                    }
                }
            }
            primary_flag++;
        }
    return false;
    }

    //带where情况涉及主键更新
    public ArrayList<Integer> isFitUpdatePrimary_where(Primarydata primarydata,ArrayList<String> update_Attribution,ArrayList<String> where_Attribution,ArrayList<String> where_Content){
        int fit_line = 0;
        ArrayList<Integer> update_line=new ArrayList<Integer>();//记录需要改变){
                        FileUtil fileUtil = new FileUtil();
                        AnalysisUtil analysisUtil = new AnalysisUtil();

        int primary_flag = 0;//提取主键位置
        int fit_flag = 0;//匹配次数
        boolean isPrimary = false;//输入参数是否带主键，包括多主键
        ArrayList<Integer> primary_flag_arr = new ArrayList<Integer>();
        for(boolean isTrue:primarydata.getAlltable().getIsPrimary()){
            if(isTrue){
                primary_flag_arr.add(primary_flag);
            }
            primary_flag++;
        }
            for(int up=0;up<update_Attribution.size();up++){
                for(int pf:primary_flag_arr){
                    if(update_Attribution.get(up).equals(primarydata.getAlltable().getAttribute().get(pf)))
                        fit_flag++;
                }
            }
            if(fit_flag==primary_flag_arr.size()){
                isPrimary=true;
            }

            //多主键判断完成

                        //获取记录空间
                        String[] record = fileUtil.getCertainLineOfTxt(primarydata.getTablePath()+"/"+primarydata.getTableName()+".txt",9).split("");
                        for(int i=analysisUtil.getSYS_First(primarydata);i<=analysisUtil.getSYS_End(primarydata);i++) {
                            if (record[(i - 1)].equals("1")) {
                                String tableDateLine = fileUtil.getCertainLineOfTxt(primarydata.getTablePath() + "/" + primarydata.getTableName() + ".txt", (11 + i));
                                String[] tableDate = tableDateLine.split(",");
                                //封装
                                for (int j = 0; j < primarydata.getAlltable().getAttribute().size(); j++) {
                                    primarydata.getAlltable().getContent().set(j, tableDate[j]);
                                }
                                //已得到一个封装完成的tableContent，下个循环会覆盖Content
                                //开始判断条件成立
                                int where_succeed = 0;
                                for (int j = 0; j < primarydata.getAlltable().getAttribute().size(); j++) {
                                    for (int k = 0; k < where_Attribution.size(); k++) {
                                        System.out.println("数据表中的属性为：" + primarydata.getAlltable().getAttribute().get(j));
                                        System.out.println("条件判断的属性为：" + where_Attribution.get(k));
                                        if (primarydata.getAlltable().getAttribute().get(j).equals(where_Attribution.get(k))) {
                                            //如果相等，则判断Content是否匹配
                                            System.out.println("数据表中的内容为：" + primarydata.getAlltable().getContent().get(j));
                                            System.out.println("条件判断的内容为：" + where_Content.get(k));
                                            if (primarydata.getAlltable().getContent().get(j).equals(where_Content.get(k))) {
                                                //匹配成功，进行记录
                                                where_succeed++;
                                            }
                                        }
                                    }
                                }
                                //全部匹配一遍
                                System.out.println("需匹配次数:" + where_Attribution.size());
                                System.out.println("匹配成功次数:" + where_succeed);
                                if (where_succeed == where_Attribution.size()) {
                                    fit_line++;
                                    //行数封装进int 数组中
                                    update_line.add((11 + i));
                                }
                                if (fit_line == 2) {
                                    if (isPrimary) {
                                        System.out.println("修改的内容涉及到主键");
                                        System.out.println("拒绝访问");
                                        return null;
                                    }
                                }
                            }
                        }
        return update_line;
    }


}
