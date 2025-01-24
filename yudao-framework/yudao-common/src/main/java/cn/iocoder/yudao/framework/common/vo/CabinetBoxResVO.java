package cn.iocoder.yudao.framework.common.vo;

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
public class CabinetBoxResVO implements Serializable {

    private static final long serialVersionUID = 1L;


    private Integer id;


    private Integer roomId;

    private String roomName;
    /**
     * 机柜id
     */
    private int cabinetId;

    private String cabinetName;
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