package cn.iocoder.yudao.module.cabinet.dal.dataobject.temcolor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 机柜温度颜色 DO
 *
 * @author clever
 */
@TableName("cabinet_tem_color")
@KeySequence("cabinet_tem_color_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemColorDO{

    /**
     * 自增id
     */
    @TableId
    private Long id;
    /**
     * 温度范围最小值
     */
    private Integer min;
    /**
     * 温度范围最大值
     */
    private Integer max;
    /**
     * 温度范围对应的颜色
     */
    private String color;
    /**
     * 创建时间
     */
//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "热温度范围最小值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer hotMin;

    @Schema(description = "热温度范围最大值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer hotMax;

    @Schema(description = "热温度范围对应的颜色", requiredMode = Schema.RequiredMode.REQUIRED)
    private String hotColor;


}