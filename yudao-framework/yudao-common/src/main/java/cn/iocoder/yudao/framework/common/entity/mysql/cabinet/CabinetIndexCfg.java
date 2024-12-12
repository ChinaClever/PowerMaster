package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜索引表
 * @date 2024/4/23 10:40
 */
@Data
public class CabinetIndexCfg implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;


    /**
     * 机房编号
     */
    private int roomId;

    /**
     * 机柜名称
     */
    private String cabinetName;
    /**
     * 通道编号
     */
    private int aisleId;

    /**
     *  柜列的位置 从1开始 0未被分配
     */
    private Integer  aisleX;
    /**
     * 机柜类型
     */
    private String cabinetType;


    private int  cabinetHeight;
    /**
     * 电力容量
     */
    private Double powerCapacity;

    /**
     * 运行状态
     */
    private int runStatus;
    /**
     * 负载状态
     */
    private int loadStatus;

    /**
     * 数据来源
     */
    private Boolean pduBox;

    /**
     * 是否禁用
     */
    private Boolean isDisabled;

    /**
     * 是否删除
     */
    private Boolean isDeleted;


    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @TableField(value = "count(*)",insertStrategy = FieldStrategy.NEVER,updateStrategy = FieldStrategy.NEVER,select = false)
    private  Integer count;



    /**
     * 机柜id
     */
    private int cabinetId;

    /**
     * x坐标
     */
    @JsonProperty(value = "x_coordinate")
    private int xCoordinate;

    /**
     * y坐标
     */
    @JsonProperty(value = "y_coordinate")
    private int yCoordinate;

    /**
     * 所属于公司
     */
    private String company;

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
