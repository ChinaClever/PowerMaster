package cn.iocoder.yudao.framework.common.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RoomIndexCfgVO {


    @Schema(description = "机房id", example = "2")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    /**
     * 机房名
     */
    @Schema(description = "机房名", example = "机房2")
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
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private int isDelete;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "0")
    private float powerCapacity;


    /**
     * 空调额定功率
     */
    @Schema(description = "空调额定功率", example = "0")
    private float airPower;


    /**
     * 机房x长度(单位机柜)
     */
    @JsonProperty(value="xLength")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @JsonProperty(value="yLength")
    private int yLength;

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
