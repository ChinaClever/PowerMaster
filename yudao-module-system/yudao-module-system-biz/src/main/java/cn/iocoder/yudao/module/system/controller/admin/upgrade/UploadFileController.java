package cn.iocoder.yudao.module.system.controller.admin.upgrade;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordRespVO;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.FileRecordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.UpgradeFileRespVO;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.UpgradeRecordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.UpgradeRecordRespVO;
import cn.iocoder.yudao.module.system.service.upgrade.UploadFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @Author: chenwany
 * @Date: 2024/8/1 14:09
 * @Description: 文件上传
 */
@Tag(name = "管理后台 - 系统升级文件上传")
@RequestMapping("/system/upgrade")
@RestController
public class UploadFileController {


    @Resource
    private UploadFileService uploadFileService;

    @PostMapping("/file/upload")
    @Operation(summary = "文件上传")
    @PreAuthorize("@ss.hasPermission('system:upgrade:file:upload')")
    public CommonResult<Integer> uploadFile( @RequestParam("upgradeDev")  int upgradeDev,
                                             @RequestParam(value = "roomIds",required = false) List<Integer> roomIds,
                                             @RequestParam(value = "ipList",required = false) List<String> ipList,
                                             @RequestPart("files") List<MultipartFile> files) {
        return success(uploadFileService.uploadFile(upgradeDev,roomIds,ipList,files));
    }

    @GetMapping("/file/download")
    @Operation(summary = "文件下载")
    @PermitAll
    public void downloadFile(HttpServletRequest request, HttpServletResponse response) {
        uploadFileService.downloadFile(request, response);
    }

    @GetMapping("/file/notice")
    @Operation(summary = "升级结果通知")
    @PermitAll
    public void notice(@RequestParam String devIp,
                       @RequestParam String code,@RequestParam String message) {
        uploadFileService.notice(devIp, code,message);
    }


    @GetMapping("/file/reNotice")
    @Operation(summary = "重发")
    public CommonResult<Integer> reNotice(@RequestParam Long id) {
        return success(uploadFileService.reNotice(id));
    }

    @DeleteMapping("/file/delete")
    @Operation(summary = "删除记录")
    @Parameter(name = "ids", description = "编号列表", required = true)
    public CommonResult<Boolean> deleteFileRecord(@RequestParam List<Integer> ids) {
        uploadFileService.deleteFileRecord(ids);
        return success(true);
    }


    @PostMapping("/file/page")
    @Operation(summary = "获得记录分页")
    public CommonResult<PageResult<UpgradeFileRespVO>> getFileRecordPage(@Valid @RequestBody FileRecordPageReqVO pageReqVO) {
        PageResult<UpgradeFileRespVO> pageResult = uploadFileService.getFileRecordPage(pageReqVO);
        return success(pageResult);
    }

    @DeleteMapping("/record/delete")
    @Operation(summary = "删除记录")
    @Parameter(name = "ids", description = "编号列表", required = true)
    public CommonResult<Boolean> deleteRecord(@RequestParam List<Integer> ids) {
        uploadFileService.deleteRecord(ids);
        return success(true);
    }


    @PostMapping("/record/page")
    @Operation(summary = "获得记录分页")
    public CommonResult<PageResult<UpgradeRecordRespVO>> getRecordPage(@Valid @RequestBody UpgradeRecordPageReqVO pageReqVO) {
        PageResult<UpgradeRecordRespVO> pageResult = uploadFileService.getRecordPage(pageReqVO);
        return success(pageResult);
    }
}
