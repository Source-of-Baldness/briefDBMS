package com.manager.analysis;


import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pojo.Primarydata;
import com.pojo.Table;
import com.util.FileUtil;
import com.ui.ManinUI;
import com.util.AnalysisUtil;
import net.sf.json.JSONObject;



public class Select {
    //传入sql语句，进行解析语法
    public void baseAnalysis(String sql) throws IOException {
        System.out.println("二次正则判断");
        //实例化工具类
        AnalysisUtil au=new AnalysisUtil();
        FileUtil fu=new FileUtil();
        boolean result=false;
        //判断输入语句是否符合语法规则

        //1.无筛选条件（查找全表时）
        Pattern p=Pattern.compile("^[\\s]*SELECT[\\s]+(\\*)[\\s]+(FROM)[\\s]+[A-Z](\\w)*[\\s]*$");
        Matcher m=p.matcher(sql);
        result=m.matches();
        System.out.println(result);
        //上式为真 继续判断 判断库中是否含有该表
        String tableName=getSqlTableName(sql);
        tableName=tableName.trim();
        String filePath=ManinUI.currentDatabase.getFilename();
        //filePath=filePath.trim();
        boolean tableBoolean=au.isHaveTheTable(tableName,filePath);
        if(tableBoolean==true)//输入表名正确
        {
            result=false;//重置判断标志
            System.out.println("正在查找");
            //判断查找的类型 全表？有无限定词？等
            //1.无限定条件查找全表
            p=Pattern.compile("[\\s]*SELECT[\\s]+(\\*)[\\s]+(FROM)[\\s]+[A-Z](\\w)*");
            m=p.matcher(sql);
            result=m.matches();
            if(result)
            {
                //直接读取全表数据
                selectAllData(tableName,filePath);
               // System.out.println("-----下面是找到的数据-----");
                    //System.out.println(data);
            }
        }
        else
        {
            System.out.println("此数据库不含该表！");
        }
    }

    //获取sql语句中的表名字段
    public String getSqlTableName(String sql){
        String tableName="",tn="";
        char t = 't';
        String[] str=new String[sql.length()];
        int i=0;
        for(i=sql.length()-1;i>=0;i--)
        {
            if(t!=' ')
            {
                t=sql.charAt(i);
                tableName+=t;//获得了一个倒序的表名
                //System.out.println(t);
            }
        }
        tn=tableName;
        tableName="";
        for(i=tn.length()-1;i>=0;i--)
        {
            t=tn.charAt(i);
            tableName+=t;
            //System.out.println(t);
        }
        return tableName;
    }

