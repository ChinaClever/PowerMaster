package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDULineRes;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.ibatis.annotations.Param;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
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

    @PostMapping("/page")
    @Operation(summary = "获得PDU配电分页")
    public CommonResult<PageResult<PDUDeviceDO>> getPDUDevicePage(@RequestBody PDUDevicePageReqVO pageReqVO) {
        PageResult<PDUDeviceDO> pageResult = pDUDeviceService.getPDUDevicePage(pageReqVO);
        return success(pageResult);
    }
    @PostMapping("/getDeletedPage")
    @Operation(summary = "获得已删除PDU分页")
    public CommonResult<PageResult<PDUDeviceDO>> getDeletedPDUDevicePage(@RequestBody PDUDevicePageReqVO pageReqVO) {
        PageResult<PDUDeviceDO> pageResult = pDUDeviceService.getDeletedPDUDevicePage(pageReqVO);
        return success(pageResult);
    }
    @PostMapping("/line/page")
    @Operation(summary = "获得PDU需量分页")
    public CommonResult<PageResult<PDULineRes>> getPDULineDevicePage(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPDULineDevicePage(pageReqVO));
    }
    @PostMapping("/line/getMaxLineId")
    @Operation(summary = "获得PDU相id的最大值")
    public CommonResult<Integer> getPDUMaxLineId(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPDUMaxLineId(pageReqVO));
    }

    @PostMapping("/line/getMaxCur")
    @Operation(summary = "获得PDU电流最大值的相数据")
    public CommonResult<PageResult<PDULineRes>> getPDUMaxCurData(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPDUMaxCurData(pageReqVO));
    }

    @GetMapping("/displayscreen")
    @Operation(summary = "获得PDU设备详细信息")
    public CommonResult<String> getDisplay(String devKey) {
        return success(pDUDeviceService.getDisplayDataByDevKey(devKey));
    }

    @GetMapping("/displayscreen/location")
    @Operation(summary = "获得位置")
    public CommonResult<String> getLocationByDevKey(String devKey) {
        return success(pDUDeviceService.getLocationByDevKey(devKey));
    }

    @GetMapping("/hisdata")
    @Operation(summary = "获得PDU历史数据")
    public CommonResult<Map> getHistoryDataByDevKey(String devKey,String type) {
        return success(pDUDeviceService.getHistoryDataByDevKey(devKey,type));
    }
//pdu_hda_line_realtime
    @GetMapping("/pduHdaLineHisdata")
    @Operation(summary = "获得PDU相历史数据")
    public CommonResult<Map> getPduHdaLineHisdataKey(String devKey,String type) {
        return success(pDUDeviceService.getPduHdaLineHisdataKey(devKey,type));
    }

    @GetMapping("/chartNewData")
    @Operation(summary = "获得PDU历史最新数据")
    public CommonResult<Map> getChartNewDataByPduDevKey(String devKey,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,String type) {
        return success(pDUDeviceService.getChartNewDataByPduDevKey(devKey,oldTime,type));
    }

    @PostMapping("/report/ele")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportConsumeDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportConsumeDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pfline")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getPDUPFLine(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPDUPFLine(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pow")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportPowDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportPowDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/outlet")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportOutLetDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportOutLetDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/tem")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportTemDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportTemDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
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

    @GetMapping("/delete")
    @Operation(summary = "删除PDU设备")
    public CommonResult<Integer> deletePDU(@Param("devKey") String devKey) throws Exception  {
        int pduId = pDUDeviceService.deletePDU(devKey);
        if (pduId == -1) {
            return error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "删除失败");
        }
        return success(pduId);
    }

    @GetMapping("/restore")
    @Operation(summary = "恢复设备")
    public CommonResult<Integer> restorePDU(@Param("devKey") String devKey) throws Exception  {
        int pduId = pDUDeviceService.restorePDU(devKey);
        if (pduId == -1) {
            return error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "恢复失败");
        }
        return success(pduId);
    }
    @GetMapping("line/getMaxLine")
    @Operation(summary = "PDU需量详情数据")
    public CommonResult<Map> getPduMaxLine(@Parameter(description =  "pdu的id") Integer id,@Parameter(description =  "选择类型24小时：hour/30天 day") String type) {
        return success(pDUDeviceService.getPduMaxLine(id,type));
    }
}