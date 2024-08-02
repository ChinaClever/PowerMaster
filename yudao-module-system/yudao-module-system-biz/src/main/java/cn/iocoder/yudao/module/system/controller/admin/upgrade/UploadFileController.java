package cn.iocoder.yudao.module.system.controller.admin.upgrade;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.UploadFileReqVo;
import cn.iocoder.yudao.module.system.service.alarm.AlarmRecordService;
import cn.iocoder.yudao.module.system.service.upgrade.UploadFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @Author: chenwany
 * @Date: 2024/8/1 14:09
 * @Description: 文件上传
 */
@Tag(name = "管理后台 - 系统升级文件上传")
@RequestMapping("/system/upgrade/file")
@RestController
public class UploadFileController {


    @Resource
    private UploadFileService uploadFileService;

    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    @PreAuthorize("@ss.hasPermission('system:upgrade:file:upload')")
    public CommonResult<Integer> uploadFile( @RequestParam("upgradeDev")  int upgradeDev,
                                             @RequestParam(value = "roomIds",required = false) List<Integer> roomIds,
                                             @RequestParam(value = "ipList",required = false) List<Integer> ipList,
                                             @RequestPart("files") List<MultipartFile> files) {
        return success(uploadFileService.uploadFile(upgradeDev,roomIds,ipList,files));
    }
}
