package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜配置表
 * @date 2024/4/28 13:53
 */
@Data
@TableName(value = "cabinet_cfg")
public class CabinetCfg implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;


    /**
     * 机柜id
     */
    private int cabinetId;

//    /**
//     * 机柜名称
//     */
//    private String cabinetName;

//    /**
//     * 机柜高度
//     */
//    private int cabinetHeight;

//    /**
//     * 机柜类型
//     */
//    private String type;

    /**
     * x坐标
     */
    @JsonProperty(value="xCoordinate")
    private int xCoordinate;

    /**
     * y坐标
     */
    @JsonProperty(value="yCoordinate")
    private int yCoordinate;

    /**
     * 所属于公司
     */
    private String company;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

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
