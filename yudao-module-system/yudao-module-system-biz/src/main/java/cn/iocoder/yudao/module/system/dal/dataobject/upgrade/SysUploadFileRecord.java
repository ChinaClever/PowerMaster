package cn.iocoder.yudao.module.system.dal.dataobject.upgrade;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: chenwany
 * @Date: 2024/8/1 13:48
 * @Description: 升级文件上传记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_upload_file_record",autoResultMap=true)
public class SysUploadFileRecord  {

    /**
     * 序号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 升级设备 0 全部  1 所选机房  2 指定ip
     */
    private int upgradeDev;


    /**
     * 机房id
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> roomIds;

    /**
     * ip列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> ipList;

    /**
     * 升级文件
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileNames;

    /**
     * 文件存放路径
     */
    private String filePath;

    /**
     * 上传结果
     */
    private String uploadResult;

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
