package cn.iocoder.yudao.module.statis.entity.es;

import lombok.Data;

/**
 * @author chenwany
 * @Date: 2024/3/28 14:47
 * @Description: pdu总电能表(实时)
 */
@Data
public class PduEleTotalRealtimeDo extends BaseDo {


    /**
     * 电能
     */
    private double ele;


}
