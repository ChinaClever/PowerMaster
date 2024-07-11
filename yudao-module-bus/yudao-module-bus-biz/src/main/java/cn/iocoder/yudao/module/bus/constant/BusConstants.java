package cn.iocoder.yudao.module.bus.constant;

/**
 * @author luowei
 * @version 1.0
 * @description: 常量类
 * @date 2024/4/15 9:40
 */
public class BusConstants {

    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_BUS = "packet:bus:";
    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_PDU = "packet:始端箱:";

    /**
     * 分隔符
     */
    public static final String SPLIT_KEY = "-";


    /**
     * 分隔符
     */
    public static final String SPLIT = "-";

    /**
     * ES补位
     */
    public static final String KEYWORD = ".keyword";

    /**
     * outlet聚合名称
     */
    public static final String BY_OUTLET = "by_outlet";

    /**
     * 始端箱聚合名称
     */
    public static final String BY_PDU = "by_始端箱";

    /**
     * line聚合名称
     */
    public static final String BY_LINE = "by_line";


    /**
     * loop聚合名称
     */
    public static final String BY_LOOP = "by_loop";

    /**
     * 传感器
     */
    public static final String BY_SENSOR = "by_sensor";

    public static final String STATIS_CONFIG = "statisConfig";

    /**
     * 数据包字段
     */
    public static final String BUS_POWER = "bus_power";

    /**
     * 数据包字段
     */
    public static final String PATH_A = "path_a";


    /**
     * 数据包字段
     */
    public static final String PATH_B = "path_b";

    /**
     * 数据包字段
     */
    public static final String TOTAL_DATA = "total_data";

    /**
     * 有功电能
     */
    public static final String ELE_ACTIVE = "ele_active";

    /**
     * 有功功率
     */
    public static final String POW_ACTIVE = "pow_active";

    /**
     * 视在功率
     */
    public static final String POW_APPARENT = "pow_apparent";


    /**
     * 聚合名称
     */
    public static final String BY_BUS = "by_bus";
    /**
     * 公司
     */
    public static final String COMPANY = "company";


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
    /**
     * 始端箱id
     */
    public static final String BUS_ID = "bus_id";

    /**
     * 始端箱功率历史数据表（实时）
     */
    public static final String BUS_HDA_POW_REALTIME = "bus_hda_pow_realtime";

    /**
     * 始端箱功率历史数据表（按小时算）
     */
    public static final String BUS_HDA_POW_HOUR = "bus_hda_pow_hour";

    /**
     * 始端箱功率历史数据表（按天算）
     */
    public static final String BUS_HDA_POW_DAY = "bus_hda_pow_day";

    /**
     * 始端箱总电能表(实时)
     */
    public static final String BUS_ELE_TOTAL_REALTIME = "bus_ele_total_realtime";

    /**
     * 始端箱电量表（按天算）
     */
    public static final String BUS_EQ_TOTAL_DAY = "bus_eq_total_day";

    /**
     * 始端箱电量表（按周算）
     */
    public static final String BUS_EQ_TOTAL_WEEK = "bus_eq_total_week";

    /**
     * 始端箱电量表（按月算）
     */
    public static final String BUS_EQ_TOTAL_MONTH = "bus_eq_total_month";

    /**
     * 始端箱相历史数据表（按小时算
     */
    public static final String BUS_HDA_LINE_HOUR = "bus_hda_line_hour" ;
    
    /**
     * 始端箱相历史数据表（按小时算
     */
    public static final String BUS_HDA_LINE_DAY = "bus_hda_line_day";



    /**
     * 数据包
     */
    public static final String PDU_DATA = "始端箱_data";

    /**
     * 输出位数据
     */
    public static final String OUTPUT_ITEM_LIST = "output_item_list";

    /**
     * 相电流
     */
    public static final String CUR_VALUE = "cur_value";


    /**
     * 设备的ip地址
     */
    public static final String DEV_IP = "dev_ip";

    /**
     * 始端箱名称
     */
    public static final String BUS_NAME = "bus_name";

    /**
     * 插接箱名称
     */
    public static final String BOX_NAME = "box_name";

    /**
     * 分隔符
     */
    public static final String SPLIT_KEY_BUS = "_";

    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_BOX = "packet:box:";


}
