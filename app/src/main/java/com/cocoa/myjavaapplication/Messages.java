package com.cocoa.myjavaapplication;

public class Messages {
    public static String get(String str, Object args){
        return String.format(str, args);
    }
    public static String get(String str){
        return String.format(str);
    }
}
