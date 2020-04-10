package com.manager.analysis;

import com.ui.ManinUI;

public class BreakConnect {
    public void baseAnalysis(String sql) throws Exception {
        System.out.println("已断开当前DBMS与客户端的连接");
        ManinUI.initSucceed=0;
        ManinUI maninUI = new ManinUI();
        maninUI.main(null);
    }
}
