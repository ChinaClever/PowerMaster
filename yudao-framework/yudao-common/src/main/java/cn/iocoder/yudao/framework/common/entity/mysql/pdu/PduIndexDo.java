package cn.iocoder.yudao.framework.common.entity.mysql.pdu;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author chenwany
 * @Date: 2024/3/28 09:57
 * @Description: pdu设备索引表
 */
@Data
@TableName(value = "pdu_index")
public class PduIndexDo implements Serializable {

    private static final long serialVersionUID = 1L;
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