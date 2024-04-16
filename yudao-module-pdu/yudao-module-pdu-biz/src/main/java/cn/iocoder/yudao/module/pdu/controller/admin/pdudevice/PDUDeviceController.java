package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDeviceRespVO;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDeviceSaveReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

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

import cn.iocoder.yudao.module.pdu.service.pdudevice.PDUDeviceService;

@Tag(name = "管理后台 - PDU设备")
@RestController
@RequestMapping("/pdu/PDU-device")
@Validated
public class PDUDeviceController {

    @Resource
    private PDUDeviceService pDUDeviceService;



    @GetMapping("/page")
    @Operation(summary = "获得PDU设备分页")
    @PreAuthorize("@ss.hasPermission('pdu:PDU-device:query')")
    public CommonResult<PageResult<PDUDeviceDO>> getPDUDevicePage(@Valid PDUDevicePageReqVO pageReqVO) {
        PageResult<PDUDeviceDO> pageResult = pDUDeviceService.getPDUDevicePage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/displayscreen")
    @Operation(summary = "获得PDU设备详细信息")
    public String getDisplay(@Valid PDUDevicePageReqVO pageReqVO) {
        return pDUDeviceService.getDisplayDataByDevKey(pageReqVO.getDevKey());
    }

    @GetMapping("/hisdata/{id}")
    @Operation(summary = "获得PDU历史数据")
    public CommonResult<Map> getHistoryDataByPduId(@PathVariable("id") Long id) {
        return success(pDUDeviceService.getHistoryDataByPduId(id));
    }

}