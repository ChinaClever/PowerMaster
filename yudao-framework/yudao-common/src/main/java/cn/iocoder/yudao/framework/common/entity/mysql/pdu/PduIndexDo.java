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
     * 级联地址
     */
    private String cascadeAddr;

    /**
     * 节点ip
     */
    private String nodeIp;

    /**
     * 是否删除
     */
    private int isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
