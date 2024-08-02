package cn.iocoder.yudao.module.system.controller.admin.upgrade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author: chenwany
 * @Date: 2024/8/1 14:12
 * @Description: 文件上传
 */
@Schema(description = "管理后台 - 文件上传 Request VO")
@Data
@ToString(callSuper = true)
public class UploadFileReqVo {

//    /**
//     * 升级文件
//     */
//    @Schema(description = "升级文件", requiredMode = Schema.RequiredMode.REQUIRED, example = "[]")
//    private List<MultipartFile>  files;

    /**
     * 升级设备 0 全部  1 所选机房  2 指定ip
     */
    @Schema(description = "升级设备", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private int upgradeDev;


    /**
     * 机房id
     */
    @Schema(description = "机房id", example = "[]")
    private List<Integer> roomIds;

    /**
     * ip列表
     */
    @Schema(description = "ip列表", example = "[]")
    private List<String> ipList;
}
