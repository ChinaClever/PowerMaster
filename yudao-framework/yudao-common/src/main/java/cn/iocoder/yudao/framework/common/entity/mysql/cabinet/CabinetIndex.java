package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜索引表
 * @date 2024/4/23 10:40
 */
@Data
@TableName(value = "cabinet_index")
public class CabinetIndex implements Serializable {

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
     * 机柜高度
     */
    private int cabinetHeight;

    /**
     * 电力容量
     */
    private float powerCapacity;

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
    private int pduBox;

    /**
     * 是否禁用
     */
    private int isDisabled;

    /**
     * 是否删除
     */
    private int isDeleted;

//    /**
//     * 日用能告警开关
//     */
//    private  int eleAlarmDay;
//
//    /**
//     * 月用能告警开关
//     */
//    private  int eleAlarmMonth;
//
//    /**
//     * 日用能限制
//     */
//    private double eleLimitDay;
//
//    /**
//     * 月用能限制
//     */
//    private double eleLimitMonth;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-05-07 01:00:00")
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-05-07 01:00:00")
    private LocalDateTime createTime;


    @TableField(value = "count(*)",insertStrategy = FieldStrategy.NEVER,updateStrategy = FieldStrategy.NEVER,select = false)
    private  Integer count;
}
