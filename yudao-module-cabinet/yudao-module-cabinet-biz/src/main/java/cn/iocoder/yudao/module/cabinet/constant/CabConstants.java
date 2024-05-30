package cn.iocoder.yudao.module.cabinet.constant;

/**
 * @author luowei
 * @version 1.0
 * @description: 常量类
 * @date 2024/4/15 9:40
 */
public class CabConstants {

    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_CABINET = "packet:cabinet:";
    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_PDU = "packet:pdu:";

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
     * pdu聚合名称
     */
    public static final String BY_PDU = "by_pdu";

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
    public static final String CABINET_POWER = "cabinet_power";

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
    public static final String BY_CABINET = "by_cabinet";
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
     * 机柜id
     */
    public static final String CABINET_ID = "cabinet_id";

    /**
     * 机柜功率历史数据表（实时）
     */
    public static final String CABINET_HDA_POW_REALTIME = "cabinet_hda_pow_realtime";

    /**
     * 机柜功率历史数据表（按小时算）
     */
    public static final String CABINET_HDA_POW_HOUR = "cabinet_hda_pow_hour";

    /**
     * 机柜功率历史数据表（按天算）
     */
    public static final String CABINET_HDA_POW_DAY = "cabinet_hda_pow_day";

    /**
     * 机柜总电能表(实时)
     */
    public static final String CABINET_ELE_TOTAL_REALTIME = "cabinet_ele_total_realtime";

    /**
     * 机柜电量表（按天算）
     */
    public static final String CABINET_EQ_TOTAL_DAY = "cabinet_eq_total_day";

    /**
     * 机柜电量表（按周算）
     */
    public static final String CABINET_EQ_TOTAL_WEEK = "cabinet_eq_total_week";

    /**
     * 机柜电量表（按月算）
     */
    public static final String CABINET_EQ_TOTAL_MONTH = "cabinet_eq_total_month";

    /**
     * pdu相历史数据表（按小时算
     */
    public static final String PDU_HDA_LINE_HOUR = "pdu_hda_line_hour" ;

    /**
     * 数据包
     */
    public static final String PDU_DATA = "pdu_data";

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
