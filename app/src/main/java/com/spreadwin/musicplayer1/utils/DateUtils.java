package com.spreadwin.musicplayer1.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 作者：lixiang on 2016/1/8 13:45
 * 邮箱：xiang.li@spreadwin.com
 */
public class DateUtils {
    // 将毫秒转化为时间
    public static String getTime(int time) {
        Date date = new Date();// 获取当前时间
        SimpleDateFormat hms = new SimpleDateFormat("mm:ss");
        date.setTime(-8 * 60 * 60 * 1000 + time);
        String data = hms.format(date);
        return data;
    }
}
