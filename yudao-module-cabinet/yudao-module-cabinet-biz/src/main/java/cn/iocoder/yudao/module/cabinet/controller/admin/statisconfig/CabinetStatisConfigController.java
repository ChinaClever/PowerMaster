package cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig;

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

import cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.statisconfig.CabinetStatisConfigDO;
import cn.iocoder.yudao.module.cabinet.service.statisconfig.CabinetStatisConfigService;

@Tag(name = "管理后台 - 机柜计算服务配置")
@RestController
@RequestMapping("/cabinet/statis-config")
@Validated
public class CabinetStatisConfigController {

    @Resource
    private CabinetStatisConfigService cabinetStatisConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建机柜计算服务配置")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:create')")
    public CommonResult<Integer> createStatisConfig(@Valid @RequestBody CabinetStatisConfigSaveReqVO createReqVO) {
        return success(cabinetStatisConfigService.createStatisConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新机柜计算服务配置")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:update')")
    public CommonResult<Boolean> updateStatisConfig(@Valid @RequestBody CabinetStatisConfigSaveReqVO updateReqVO) {
        cabinetStatisConfigService.updateStatisConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除机柜计算服务配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:delete')")
    public CommonResult<Boolean> deleteStatisConfig(@RequestParam("id") Integer id) {
        cabinetStatisConfigService.deleteStatisConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得机柜计算服务配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:query')")
    public CommonResult<CabinetStatisConfigRespVO> getStatisConfig(@RequestParam("id") Integer id) {
        CabinetStatisConfigDO statisConfig = cabinetStatisConfigService.getStatisConfig(id);
        return success(BeanUtils.toBean(statisConfig, CabinetStatisConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得机柜计算服务配置分页")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:query')")
    public CommonResult<PageResult<CabinetStatisConfigRespVO>> getStatisConfigPage(@Valid CabinetStatisConfigPageReqVO pageReqVO) {
        PageResult<CabinetStatisConfigDO> pageResult = cabinetStatisConfigService.getStatisConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CabinetStatisConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机柜计算服务配置 Excel")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:export')")
    @OperateLog(type = EXPORT)
    public void exportStatisConfigExcel(@Valid CabinetStatisConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CabinetStatisConfigDO> list = cabinetStatisConfigService.getStatisConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机柜计算服务配置.xls", "数据", CabinetStatisConfigRespVO.class,
                        BeanUtils.toBean(list, CabinetStatisConfigRespVO.class));
    }

}