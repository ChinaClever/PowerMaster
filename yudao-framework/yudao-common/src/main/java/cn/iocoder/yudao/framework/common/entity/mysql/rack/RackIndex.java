package cn.iocoder.yudao.framework.common.entity.mysql.rack;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架索引表
 * @date 2024/5/13 14:36
 */
@Data
@TableName(value = "rack_index",autoResultMap = true)
public class RackIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;
    /**
     * 机柜id
     */
    private Integer cabinetId;
    /**
     * 机房id
     */
//    private Integer roomId;
    /**
     * U位名称
     */
    private String rackName;

    private String ip;

    /**
     * A路输出位
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> outletIdA;

    /**
     * B路输出位
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> outletIdB;
    /**
     * 所属公司
     */
    private String company;
    /**
     * U位位置
     */
    @JsonProperty(value="uAddress")
    private Integer uAddress;

    /**
     * U位高度
     */
    @JsonProperty(value="uHeight")
    private Integer uHeight;
    /**
     * 设备类型
     */
    private String rackType;

    /**
     * 是否删除 0未删除 1已删除
     */
    private Boolean isDelete;

    //机架状态，0：关机 1：开机
    private Integer runStatus;
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


}
