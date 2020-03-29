package com.pojo;

import java.util.List;

public class Table {
    private List<String> attribute;//表的属性
    private List<String> datatype;//数据类型
    private List<Boolean> isNull;//是否为空
    private List<Boolean> isPrimary;//是否为主键
    private String content;//表的内容

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getAttribute() {
        return attribute;
    }

    public void setAttribute(List<String> attribute) {
        this.attribute = attribute;
    }

    public List<String> getDatatype() {
        return datatype;
    }

    public void setDatatype(List<String> datatype) {
        this.datatype = datatype;
    }

    public List<Boolean> getIsNull() {
        return isNull;
    }

    public void setIsNull(List<Boolean> isNull) {
        this.isNull = isNull;
    }

    public List<Boolean> getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(List<Boolean> isPrimary) {
        this.isPrimary = isPrimary;
    }
}
