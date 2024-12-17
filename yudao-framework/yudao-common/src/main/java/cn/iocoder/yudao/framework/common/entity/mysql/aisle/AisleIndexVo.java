package cn.iocoder.yudao.framework.common.entity.mysql.aisle;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Schema(description = "管理后台 - 柜列配置 Request VO")
@Data
public class AisleIndexVo {
    /**
     * 机房id
     */
    @Schema(description = "柜列id", example = "2")
    private Integer id;
    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "机房1")
    private String aisleName;


    /**
     * 柜列方向 x y
     */
    private String direction;

    /**
     * 数据来源 0：PDU 1：母线
     */
    @Schema(description = "数据来源 0：PDU 1：母线", example = "0")
    private Boolean pduBar;

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
     * 电力容量
     */
    @Schema(description = "电力容量", example = "5")
    private float powerCapacity;

    /**
     * 柜列长度
     */
    @Schema(description = "柜列长度", example = "0")
    @JsonProperty(value="aisleLength")
    private int aisleLength;

    /**
     * 日用能告警开关
     */
    private  int eleAlarmDay;

    /**
     * 月用能告警开关
     */
    private  int eleAlarmMonth;

    /**
     * 日用能限制
     */
    private double eleLimitDay;

    /**
     * 月用能限制
     */
    private double eleLimitMonth;
}
