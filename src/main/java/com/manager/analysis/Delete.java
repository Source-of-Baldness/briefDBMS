package com.manager.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Delete {
    public boolean baseAnalysis(String sql){
        System.out.println(sql);
        Pattern p = Pattern.compile("^[\\s]*SELECT[\\s]+([A-Z][A-Z]*[\\s]*,[\\s]*)*([A-Z][A-Z]*)[\\s]+FROM[\\s]+([A-Z][A-Z]*)[\\s]+WHERE[\\s]+([A-Z][A-Z]*=[^\\s]+[\\s]*(OR|AND)[\\s]*)*([A-Z][A-Z]*=[^\\s]+[\\s]*)[\\s]*$");
        Matcher m = p.matcher(sql);
        boolean result = m.matches();

        System.out.println(result);
        return result;
    }
}
