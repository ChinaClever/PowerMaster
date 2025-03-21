package cn.iocoder.yudao.framework.common.dto.room;

import cn.iocoder.yudao.framework.common.dto.room.RoomCabinetDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列详情
 * @date 2024/6/21 15:49
 */
@Data
public class AisleDataDTO {

    //柜列
    /**
     * 柜列iD
     */
    @Schema(description = "柜列iD")
    private Integer id;
    /**
     * 机房id
     */
    @Schema(description = "机房id")
    private Integer roomId;

    /**
     * 机房名称
     */
    @Schema(description = "机房名称")
    private String roomName;

    /**
     * 柜列名称
     */
    @Schema(description = "柜列名称")
    private String aisleName;

    /**
     * 数据来源 0：PDU 1：母线
     */
    @Schema(description = "数据来源 0：PDU 1：母线")
    private Integer pduBar;

    /**
     * 长度
     */
    @Schema(description = "长度")
    private Integer length;

    /**
     * 类型
     */
    @Schema(description = "类型")
    private String type;

    /**
     * 起始x坐标
     */
    @Schema(description = "起始x坐标")
    @JsonProperty(value = "xCoordinate")
    private int xCoordinate;

    /**
     * 起始y坐标
     */
    @Schema(description = "起始y坐标")
    @JsonProperty(value = "yCoordinate")
    private int yCoordinate;

    /**
     * 柜列方向 x y
     */
    @Schema(description = "柜列方向 x y")
    private String direction;

    @Schema(description = "额定容量")
    private Double powerCapacity;

    /**
     * 日用能告警开关
     */
    @Schema(description = "日用能告警开关")
    private  int eleAlarmDay;

    /**
     * 月用能告警开关
     */
    @Schema(description = "月用能告警开关")
    private  int eleAlarmMonth;

    /**
     * 日用能限制
     */
    @Schema(description = "日用能限制")
    private double eleLimitDay;

    /**
     * 月用能限制
     */
    @Schema(description = "月用能限制")
    private double eleLimitMonth;

    @Schema(description = "总视在功率")
    private Double powApparentTotal;

    @Schema(description = "总有功功率")
    private Double powActiveTotal;

    @Schema(description = "总无功功率")
    private Double powReactiveTotal;

    @Schema(description = "A路视在功率")
    private Double powApparentA;

    @Schema(description = "A路有功功率")
    private Double powActiveA;

    @Schema(description = "A路无功功率")
    private Double powReactiveA;

    @Schema(description = "B路视在功率")
    private Double powApparentB;

    @Schema(description = "B路有功功率")
    private Double powActiveB;

    @Schema(description = "B路无功功率")
    private Double powReactiveB;

    @Schema(description = "A路电流")
    private List<Double> curAList;

    @Schema(description = "B路电流")
    private List<Double> curBList;

    @Schema(description = "A路电压")
    private List<Double> volAList;

    @Schema(description = "B路电压")
    private List<Double> volBList;

    @Schema(description = "B路功率因素")
    private Double powerFactorA;

    @Schema(description = "B路功率因素")
    private Double powerFactorB;

    @Schema(description = "功率因素")
    private Double powerFactor;


    @Schema(description = "A路功率")
    private List<Double> powValueAList;

    @Schema(description = "B路功率")
    private List<Double>  powValueBList;

    @Schema(description = "B路有功电能")
    private Double eleActiveB;

    @Schema(description = "A路有功电能")
    private Double eleActiveA;

    @Schema(description = "总有功电能")
    private Double eleActiveTotal;

    /**
     * 昨日电量
     */

    @Schema(description = "昨日电量", example = "1")
    private Double yesterdayEq;

    //机柜
    /**
     * 机柜数据
     */
    @Schema(description = "机柜数据")
    private List<RoomCabinetDTO> cabinetList;


}
