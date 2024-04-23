package cn.iocoder.yudao.framework.common.entity.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 数据采集系统配置
 * @date 2024/4/16 15:50
 */
@Data
@TableName(value = "sys_mq_config")
public class SysMqConfig {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    /**
     * IP端口
     */
    private  String ipAndPorts;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String passWord;


    /**
     * 主题
     */
    private String topic;

    /**
     * mq名称
     */
    private String mq;


    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 创建时间
     */
    private String createTime;
}
