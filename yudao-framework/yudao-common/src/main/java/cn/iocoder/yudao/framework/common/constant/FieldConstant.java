package cn.iocoder.yudao.framework.common.constant;

/**
 * @author chenwany
 * @Date: 2024/3/27 17:26
 * @Description: 字段常量
 */
public class FieldConstant {


    /**
     * 设备唯一标识
     */
    public static final String DEV_KEY = "dev_key";

    /**
     * 接收数据系统时间
     */
    public static final String SYS_TIME = "sys_time";

    /**
     * pdu
     */
    public static final String PDU_ID = "pdu_id";

    /**
     * 回路
     */
    public static final String LOOP_ID = "loop_id";

    /**
     * 相
     */
    public static final String LINE_ID = "line_id";

    /**
     * 输出位
     */
    public static final String OUTLET_ID = "outlet_id";


    /**
     * 传感器
     */
    public static final String SENSOR_ID = "sensor_id";

    /**
     * 创建时间
     */
    public static final String CREATE_TIME = "create_time";


    /**
     * 电压
     */
    public static final String VOL = "vol_value";

    /**
     * 电流
     */
    public static final String CUR = "cur_value";

    /**
     * 温度
     */
    public static final String TEM = "tem_value";

    /**
     * 湿度
     */
    public static final String HUM = "hum_value";


    /**
     * 有功功率
     */
    public static final String ACTIVE_POW = "pow_active";

    /**
     * 视在功率
     */
    public static final String APPARENT_POW = "pow_apparent";


    /**
     * 平均电压
     */
    public static final String VOL_AVG_VALUE = "vol_avg_value";

    /**
     * 最大电压
     */
    public static final String VOL_MAX_VALUE = "vol_max_value";

    /**
     * 最小电压
     */
    public static final String VOL_MIN_VALUE = "vol_min_value";

    /**
     * 最大电压时间
     */
    public static final String VOL_MAX_TIME = "vol_max_time";

    /**
     * 最小电压时间
     */
    public static final String VOL_MIN_TIME = "vol_min_time";

    /**
     * 平均电流
     */
    public static final String CUR_AVG_VALUE = "cur_avg_value";

    /**
     * 最大电流
     */
    public static final String CUR_MAX_VALUE = "cur_max_value";

    /**
     * 最小电流
     */
    public static final String CUR_MIN_VALUE = "cur_min_value";


    /**
     * 最大电流时间
     */
    public static final String CUR_MAX_TIME = "cur_max_time";

    /**
     * 最小电流时间
     */
    public static final String CUR_MIN_TIME = "cur_min_time";

    /**
     * 平均有功功率
     */
    public static final String ACTIVE_POW_AVG_VALUE = "pow_active_avg_value";

    /**
     * 最大有功功率
     */
    public static final String ACTIVE_POW_MAX_VALUE = "pow_active_max_value";

    /**
     * 最小有功功率
     */
    public static final String ACTIVE_POW_MIN_VALUE = "pow_active_min_value";

    /**
     * 最大有功功率时间
     */
    public static final String ACTIVE_POW_MAX_TIME = "pow_active_max_time";

    /**
     * 最小有功功率时间
     */
    public static final String ACTIVE_POW_MIN_TIME = "pow_active_min_time";

    /**
     * 平均视在功率
     */
    public static final String APPARENT_POW_AVG_VALUE = "pow_apparent_avg_value";

    /**
     * 最大视在功率
     */
    public static final String APPARENT_POW_MAX_VALUE = "pow_apparent_max_value";

    /**
     * 最小视在功率
     */
    public static final String APPARENT_POW_MIN_VALUE = "pow_apparent_min_value";

    /**
     * 最大视在功率时间
     */
    public static final String APPARENT_POW_MAX_TIME = "pow_apparent_max_time";

    /**
     * 最小视在功率时间
     */
    public static final String APPARENT_POW_MIN_TIME = "pow_apparent_min_time";

    /**
     * 平均温度
     */
    public static final String TEM_AVG_VALUE = "tem_avg_value";

    /**
     * 最大温度
     */
    public static final String TEM_MAX_VALUE = "tem_max_value";
    /**
     * 最大温度时间
     */
    public static final String TEM_MAX_TIME = "tem_max_time";

    /**
     * 最低温度
     */
    public static final String TEM_MIN_VALUE = "tem_min_value";

    /**
     * 最低温度时间
     */
    public static final String TEM_MIN_TIME = "tem_min_time";
    /**
     * 平均湿度
     */
    public static final String HUM_AVG_VALUE = "hum_avg_value";

    /**
     * 最高湿度
     */
    public static final String HUM_MAX_VALUE = "hum_max_value";

    /**
     * 最高湿度时间
     */
    public static final String HUM_MAX_TIME = "hum_max_time";

    /**
     * 最低湿度
     */
    public static final String HUM_MIN_VALUE = "hum_min_value";

    /**
     * 最低湿度时间
     */
    public static final String HUM_MIN_TIME = "hum_min_time";

    /**
     * 电量
     */
    public static final String EQ_VALUE = "eq_value";

    /**
     * 电费
     */
    public static final String BILL_VALUE = "bill_value";

    /**
     * 计费方式
     */
    public static final String BILL_MODE = "bill_mode";

    /**
     * 计费时间段
     */
    public static final String BILL_PERIOD = "bill_period";

    /**
     * 开始电能
     */
    public static final String START_ELE = "start_ele";

    /**
     * 结束电能
     */
    public static final String END_ELE = "end_ele";


    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_PDU = "packet:pdu:";

    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_BUS = "packet:bus:";


    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_BOX = "packet:box:";

    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_AISLE = "packet:aisle:";

    /**
     * 分隔符
     */
    public static final String SPLIT_KEY_BUS = "_";

    /**
     * 分隔符
     */
    public static final String SPLIT_KEY = "-";

    /**
     * 聚合
     */
    public static final String BY_TIME = "by_time";

    /**
     * es聚合长度
     */
    public static final Integer BUCKET_SIZE = 50000;

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
}

