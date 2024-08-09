package cn.iocoder.yudao.module.system.dal.dataobject.upgrade;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Author: chenwany
 * @Date: 2024/8/1 13:08
 * @Description: 设备升级记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_upgrade_record",autoResultMap=true)
public class SysUpgradeRecord {

    /**
     * 序号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备ip
     */
    private String devIp;

    /**
     * 位置信息
     */
    private String devPosition;

    /**
     * 升级文件
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileNames;

    /**
     * 升级状态
     */
    private int status;

    /**
     * 下载进度
     */
    private double downloadProgress;

    /**
     * 升级结果
     */
    private String  result;

    /**
     * 结束原因
     */
    private String  finishReason;

    /**
     * 升级开始时间
     */
    private Date  startTime;

    /**
     * 升级结束时间
     */
    private Date  endTime;

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
}
