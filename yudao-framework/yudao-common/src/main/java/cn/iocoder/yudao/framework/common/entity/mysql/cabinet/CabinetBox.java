package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜与母线关联表
 * @date 2024/4/23 10:30
 */
@Data
@TableName(value = "cabinet_box")
public class CabinetBox implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 机柜id
     */
    private int cabinetId;
    /**
     * A路插接箱
     */
    private String boxKeyA;
    /**
     * A路插接箱输出位
     */
    private Integer outletIdA;
    /**
     * B路插接箱
     */
    private String boxKeyB;
    /**
     * B路插接箱输出位
     */
    private Integer outletIdB;

    /**
     * 标记位-名字  前端用
     */
//    private String boxIndexA;

    /**
     * 标记位-名字  前端用
     */
//    private String boxIndexB;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}