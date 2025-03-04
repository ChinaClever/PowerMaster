package cn.iocoder.yudao.framework.common.entity.mysql.bus;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author chenwany
 * @Date: 2024/3/28 09:57
 * @Description: 插接箱设备索引表
 */
@Data
@TableName(value = "box_index")
public class BoxIndex implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * bus_id  唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 设备识别码
     */
    private String boxKey;

    private String busKey;

    /**
     * ip地址
     */
    private String ipAddr;

    /**
     * 母线编号
     */
    private Integer busId;


    private Integer  boxId;

    /**
     * 插接箱类型0：插接箱；1：温度模块
     */
    private Integer boxType;

    /**
     * 插接箱名称
     */
    private String boxName;

    /**
     * 负载率：总视在功率/电力容量
     * 负载量状态：load_rate_status
     * 0：空载
     * 1：<30%
     * 2: 30~60
     * 3：60~90
     * 4：>90%
     */
    private Integer loadRateStatus;

    /**
     * 三相电流不平衡度：cur_unbalance_status
     * 0：无效（单相设备）
     * 1：小电流不平衡
     * 2：< 15%
     * 3: 15~30
     * 4：> 31%
     */
    private Integer curUnbalanceStatus;

    /**
     * 运行状态
     */
    private Integer runStatus;

    /**
     * 是否删除
     */
    private Boolean isDeleted;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 节点ip
     */
    private Integer nodeId;

}