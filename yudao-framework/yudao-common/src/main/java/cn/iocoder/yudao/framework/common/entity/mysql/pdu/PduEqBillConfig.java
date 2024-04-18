package cn.iocoder.yudao.framework.common.entity.mysql.pdu;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author luowei
 * @version 1.0
 * @description: pdu电量计费配置
 * @date 2024/4/17 13:59
 */
@Data
@TableName(value = "pdu_eq_bill_config")
public class PduEqBillConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 电费
     */
    private double bill;

    /**
     * 计费方式
     */
    private int billMode;

    /**
     * 计费时间段
     */
    private String billPeriod;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

}
