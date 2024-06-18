package cn.iocoder.yudao.module.bus.dal.dataobject.boxcurbalancecolor;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 插接箱不平衡度颜色 DO
 *
 * @author clever
 */
@TableName("box_curbalance_color")
@KeySequence("box_curbalance_color_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxCurbalanceColorDO extends BaseDO {

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

}