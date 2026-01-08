package com.example.mylibrary;

import java.util.Locale;

public class TimeUtil {
    /**
     * 将毫秒转换为易读的字符串
     * @param durationMillis 毫秒数
     * @return 例如: "1小时20分", "45分钟", "未开始"
     */
    public static String formatDuration(long durationMillis) {
        if (durationMillis <= 0) return "未开始";

        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d小时%d分", hours, minutes);
        } else {
            return String.format(Locale.getDefault(), "%d分钟", minutes);
        }
    }
}