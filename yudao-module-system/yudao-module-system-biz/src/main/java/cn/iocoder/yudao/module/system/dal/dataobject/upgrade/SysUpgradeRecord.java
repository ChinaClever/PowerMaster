package cn.iocoder.yudao.module.system.dal.dataobject.upgrade;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: chenwany
 * @Date: 2024/8/1 13:08
 * @Description: 设备升级记录
 */
@Data
@Builder
@TableName(value = "sys_upgrade_record")
public class SysUpgradeRecord extends BaseDO {

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

}
