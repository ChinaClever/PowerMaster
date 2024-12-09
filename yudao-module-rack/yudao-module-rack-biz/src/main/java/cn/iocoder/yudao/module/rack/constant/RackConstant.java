package cn.iocoder.yudao.module.rack.constant;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架常量
 * @date 2024/5/27 14:13
 */
public class RackConstant {

    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_RACK = "packet:rack:";

    /**
     * 分隔符
     */
    public static final String SPLIT_KEY = "-";

    /**
     * ES补位
     */
    public static final String KEYWORD = ".keyword";

    /**
     * 聚合名称
     */
    public static final String BY_RACK = "by_rack";


    /**
     * 机架id
     */
    public static final String RACK_ID = "rack_id";


    /**
     * 机架功率历史数据表（按小时算）
     */
    public static final String RACK_HDA_POW_HOUR = "rack_hda_pow_hour";


    /**
     * 机架功率历史数据表（按天算）
     */
    public static final String RACK_HDA_POW_DAY = "rack_hda_pow_day";

    /**
     * 机架功率历史数据表（实时）
     */
    public static final String RACK_HDA_POW_REALTIME = "rack_hda_pow_realtime";


    public static final String RACK_ELE_TOTAL_REALTIME = "rack_ele_total_realtime";

    public static final String RACK_EQ_TOTAL_DAY = "rack_eq_total_day";

    public static final String RACK_EQ_TOTAL_MONTH = "rack_eq_total_month";

    public static final String RACK_EQ_TOTAL_WEEK = "rack_eq_total_week";


    /**
     * 小时
     */
    public static final String HOUR = "HOUR";

    /**
     * 天
     */
    public static final String DAY = "DAY";


    /**
     * 周
     */
    public static final String WEEK = "WEEK";

    /**
     * 月
     */
    public static final String MONTH = "MONTH";

}
