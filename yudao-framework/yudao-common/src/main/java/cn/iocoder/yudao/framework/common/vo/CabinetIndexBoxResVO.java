package cn.iocoder.yudao.framework.common.vo;

import lombok.Data;

@Data
public class CabinetIndexBoxResVO {

    private Integer id;


    /**
     * 机房编号
     */
    private int roomId;

    /**
     * 机柜名称
     */
    private String cabinetName;

    /**
     * 数据来源
     */
    private Boolean pduBox;

    /**
     * A路插接箱
     */
    private String boxKeyA;

    /**
     * B路插接箱
     */
    private String boxKeyB;

}
