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
    private String busKey;
    /**
     * ip地址
     */
    private String ipAddr;

    /**
     * 母线编号
     */
    private Integer busId;

    /**
     * 节点IP
     */
    private String  busName;

    /**
     * 运行状态 0：离线 1：正常  2：告警
     */
    private Integer runStatus;
    /**
     * 节点IP
     */
    private Integer nodeId;
    /**
     * 逻辑删除
     */
    private Boolean isDeleted;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}
