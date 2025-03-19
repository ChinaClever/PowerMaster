package cn.iocoder.yudao.framework.common.entity.mysql.room;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房新增修改实体
 * @date 2024/4/29 15:00
 */
@Schema(description = "管理后台 - 机房新增/编辑 Request VO")
@Data
public class RoomSavesVo {

    /**
     * 机房id
     */
    @Schema(description = "机房id", example = "2")
    private Integer id;
    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "机房1")
    private String roomName;

    /**
     * 地址（楼层）
     */
    @Schema(description = "地址（楼层）", example = "地址（楼层）")
    private String addr;

    /**
     * 显示选择
     */
    @Schema(description = "显示选择")
    private Boolean displayFlag;

    /**
     * 显示类型（0-负载率/1-pue）
     */
    @Schema(description = "显示类型（0-负载率/1-pue）")
    private Boolean displayType;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "5")
    private float powerCapacity;

    /**
     * 空调额定功率
     */
    @Schema(description = "空调额定功率", example = "0")
    private float airPower;


    /**
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private Integer isDeleted;


    /**
     * 机房x长度(单位机柜)
     */
    @Schema(description = "x长度", example = "0")
    @JsonProperty(value="xLength")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @Schema(description = "y长度", example = "0")
    @JsonProperty(value="yLength")
    private int yLength;

    @Schema(description = "柜列列表", example = "[]")
    private List<AisleSaveVo>  aisleList;

    @Schema(description = "机柜列表", example = "[]")
    private List<CabinetVo>  cabinetList;

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
