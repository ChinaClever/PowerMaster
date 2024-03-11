package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.pdu.service.pdudevice.PDUDeviceService;

@Tag(name = "管理后台 - PDU设备")
@RestController
@RequestMapping("/pdu/PDU-device")
@Validated
public class PDUDeviceController {

    @Resource
    private PDUDeviceService pDUDeviceService;

    @PostMapping("/create")
    @Operation(summary = "创建PDU设备")
    @PreAuthorize("@ss.hasPermission('pdu:PDU-device:create')")
    public CommonResult<Long> createPDUDevice(@Valid @RequestBody PDUDeviceSaveReqVO createReqVO) {
        return success(pDUDeviceService.createPDUDevice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新PDU设备")
    @PreAuthorize("@ss.hasPermission('pdu:PDU-device:update')")
    public CommonResult<Boolean> updatePDUDevice(@Valid @RequestBody PDUDeviceSaveReqVO updateReqVO) {
        pDUDeviceService.updatePDUDevice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除PDU设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pdu:PDU-device:delete')")
    public CommonResult<Boolean> deletePDUDevice(@RequestParam("id") Long id) {
        pDUDeviceService.deletePDUDevice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得PDU设备")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pdu:PDU-device:query')")
    public CommonResult<PDUDeviceRespVO> getPDUDevice(@RequestParam("id") Long id) {
        PDUDeviceDO pDUDevice = pDUDeviceService.getPDUDevice(id);
        return success(BeanUtils.toBean(pDUDevice, PDUDeviceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得PDU设备分页")
    @PreAuthorize("@ss.hasPermission('pdu:PDU-device:query')")
    public CommonResult<PageResult<PDUDeviceRespVO>> getPDUDevicePage(@Valid PDUDevicePageReqVO pageReqVO) {
        PageResult<PDUDeviceDO> pageResult = pDUDeviceService.getPDUDevicePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PDUDeviceRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出PDU设备 Excel")
    @PreAuthorize("@ss.hasPermission('pdu:PDU-device:export')")
    @OperateLog(type = EXPORT)
    public void exportPDUDeviceExcel(@Valid PDUDevicePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PDUDeviceDO> list = pDUDeviceService.getPDUDevicePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "PDU设备.xls", "数据", PDUDeviceRespVO.class,
                        BeanUtils.toBean(list, PDUDeviceRespVO.class));
    }

}