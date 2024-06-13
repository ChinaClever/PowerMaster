package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDULineRes;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.Operation;

import javax.validation.*;

import java.time.LocalDateTime;
import java.util.*;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import org.springframework.format.annotation.DateTimeFormat;

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
    public CommonResult<PageResult<PDUDeviceDO>> getPDUDevicePage(@Valid PDUDevicePageReqVO pageReqVO) {
        PageResult<PDUDeviceDO> pageResult = pDUDeviceService.getPDUDevicePage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/line/page")
    @Operation(summary = "获得PDU设备分页")
    public CommonResult<PageResult<PDULineRes>> getPDULineDevicePage(@Valid PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPDULineDevicePage(pageReqVO));
    }

    @GetMapping("/displayscreen")
    @Operation(summary = "获得PDU设备详细信息")
    public String getDisplay(@Valid PDUDevicePageReqVO pageReqVO) {
        return pDUDeviceService.getDisplayDataByDevKey(pageReqVO.getDevKey());
    }

    @GetMapping("/hisdata")
    @Operation(summary = "获得PDU历史数据")
    public CommonResult<Map> getHistoryDataByDevKey(String devKey,String type) {
        return success(pDUDeviceService.getHistoryDataByDevKey(devKey,type));
    }

    @GetMapping("/chartNewData")
    @Operation(summary = "获得PDU历史最新数据")
    public CommonResult<Map> getChartNewDataByPduDevKey(String devKey,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,String type) {
        return success(pDUDeviceService.getChartNewDataByPduDevKey(devKey,oldTime,type));
    }

    @GetMapping("/report/ele")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportConsumeDataByDevKey(String devKey,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(pDUDeviceService.getReportConsumeDataByDevKey(devKey,timeType,oldTime,newTime));
    }

    @GetMapping("/report/pow")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportPowDataByDevKey(String devKey,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(pDUDeviceService.getReportPowDataByDevKey(devKey,timeType,oldTime,newTime));
    }

    @GetMapping("/report/outlet")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportOutLetDataByDevKey(String devKey,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(pDUDeviceService.getReportOutLetDataByDevKey(devKey,timeType,oldTime,newTime));
    }

    @GetMapping("/report/tem")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportTemDataByDevKey(String devKey,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(pDUDeviceService.getReportTemDataByDevKey(devKey,timeType,oldTime,newTime));
    }

    @GetMapping("/devKeyList")
    @Operation(summary = "获得PDU设备devKey列表")
    public List<String> getDevKeyList() {
        return pDUDeviceService.getDevKeyList();
    }

    @GetMapping("/ipList")
    @Operation(summary = "获得PDU设备Ip列表")
    public List<String> getIpList() {
        return pDUDeviceService.getIpList();
    }
}