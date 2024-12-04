package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
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

    /**
     * x坐标
     */
    @JsonProperty(value = "xCoordinate")
    private int xCoordinate;

    /**
     * y坐标
     */
    @JsonProperty(value = "yCoordinate")
    private int yCoordinate;

    /**
     * 所属于公司
     */
    private String company;

    /**
     * 更新时间
     */
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 日用电告警开关 0禁用 1启用
     */
    private Boolean eleAlarmDay;

    /**
     * 日用能限制
     */
    private BigDecimal eleLimitDay;

    /**
     * 月用电告警开关 0禁用 1启用
     */
    private Boolean eleAlarmMonth;

    /**
     * 月用能限制
     */
    private BigDecimal eleLimitMonth;
}

