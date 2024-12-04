package cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PduIndex {

    /**
     * pdu_id  唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 设备识别码
     */
    private String pduKey;

    /**
     * ip地址
     */
    private String ipAddr;

    /**
     * 级联地址
     */
    private Integer cascadeId;

    /**
     * 节点ip
     */
    private Integer nodeId;

    /**
     * 是否删除
     */
    private Boolean isDeleted;
    /**
     * 运行状态
     */
    private Integer runStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
