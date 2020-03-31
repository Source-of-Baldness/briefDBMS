package com.manager.data;

import com.ui.ManinUI;
import com.util.FileUtil;
import com.util.TimeUtil;
import net.sf.json.JSONObject;

import java.io.IOException;

public class LogRecord {
    //写入日志操作
    public void writeLog(String sql) throws IOException {
        FileUtil fileUtil= new FileUtil();
        fileUtil.writeToFile(sql, ManinUI.currentDatabase.getFilename()+"/"+ManinUI.currentDatabase.getName()+"_LOG.txt");
    }
}

