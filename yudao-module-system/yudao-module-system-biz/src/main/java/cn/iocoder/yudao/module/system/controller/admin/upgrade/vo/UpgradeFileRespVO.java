package cn.iocoder.yudao.module.system.controller.admin.upgrade.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 文件上传 Response VO")
@Data
public class UpgradeFileRespVO {

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
     * 升级设备 0 全部  1 所选机房  2 指定ip
     */
    private String upgradeDevMsg;


    /**
     * 机房id
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> roomIds;

    /**
     * 机房id
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> roomNames;

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


    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