    //查找表中的所有数据（不含条件符） 参数一待查找的表名 filePath全局变量中的当前数据库位置
    public void selectAllData(String tableName,String filePath) throws IOException {
        FileUtil fileUtil= new FileUtil();
        AnalysisUtil analysisUtil = new AnalysisUtil();
        Primarydata primarydata = new Primarydata();
        int selctline=0;
        ArrayList<String> lines = new ArrayList<String>();
        lines = fileUtil.getlContentLineOfTxt(filePath+"/TABLE/"+tableName+".txt","SYS_TITLE_RECORD_BEGIN","SYS_TITLE_RECORD_END");
        for(String line:lines){
            String[] str = line.split("");

            for(int i=0;i<str.length;i++){
                if(str[i].equals("1")){
                    selctline=i+1;
                    String selctinfo = fileUtil.getCertainLineOfTxt(filePath+"/TABLE/"+tableName+".txt",(11+selctline));
                    primarydata =analysisUtil.getTableStruct(tableName,filePath,ManinUI.currentDatabase.getName());
                    String[] si=selctinfo.split(",");
                    for (String sss:si){
                        System.out.println(sss);
                    }
                    for(int j=0;j<primarydata.getAlltable().getAttribute().size();j++){

                        primarydata.getAlltable().getContent().add("1");
                    }
                    JSONObject json = JSONObject.fromObject(primarydata);
                    String str22 = json.toString();
                    System.out.println(str22);


                    System.out.println();

                }
            }
        }
        // 开始读取数据

//        //补全路径
//        filePath+="\\TABLE\\"+tableName+".txt";
//        filePath=filePath.trim();
//        System.out.println(filePath);
//        BufferedReader br = new BufferedReader(new FileReader(filePath));
//        String data = null;
//        ArrayList<String> datas =new ArrayList<String>();
//        boolean result=false;//正则判断符
//        String temp1,temp2;//存放临时读到的需要判断的成对的文件标识字符串
//        int flag=0;//判断匹配的语句位置 避免一个正则多次判断
//        Pattern p;
//        Matcher m;
//
//        while ((data = br.readLine()) != null)
//        {
//            p=Pattern.compile("^[\\s]*SYS_First@@[\\d]+[\\s]*$");
//            m=p.matcher(data);
//            result=m.matches();
//            if(result){
//                p=Pattern.compile("^[\\s]*SYS_First@@0[\\s]*$");
//                m=p.matcher(data);
//                result=m.matches();
//                temp1=data;//存放First数据
//                System.out.println("here");
//                if(result)
//                {
//                    System.out.println("first=0");
//                    //继续往下匹配
//                    if((data=br.readLine())!=null)
//                    {
//                        result=false;//重置
//                        p=Pattern.compile("^[\\s]*SYS_End@@0[\\s]*$");
//                        m=p.matcher(data);
//                        result=m.matches();
//                        System.out.println("end=0");
//                        if(result)
//                            return datas;//表中无数据 直接返回空数组
//                        else
//                            return null;//错误的文件数据 返回空数组
//                    }
//                }
//                else//first不等于0 表中有数据
//                {
//                    System.out.println("first!=0");
//                    temp2=data=br.readLine();
//                   System.out.println("temp2"+temp2);
//                    System.out.println("temp1"+temp1);
//                    while((data=br.readLine())!=null)
//                    {
//                        p=Pattern.compile("^[\\s]*SYS_TITLE_DATA_BEGIN[\\s]*$");
//                        result=false;//重置
////                        p=Pattern.compile("^[\\s]*SYS_First@@[1-9]+[\\s]*$");
//                        m=p.matcher(data);
//                        result=m.matches();
//                        if(result)
//                        {
//                            //判断First和Ended是否相等 相等 数据表中仅有一条数据 直接定位
//                            String num1="",temp1_num1="",temp2_num2="";//两个变量用于存储数据行位置
//                            int num,temp1_num,temp2_num;//num标记数据位 temp1_num存储temp1 first数据位 2 end数据位
//
//                            int i=temp2.lastIndexOf("@");
//                            System.out.println("i="+i);
//                            exit(-1);
//                            for(int s=i+2;s<temp2.length();s++)
//                            {
//                                num1+=temp2.charAt(s);
//                                System.out.println(num1);
//                            }
//                            System.out.println(num1);
//                            num=Integer.parseInt(num1);
//                            temp2_num=num;
//
//                            i=temp1.lastIndexOf("@");
//                            System.out.println("i="+i);
//                            for(int s=i+2;s<temp1.length();s++)
//                            {
//                                temp1_num1+=temp1.charAt(s);
//                            }
//                            temp1_num=Integer.parseInt(temp1_num1);
//
//                            System.out.println("temp1_num"+temp1_num);
//                            System.out.println("temp2_num"+temp2_num);
//
//                            if(temp1.equals(temp2))
//                            {
//                                System.out.println("比较成功");
//
//                                while((data=br.readLine())=="SYS_TITLE_DATA_BEGIN"){
//                                    //添加一个计数器 用于读取br移动次数
//                                    int count=0;
//                                    while((data=br.readLine())!=null && count!=num)
//                                    {
//                                        count++;
//                                        br.readLine();
//
//                                    }
//                                    System.out.println("循环"+data);
//                                    data=br.readLine();
//                                    System.out.println("下一行"+data);
//                                    //替换data中存在的ffff标识符
//                                    p=Pattern.compile(",(f)+");
//                                    m=p.matcher(data);
//                                    String data1=m.replaceAll("");
//                                    System.out.println("替换后"+data1);
//                                    System.out.println("替换前"+data);
//                                    datas.add(data1);
//                                    return datas;
//                                }
//                        }
//
//
//                        }
//                    }
//
//            }
//
//                }
//            }
//        br.close();
//        return datas;
    }

}
