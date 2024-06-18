package cn.iocoder.yudao.module.pdu.controller.admin.dcconfig;

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

import cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.dcconfig.DcConfigDO;
import cn.iocoder.yudao.module.pdu.service.dcconfig.PDUDcConfigService;

@Tag(name = "管理后台 - pdu数据采集配置")
@RestController
@RequestMapping("/pdu/dc-config")
@Validated
public class DcConfigController {

    @Resource
    private PDUDcConfigService PDUDcConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建pdu数据采集配置")
    @PreAuthorize("@ss.hasPermission('pdu:dc-config:create')")
    public CommonResult<Short> createDcConfig(@Valid @RequestBody DcConfigSaveReqVO createReqVO) {
        return success(PDUDcConfigService.createDcConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新pdu数据采集配置")
    @PreAuthorize("@ss.hasPermission('pdu:dc-config:update')")
    public CommonResult<Boolean> updateDcConfig(@Valid @RequestBody DcConfigSaveReqVO updateReqVO) {
        PDUDcConfigService.updateDcConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除pdu数据采集配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pdu:dc-config:delete')")
    public CommonResult<Boolean> deleteDcConfig(@RequestParam("id") Short id) {
        PDUDcConfigService.deleteDcConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得pdu数据采集配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pdu:dc-config:query')")
    public CommonResult<DcConfigRespVO> getDcConfig(@RequestParam("id") Short id) {
        DcConfigDO dcConfig = PDUDcConfigService.getDcConfig(id);
        return success(BeanUtils.toBean(dcConfig, DcConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得pdu数据采集配置分页")
    @PreAuthorize("@ss.hasPermission('pdu:dc-config:query')")
    public CommonResult<PageResult<DcConfigRespVO>> getDcConfigPage(@Valid DcConfigPageReqVO pageReqVO) {
        PageResult<DcConfigDO> pageResult = PDUDcConfigService.getDcConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DcConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出pdu数据采集配置 Excel")
    @PreAuthorize("@ss.hasPermission('pdu:dc-config:export')")
    @OperateLog(type = EXPORT)
    public void exportDcConfigExcel(@Valid DcConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DcConfigDO> list = PDUDcConfigService.getDcConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "pdu数据采集配置.xls", "数据", DcConfigRespVO.class,
                        BeanUtils.toBean(list, DcConfigRespVO.class));
    }

}