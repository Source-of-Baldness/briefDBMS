package com.ui;

import com.manager.analysis.Select;
import com.util.AnalysisUtil;
import com.util.SmallBigChange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ManinUI {

    public static void main(String[] args) throws Exception {
        System.out.println("hello briefDBMS");
        while(true){
            System.out.println("1 选择数据库 当前数据:master");
            Scanner input = new Scanner(System.in);
            int choose = input.nextInt();
            System.out.println("查询界面：");
            //输入
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String sql = br.readLine();
            //全部转化为大写
            SmallBigChange sctc = new SmallBigChange();
            sql = sctc.toBigchar(sql);
            //跳转语法解析工具，定位语法类型
            AnalysisUtil au= new AnalysisUtil();
            au.grammarPositon(sql);
        }
    }
}
