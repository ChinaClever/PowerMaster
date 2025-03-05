package cn.iocoder.yudao.framework.common.dto.aisle;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetAisleVO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
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
public class AisleDetailOldDTO {

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
    private Boolean pduBar;

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
    @JsonProperty(value="xCoordinate")
    private int xCoordinate;

    /**
     * 起始y坐标
     */
    @Schema(description = "起始y坐标")
    @JsonProperty(value="yCoordinate")
    private int yCoordinate;

    /**
     * 柜列方向 x y
     */
    @Schema(description = "柜列方向 x y")
    private String direction;

    //母线
    /**
     * A路母线
     */
    @Schema(description = "A路母线")
    private AisleBarDTO barA;
    /**
     * B路母线
     */
    @Schema(description = "B路母线")
    private AisleBarDTO barB;

    //机柜
    /**
     * 机柜数据
     */
    @Schema(description = "机柜数据")
    private List<CabinetDTO> cabinetList;

    /**
     * 柜列数据包
     */
    @Schema(description = "柜列数据包")
    private JSONObject aisleData;


    /**
     * 昨日电量
     */

    @Schema(description = "昨日电量", example = "1")
    private Double yesterdayEq;


    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "5")
    private float powerCapacity;


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


    /**
     * 总空间
     */
    @Schema(description = "总空间")
    private Integer totalSpace;


    /**
     * 已用空间
     */
    @Schema(description = "已用空间", example = "1")
    private int usedSpace;

    /**
     * 剩余空间
     */
    @Schema(description = "剩余空间", example = "1")
    private int freeSpace;

}
