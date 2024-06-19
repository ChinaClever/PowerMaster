package cn.iocoder.yudao.module.bus.controller.admin.busdcconfig;

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

import cn.iocoder.yudao.module.bus.controller.admin.busdcconfig.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busdcconfig.BusDcConfigDO;
import cn.iocoder.yudao.module.bus.service.busdcconfig.BusDcConfigService;

@Tag(name = "管理后台 - 母线数据采集配置")
@RestController
@RequestMapping("/bus/dc-config")
@Validated
public class BusDcConfigController {

    @Resource
    private BusDcConfigService dcConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建母线数据采集配置")
    @PreAuthorize("@ss.hasPermission('bus:dc-config:create')")
    public CommonResult<Short> createDcConfig(@Valid @RequestBody BusDcConfigSaveReqVO createReqVO) {
        return success(dcConfigService.createDcConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新母线数据采集配置")
    @PreAuthorize("@ss.hasPermission('bus:dc-config:update')")
    public CommonResult<Boolean> updateDcConfig(@Valid @RequestBody BusDcConfigSaveReqVO updateReqVO) {
        dcConfigService.updateDcConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除母线数据采集配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('bus:dc-config:delete')")
    public CommonResult<Boolean> deleteDcConfig(@RequestParam("id") Short id) {
        dcConfigService.deleteDcConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得母线数据采集配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bus:dc-config:query')")
    public CommonResult<BusDcConfigRespVO> getDcConfig(@RequestParam("id") Short id) {
        BusDcConfigDO dcConfig = dcConfigService.getDcConfig(id);
        return success(BeanUtils.toBean(dcConfig, BusDcConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得母线数据采集配置分页")
    @PreAuthorize("@ss.hasPermission('bus:dc-config:query')")
    public CommonResult<PageResult<BusDcConfigRespVO>> getDcConfigPage(@Valid BusDcConfigPageReqVO pageReqVO) {
        PageResult<BusDcConfigDO> pageResult = dcConfigService.getDcConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusDcConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出母线数据采集配置 Excel")
    @PreAuthorize("@ss.hasPermission('bus:dc-config:export')")
    @OperateLog(type = EXPORT)
    public void exportDcConfigExcel(@Valid BusDcConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<BusDcConfigDO> list = dcConfigService.getDcConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "母线数据采集配置.xls", "数据", BusDcConfigRespVO.class,
                        BeanUtils.toBean(list, BusDcConfigRespVO.class));
    }

}