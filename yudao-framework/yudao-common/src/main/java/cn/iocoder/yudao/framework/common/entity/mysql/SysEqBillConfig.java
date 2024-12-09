package cn.iocoder.yudao.framework.common.entity.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 电量计费配置
 * @date 2024/4/17 13:59
 */
@Data
@TableName(value = "sys_eq_bill_config")
public class SysEqBillConfig implements Serializable {
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
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
