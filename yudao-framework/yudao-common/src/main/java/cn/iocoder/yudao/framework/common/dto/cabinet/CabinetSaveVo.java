package cn.iocoder.yudao.framework.common.dto.cabinet;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机柜新增/编辑 Request VO")
@Data
public class CabinetSaveVo {

    /**
     *  机柜id
     */
    @Schema(description = "机柜id", example = "2")
    private Integer id;

    /**
     * 机房编号
     */
    @Schema(description = "机房编号", example = "2")
    private int roomId;


    /**
     * 机柜名称
     */
    @Schema(description = "机柜名称", example = "机柜1")
    private String cabinetName;


    /**
     * 机柜高度
     */
    @Schema(description = "机柜高度", example = "5")
    private int cabinetHeight;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "5")
    private float powerCapacity;


    /**
     * X坐标
     */
    @Schema(description = "注释", example = "xxx")
    @JsonProperty(value="xCoordinate")
    private int xCoordinate;

    /**
     * Y坐标
     */
    @Schema(description = "类名", example = "yyy")
    @JsonProperty(value="yCoordinate")
    private int yCoordinate;


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
