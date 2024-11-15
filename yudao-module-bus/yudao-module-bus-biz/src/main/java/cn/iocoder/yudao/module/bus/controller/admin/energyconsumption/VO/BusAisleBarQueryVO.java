package cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: jiangjinchi
 * @time: 2024/11/14 13:54
 */
@Data
public class BusAisleBarQueryVO {

    /**
     * id
     */
    @TableId
    private Integer id;
    /**
     * 设备识别码
     */
    private String devKey;
    /**
     * ip地址
     */
    private String ipAddr;
    /**
     * 母线地址
     */
    private String casAddr;
    /**
     * 母线编号
     */
    private Integer barId;
    /**
     * 运行状态 0：正常 1：预警 2：告警 3: 升级 4：故障 5：离线
     */
    private Integer runStatus;
    /**
     * 节点IP
     */
    private String nodeIp;
    /**
     * 节点IP
     */
    private String busName;
    /**
     * 逻辑删除
     */
    private Integer isDeleted;
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



    /**
     * 柜列id
     */
    @Schema(description = "柜列id", example = "1")
    private Integer aisleId;


    /**
     *  唯一标识
     */
    private String barKey;

    /**
     * ip地址
     */
    private String devIp;


    /**
     * AB路
     */
    private String path;

    /**
     * 方向  左0 右1
     */
    private Integer direction;



}
