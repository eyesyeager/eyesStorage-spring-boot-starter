package io.github.eyesyeager.eyesStorageStarter.utils;

import io.github.eyesyeager.eyesStorageStarter.context.ConfigContext;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * 日期工具类
 * @author artonyu
 * @date 2024-11-09 11:47
 */

public class DateUtils {

    /**
     * 时间戳 转 ZonedDateTime
     * @param timestamp 时间戳
     * @return ZonedDateTime
     */
    public static ZonedDateTime timestampToZonedDateTime(Long timestamp) {
        Timestamp stamp = new Timestamp(timestamp);
        return stamp.toLocalDateTime().atZone(ZoneId.of(ConfigContext.ZONE_ID));
    }

    /**
     * Date 转 ZonedDateTime
     * @param date Date
     * @return ZonedDateTime
     */
    public static ZonedDateTime dateToZonedDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.of(ConfigContext.ZONE_ID));
    }
}
