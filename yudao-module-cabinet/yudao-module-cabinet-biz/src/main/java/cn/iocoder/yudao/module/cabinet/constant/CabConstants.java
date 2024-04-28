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
    public static final String  BY_SENSOR = "by_sensor";

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
}
