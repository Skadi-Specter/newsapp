package com.example.newsapp.utils;

import java.util.Date;

public class TimeUtils {

    private static final long MINUTE = 60 * 1000;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long MONTH = 30 * DAY;
    private static final long YEAR = 12 * MONTH;

    /**
     * 获取一个友好的时间跨度显示
     * @param millis 毫秒时间戳
     * @return 友好的时间字符串
     */
    public static String getFriendlyTimeSpanByNow(long millis) {
        long now = System.currentTimeMillis();
        long span = now - millis;

        if (span < 0) {
            return String.format("%tF %tT", new Date(millis), new Date(millis));
        }
        if (span < MINUTE) {
            return "刚刚";
        } else if (span < HOUR) {
            return String.format("%d分钟前", span / MINUTE);
        } else if (span < DAY) {
            return String.format("%d小时前", span / HOUR);
        } else if (span < MONTH) {
            return String.format("%d天前", span / DAY);
        } else if (span < YEAR) {
            return String.format("%d个月前", span / MONTH);
        } else {
            return String.format("%d年前", span / YEAR);
        }
    }
} 