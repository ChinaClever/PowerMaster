package cn.iocoder.yudao.module.cabinet.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.cabinet.vo.EqBillConfigVo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.SPLIT;

/**
 * @author luowei
 * @version 1.0
 * @description: 时间处理工具类
 * @date 2024/4/17 15:50
 */
public class TimeUtil {

    public static EqBillConfigVo getTimeByDay(EqBillConfigVo configVo) {

        //获取当前时间
        DateTime end = DateTime.now();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        DateTime start = DateTime.of(calendar.getTime());
        //分段时间
        String[] times = configVo.getBillPeriod().split(SPLIT);


        String startDate = DateUtil.formatDate(start);
        String endDate = DateUtil.formatDate(end);

        String sTime1 = startDate + " " + times[0];
        String sTime2 = endDate + " " + times[0];

        String eTime1 = startDate + " " + times[1];
        String eTime2 = endDate + " " + times[1];


        if (belongCalendar(DateUtil.parseDateTime(sTime1).toJdkDate(), start.toJdkDate(), end.toJdkDate())
                && belongCalendar(DateUtil.parseDateTime(eTime1).toJdkDate(), start.toJdkDate(), end.toJdkDate())) {
            configVo.setStartTime(sTime1);
            configVo.setEndTime(eTime1);
        } else if (belongCalendar(DateUtil.parseDateTime(sTime2).toJdkDate(), start.toJdkDate(), end.toJdkDate())
                && belongCalendar(DateUtil.parseDateTime(eTime2).toJdkDate(), start.toJdkDate(), end.toJdkDate())) {
            configVo.setStartTime(sTime2);
            configVo.setEndTime(eTime2);
        }
        return configVo;

    }

    /**
     * 判断时间是否在时间段内
     *
     * @param nowTime
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }


    // 获得某天最大时间 2020-02-19 23:59:59
    public static Date getEndOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        ;
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    // 获得某天最小时间 2020-02-17 00:00:00
    public static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

}
