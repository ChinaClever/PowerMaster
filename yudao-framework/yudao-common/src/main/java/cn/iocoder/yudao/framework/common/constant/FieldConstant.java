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

    /**
     * Redis设置key
     */
    public static final String REDIS_KEY_ROOM = "packet:room:";

    /**
     * ES补位
     */
    public static final String KEYWORD = ".keyword";

    /**
     * 机柜id
     */
    public static final String CABINET_ID = "cabinet_id";


    /**
     * 始端箱id
     */
    public static final String BUS_ID = "bus_id";

    /**
     * 插接箱id
     */
    public static final String BOX_ID = "box_id";


    /**
     * 机柜电量表（按天算）
     */
    public static final String CABINET_EQ_TOTAL_DAY = "cabinet_eq_total_day";


    /**
     * 始端箱总电能表（按天算
     */
    public static final String BUS_EQ_TOTAL_DAY = "bus_eq_total_day";

    /**
     * 插接箱输出位电能表（按天算）
     */
    public static final String BOX_EQ_OUTLET_DAY = "box_eq_outlet_day";


    public static final String REDIS_KEY_CABINET = "packet:cabinet:";

    /**
     * 始端箱数据
     */
    public static final String BUS_DATA = "bus_data";

    /**
     * 环境数据
     */
    public static final String ENV_ITEM_LIST = "env_item_list";
    /**
     * 温度数据
     */
    public static final String TEM_VALUE = "tem_value";

    /**
     * 相数据
     */
    public static final String LINE_ITEM_LIST = "line_item_list";

    /**
     * 负载率
     */
    public static final String LOAD_RATE = "load_rate";
    /**
     * 电流
     */
    public static final String CUR_VALUE = "cur_value";
    /**
     * 电压
     */
    public static final String VOL_VALUE = "vol_value";

    /**
     * 插接箱数据
     */
    public static final String BOX_DATA = "box_data";

    /**
     * 输出位数据
     */
    public static final String OUTLET_ITEM_LIST = "outlet_item_list";

    /**
     * 有功功率
     */
    public static final String POW_ACTIVE = "pow_active";

    /**
     * 视在功率
     */
    public static final String POW_APPARENT = "pow_apparent";

    /**
     * 无功功率
     */
    public static final String POW_REACTIVE = "pow_reactive";

    /**
     * 功率因素
     */
    public static final String POWER_FACTOR = "power_factor";

    /**
     * 负载率
     */
    public static final String LOAD_FACTOR = "load_factor";

    /**
     * 机柜数据
     */
    public static final String CABINET_POWER = "cabinet_power";
    /**
     * 机柜环境数据
     */
    public static final String CABINET_ENV = "cabinet_env";

    public static final String CABINET_KEY = "cabinet_key";
    /**
     * A路数据
     */
    public static final String PATH_A = "path_a";

    /**
     * B路数据
     */
    public static final String PATH_B = "path_b";

    /**
     * 总数据
     */
    public static final String TOTAL_DATA = "total_data";

    /**
     * 柜列数据
     */
    public static final String AISLE_POWER = "aisle_power";
    /**
     * 柜列id
     */
    public static final String AISLE_ID = "aisle_id";

    /**
     * 通道总电能表(实时)
     */
    public static final String AISLE_ELE_TOTAL_REALTIME = "aisle_ele_total_realtime";

    /**
     * 通道电量表（按天算）
     */
    public static final String AISLE_EQ_TOTAL_DAY = "aisle_eq_total_day";
    /**
     * 通道电量表（按周算）
     */
    public static final String AISLE_EQ_TOTAL_WEEK = "aisle_eq_total_week";

    /**
     * 通道电量表（按月算）
     */
    public static final String AISLE_EQ_TOTAL_MONTH = "aisle_eq_total_month";

    /**
     * 机房总电能表(实时)
     */
    public static final String ROOM_ELE_TOTAL_REALTIME = "room_ele_total_realtime";

    /**
     * 机房电量表（按天算）
     */
    public static final String ROOM_EQ_TOTAL_DAY = "room_eq_total_day";
    /**
     * 机房电量表（按周算）
     */
    public static final String ROOM_EQ_TOTAL_WEEK = "room_eq_total_week";

    /**
     * 机房电量表（按月算）
     */
    public static final String ROOM_EQ_TOTAL_MONTH = "room_eq_total_month";

    /**
     * 机房id
     */
    public static final String ROOM_ID = "room_id";

    public static final String ROOM_POWER = "room_power";

    public static final String ROOM_HDA_POW_REALTIME = "room_hda_pow_realtime";

    public static final String ROOM_HDA_POW_HOUR = "room_hda_pow_hour";

    public static final String ROOM_KEY = "room_key";

    /**
     * 聚合名称
     */
    public static final String BY_ROOM = "by_room";
}

