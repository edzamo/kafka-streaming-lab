package com.example.banking.services.enums;

public enum FormatDateEnum {

    yyyy_MM_dd_T_HH_mm_ss_Z("yyyy-MM-dd'T'HH:mm:ss'Z'"),
    yyyyMMdd("yyyyMMdd"),
    yyyy_MM_dd_T_HH_mm("yyyy-MM-dd'T'HH:mm");

    private String name;

    FormatDateEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
