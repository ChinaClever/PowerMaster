package cn.iocoder.yudao.module.statis.controller.admin.pdudevice;

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

import cn.iocoder.yudao.module.statis.controller.admin.pdudevice.vo.*;
import cn.iocoder.yudao.module.statis.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.statis.service.pdudevice.PDUDeviceService;

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

    @GetMapping("/displayscreen")
    @Operation(summary = "获得PDU设备分页")
    public String getDisplay(@Valid PDUDevicePageReqVO pageReqVO) {
        if("192.168.1.1-0".equals(pageReqVO.getDevKey())){
            return "{\n" +
                    "\t\"addr\": 0,\n" +
                    "    \"dev_ip\": \"192.168.1.163\",\t   \t\n" +
                    "\t\"dev_key\": \"192.168.1.163:0\",\t  \n" +
                    "\t\"datetime\": \"2024-03-15 14:55:50\",\n" +
                    "\t\"pdu_alarm\": \"本机第1相电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第2相电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第3相电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第1回路断路器, 断开; \\n本机第2回路断路器, 断开; \\n本机第3回路断路器, 断开; \\n本机第1回路电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第2回路电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第3回路电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n\",\n" +
                    "\t\"pdu_data\": {\n" +
                    "\t\t\"dual_item_list\": {},\n" +
                    "\t\t\"env_item_list\": {\n" +
                    "\t\t\t\"insert\": [0, 0, 0, 0]\n" +
                    "\t\t},\n" +
                    "\t\t\"group_item_list\": {},\n" +
                    "\t\t\"line_item_list\": {  \n" +
                    "\t\t\t\"apparent_pow\": [0, 0, 0],\n" +
                    "\t\t\t\"cur_alarm_status\": [1, 0, 0],\n" +
                    "\t\t\t\"cur_alarm_max\":[6,6,0],\n" +
                    "\t\t\t\"cur_value\": [6, 2, 0],\n" +
                    "\t\t\t\"ele\": [0, 0, 0],\n" +
                    "\t\t\t\"pf\": [0, 0, 0],\n" +
                    "\t\t\t\"phase_voltage\": [0, 0, 0],\n" +
                    "\t\t\t\"pow_alarm_status\": [0, 0, 0],\n" +
                    "\t\t\t\"pow_value\": [0, 0, 0],\n" +
                    "\t\t\t\"reactive_pow\": [0, 0, 0],\n" +
                    "\t\t\t\"vol_alarm_status\": [1, 1, 1],\n" +
                    "\t\t\t\"vol_value\": [0, 0, 0]\n" +
                    "\t\t},\n" +
                    "\t\t\"loop_item_list\": { \n" +
                    "\t\t\t\"apparent_pow\": [0, 0, 0],\n" +
                    "\t\t\t\"breaker\": [0, 0, 0],\n" +
                    "\t\t\t\"cur_alarm_status\": [1, 0, 0],\n" +
                    "\t\t\t\"cur_value\": [6, 0, 0],\n" +
                    "\t\t\t\"ele\": [0, 0, 0],\n" +
                    "\t\t\t\"pf\": [0, 0, 0],\n" +
                    "\t\t\t\"pow_alarm_status\": [0, 0, 0],\n" +
                    "\t\t\t\"pow_value\": [0, 0, 0],\n" +
                    "\t\t\t\"reactive_pow\": [0, 0, 0],\n" +
                    "\t\t\t\"vol_alarm_status\": [1, 1, 1],\n" +
                    "\t\t\t\"vol_value\": [0, 0, 0]\n" +
                    "\t\t},\n" +
                    "\t\t\"output_item_list\": { \n" +
                    "\t\t\t\"apparent_pow\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"cur_alarm_status\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"cur_value\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"ele\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"name\": [\"Output1\", \"Output2\", \"Output3\", \"Output4\", \"Output5\", \"Output6\", \"Output7\", \"Output8\", \"Output9\", \"Output10\", \"Output11\", \"Output12\", \"Output13\", \"Output14\", \"Output15\", \"Output16\", \"Output17\", \"Output18\", \"Output19\", \"Output20\", \"Output21\", \"Output22\", \"Output23\", \"Output24\"],\n" +
                    "\t\t\t\"pf\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"pow_alarm_status\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"pow_value\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"reactive_pow\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"relay_state\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]\n" +
                    "\t\t},\n" +
                    "\t\t\"pdu_tg_data\": { \n" +
                    "\t\t\t\"apparent_pow\": 0,\n" +
                    "\t\t\t\"cur\": 0,\n" +
                    "\t\t\t\"ele\": 0,\n" +
                    "\t\t\t\"pf\": 0,\n" +
                    "\t\t\t\"pow\": 0,\n" +
                    "\t\t\t\"reactive_pow\": 0\n" +
                    "\t\t}\n" +
                    "\t},\n" +
                    "\t\"status\": 4,\n" +
                    "\t\"uuid\": \"31d8fff7-4869-4363-8142-d86248e749bd\",\n" +
                    "\t\"uut_info\": {\n" +
                    "\t\t\"location\": \"local\",\n" +
                    "\t\t\"pdu_type\": \"MPDU Pro\",\n" +
                    "\t\t\"room\": \"room\",\n" +
                    "\t\t\"uuid\": \"31d8fff7-4869-4363-8142-d86248e749bd\"\n" +
                    "\t},\n" +
                    "\t\"version\": 1\n" +
                    "}";
        }
        else{
            return "{\n" +
                    "\t\"addr\": 0,\n" +
                    "    \"dev_ip\": \"192.168.1.163\",\t   \t\n" +
                    "\t\"dev_key\": \"192.168.1.163:0\",\t  \n" +
                    "\t\"datetime\": \"2024-03-15 14:55:50\",\n" +
                    "\t\"pdu_alarm\": \"本机第1相电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第2相电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第3相电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第1回路断路器, 断开; \\n本机第2回路断路器, 断开; \\n本机第3回路断路器, 断开; \\n本机第1回路电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第2回路电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n本机第3回路电压, 过低告警; 当前值:0V　告警最小值:176V 预警最小值:176V 预警最大值:276V 告警最大值:276V; \\n\",\n" +
                    "\t\"pdu_data\": {\n" +
                    "\t\t\"dual_item_list\": {},\n" +
                    "\t\t\"env_item_list\": {\n" +
                    "\t\t\t\"insert\": [0, 0, 0, 0]\n" +
                    "\t\t},\n" +
                    "\t\t\"group_item_list\": {},\n" +
                    "\t\t\"line_item_list\": {  \n" +
                    "\t\t\t\"apparent_pow\": [0],\n" +
                    "\t\t\t\"cur_alarm_status\": [0],\n" +
                    "\t\t\t\"cur_alarm_max\":[16],\n" +
                    "\t\t\t\"cur_value\": [6],\n" +
                    "\t\t\t\"ele\": [0],\n" +
                    "\t\t\t\"pf\": [0],\n" +
                    "\t\t\t\"phase_voltage\": [0],\n" +
                    "\t\t\t\"pow_alarm_status\": [0],\n" +
                    "\t\t\t\"pow_value\": [0],\n" +
                    "\t\t\t\"reactive_pow\": [0],\n" +
                    "\t\t\t\"vol_alarm_status\": [1],\n" +
                    "\t\t\t\"vol_value\": [0]\n" +
                    "\t\t},\n" +
                    "\t\t\"loop_item_list\": { \n" +
                    "\t\t\t\"apparent_pow\": [0, 0, 0],\n" +
                    "\t\t\t\"breaker\": [0, 0, 0],\n" +
                    "\t\t\t\"cur_alarm_status\": [0, 0, 0],\n" +
                    "\t\t\t\"cur_value\": [0, 0, 0],\n" +
                    "\t\t\t\"ele\": [0, 0, 0],\n" +
                    "\t\t\t\"pf\": [0, 0, 0],\n" +
                    "\t\t\t\"pow_alarm_status\": [0, 0, 0],\n" +
                    "\t\t\t\"pow_value\": [0, 0, 0],\n" +
                    "\t\t\t\"reactive_pow\": [0, 0, 0],\n" +
                    "\t\t\t\"vol_alarm_status\": [1, 1, 1],\n" +
                    "\t\t\t\"vol_value\": [0, 0, 0]\n" +
                    "\t\t},\n" +
                    "\t\t\"output_item_list\": { \n" +
                    "\t\t\t\"apparent_pow\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"cur_alarm_status\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"cur_value\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"ele\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"name\": [\"Output1\", \"Output2\", \"Output3\", \"Output4\", \"Output5\", \"Output6\", \"Output7\", \"Output8\", \"Output9\", \"Output10\", \"Output11\", \"Output12\", \"Output13\", \"Output14\", \"Output15\", \"Output16\", \"Output17\", \"Output18\", \"Output19\", \"Output20\", \"Output21\", \"Output22\", \"Output23\", \"Output24\"],\n" +
                    "\t\t\t\"pf\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"pow_alarm_status\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"pow_value\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"reactive_pow\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\n" +
                    "\t\t\t\"relay_state\": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]\n" +
                    "\t\t},\n" +
                    "\t\t\"pdu_tg_data\": { \n" +
                    "\t\t\t\"apparent_pow\": 0,\n" +
                    "\t\t\t\"cur\": 0,\n" +
                    "\t\t\t\"ele\": 0,\n" +
                    "\t\t\t\"pf\": 0,\n" +
                    "\t\t\t\"pow\": 0,\n" +
                    "\t\t\t\"reactive_pow\": 0\n" +
                    "\t\t}\n" +
                    "\t},\n" +
                    "\t\"status\": 4,\n" +
                    "\t\"uuid\": \"31d8fff7-4869-4363-8142-d86248e749bd\",\n" +
                    "\t\"uut_info\": {\n" +
                    "\t\t\"location\": \"local\",\n" +
                    "\t\t\"pdu_type\": \"MPDU Pro\",\n" +
                    "\t\t\"room\": \"room\",\n" +
                    "\t\t\"uuid\": \"31d8fff7-4869-4363-8142-d86248e749bd\"\n" +
                    "\t},\n" +
                    "\t\"version\": 1\n" +
                    "}";
        }

    }

}