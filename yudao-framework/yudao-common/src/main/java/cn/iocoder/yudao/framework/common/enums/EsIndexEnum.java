package cn.iocoder.yudao.framework.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenwany
 * @Date: 2024/3/29 09:06
 * @Description: ES索引表
 */
@Getter
public enum EsIndexEnum {

    /**
     * pdu总历史数据表
     */
    PDU_HDA_TOTAL_REALTIME("pdu_hda_total_realtime","pdu总历史数据表（实时）"),
    /**
     * pdu相历史数据表
     */
    PDU_HDA_LINE_REALTIME("pdu_hda_line_realtime","pdu相历史数据表（实时）"),
    /**
     * pdu回路历史数据表
     */
    PDU_HDA_LOOP_REALTIME("pdu_hda_loop_realtime","pdu回路历史数据表（实时）"),
    /**
     * pdu输出位历史数据表
     */
    PDU_HDA_OUTLET_REALTIME("pdu_hda_outlet_realtime","pdu输出位历史数据表（实时）"),
    /**
     * pdu总电能表
     */
    PDU_ELE_TOTAL_REALTIME("pdu_ele_total_realtime","pdu总电能表(实时)"),
    /**
     * pdu环境表
     */
    PDU_ENV_REALTIME("pdu_env_realtime","pdu环境表（实时）"),
    /**
     * pdu总历史数据表（按小时算）
     */
    PDU_HDA_TOTAL_HOUR("pdu_hda_total_hour","pdu总历史数据表（按小时算）"),
    /**
     * pdu总历史数据表（按天算）
     */
    PDU_HDA_TOTAL_DAY("pdu_hda_total_day","pdu总历史数据表（按天算）"),
    /**
     * pdu相历史数据表（按小时算）
     */
    PDU_HDA_LINE_HOUR("pdu_hda_line_hour","pdu相历史数据表（按小时算）"),
    /**
     * pdu相历史数据表（按天算）
     */
    PDU_HDA_LINE_DAY("pdu_hda_total_day","pdu相历史数据表（按天算）"),
    /**
     * pdu回路历史数据表（按小时算）
     */
    PDU_HDA_LOOP_HOUR("pdu_hda_loop_hour","pdu回路历史数据表（按小时算）"),
    /**
     * pdu回路历史数据表（按天算）
     */
    PDU_HDA_LOOP_DAY("pdu_hda_total_day","pdu回路历史数据表（按天算）"),
    /**
     * pdu输出位历史数据表（按小时算）
     */
    PDU_HDA_OUTLET_HOUR("pdu_hda_outlet_hour","pdu输出位历史数据表（按小时算）"),
    /**
     * pdu输出位历史数据表（按天算）
     */
    PDU_HDA_OUTLET_DAY("pdu_hda_outlet_day","pdu输出位历史数据表（按天算）"),
    /**
     * pdu环境数据表（按小时算）
     */
    PDU_ENV_HOUR("pdu_env_hour","pdu环境数据表（按小时算）"),
    /**
     * pdu环境数据表（按天算）
     */
    PDU_ENV_DAY("pdu_env_day","pdu环境数据表（按天算）"),

    ;
    /**
     * es索引
     */
    private final String index;

    /**
     * 索引描述
     */
    private final String desc;


    EsIndexEnum(String index, String desc) {
        this.index = index;
        this.desc = desc;

    }


    /**
     * 根据索引表获取枚举类
     * @param index 索引
     */
    public static EsIndexEnum getEnumByIndex(String index) {
        EsIndexEnum[] enums = EsIndexEnum.values();
        for (EsIndexEnum indexEnum : enums){
            if (indexEnum.index.equals(index)){
                return indexEnum;
            }
        }
        return null;
    }


    /**
     * 获取所有索引值
     */
    public static List<String> indexs() {
        return Arrays.stream(EsIndexEnum.values()).map(EsIndexEnum::getIndex).collect(Collectors.toList());
    }
}
