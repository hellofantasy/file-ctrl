package com.ccb.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static final String YYYYMMDDHHMM = "yyyyMMddHHmm";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDDHHMMss = "yyyyMMddHHmmss";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String getDateStr(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String currentDateStr = sdf.format(new Date());
        return currentDateStr;
    }

    public final static String formatDate2Str(Date inDate
    ) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sf.format(inDate);
    }

    public final static String formatDate2Str(Date inDate
            , String format) {
        if (inDate == null) {
            return null;
        }
        SimpleDateFormat sf = new SimpleDateFormat(format
        );
        return sf.format(inDate);
    }
}