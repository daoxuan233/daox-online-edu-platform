package com.daox.online.uilts;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

    /**
     * 将Date无损转换为LocalDateTime
     *
     * @param date java.util.Date对象
     * @return 转换后的LocalDateTime对象
     */
    public static LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 将LocalDateTime无损转换为Date
     * @param localDateTime LocalDateTime对象
     * @return 转换后的Date对象
     */
    public static Date convertToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
