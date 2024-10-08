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
    private int id;

    /**
     * 设备识别码
     */
    private String devKey;

    /**
     * ip地址
     */
    private String ipAddr;

    /**
     * 级联
     */
    private Integer casAddr;

    /**
     * 母线编号
     */
    private Integer barId;
    /**
     * 节点ip
     */
    private String nodeIp;

    /**
     * 运行状态
     */
    private int runStatus;

    /**
     * 是否删除
     */
    private int isDeleted;

    /**
     * 母线名
     */
    private String busName;

    /**
     * 插接箱名称
     */
    private String boxName;

    /**
     * 插接箱类型0：插接箱；1：温度模块
     */
    private int boxType;

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

}
