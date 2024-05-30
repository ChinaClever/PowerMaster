package cn.iocoder.yudao.framework.common.entity.mysql.bus;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author chenwany
 * @Date: 2024/3/28 09:57
 * @Description: bus始端箱设备索引表
 */
@Data
@TableName(value = "bus_index")
public class BusIndex implements Serializable {

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
     * 始端箱地址
     */
    private String devAddr;

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
