package cn.iocoder.yudao.module.bus.dal.dataobject.boxharmoniccolor;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 插接箱不平衡度颜色 DO
 *
 * @author clever
 */
@TableName("box_harmonic_color")
@KeySequence("box_harmonic_color_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxHarmonicColorDO {
    /**
     * 自增id
     */
    @TableId
    private Long id;
    /**
     * 第一个小于的范围
     */
    private Integer rangeOne;
    /**
     * 第二个范围的最小值
     */
    private Integer rangeTwo;
    /**
     * 第二个范围的最大值
     */
    private Integer rangeThree;
    /**
     * 第三个大于的范围
     */
    private Integer rangeFour;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
