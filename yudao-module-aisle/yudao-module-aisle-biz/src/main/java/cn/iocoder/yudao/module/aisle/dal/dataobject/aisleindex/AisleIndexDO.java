package cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 通道列 DO
 *
 * @author clever
 */
@TableName("aisle_index")
@KeySequence("aisle_index_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AisleIndexDO {

    @Schema(description = "柜列id", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 机房id
     */
    @Schema(description = "机房id", example = "1")
    private Integer roomId;

    /**
     * 柜列名称
     */
    @Schema(description = "柜列名称", example = "柜列1")
    private String aisleName;

    /**
     * 数据来源 0：PDU 1：母线
     */
    @Schema(description = "数据来源 0：PDU 1：母线", example = "0")
    private Boolean pduBar;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private Integer isDelete;


    /**
     * 长度
     */
    private Integer aisleLength;

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
     * 柜列方向 x y
     */
    private String direction;


    private Double powerCapacity;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-05-07 01:00:00")
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-05-07 01:00:00")
//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}