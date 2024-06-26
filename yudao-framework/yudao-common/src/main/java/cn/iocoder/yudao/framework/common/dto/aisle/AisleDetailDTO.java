package cn.iocoder.yudao.framework.common.dto.aisle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列详情
 * @date 2024/6/21 15:49
 */
@Data
public class AisleDetailDTO {

    //柜列
    /**
     * 柜列iD
     */
    private Integer id;
    /**
     * 机房id
     */
    private Integer roomId;

    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 柜列名称
     */
    private String aisleName;

    /**
     * 数据来源 0：PDU 1：母线
     */
    private Integer pduBar;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 类型
     */
    private String type;

    /**
     * 起始x坐标
     */
    @JsonProperty(value="xCoordinate")
    private int xCoordinate;

    /**
     * 起始y坐标
     */
    @JsonProperty(value="yCoordinate")
    private int yCoordinate;

    /**
     * 柜列方向 x y
     */
    private String direction;

    //母线
    /**
     * A路母线
     */
    private AisleBarDTO barA;
    /**
     * B路母线
     */
    private AisleBarDTO barB;

    //机柜
    /**
     * 机柜数据
     */
    private List<AisleCabinetDTO> cabinetList;


}
