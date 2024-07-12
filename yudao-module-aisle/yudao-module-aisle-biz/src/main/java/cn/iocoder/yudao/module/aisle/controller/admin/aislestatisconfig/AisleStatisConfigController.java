package cn.iocoder.yudao.module.aisle.controller.admin.aislestatisconfig;

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

import cn.iocoder.yudao.module.aisle.controller.admin.aislestatisconfig.vo.*;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aislestatisconfig.AisleStatisConfigDO;
import cn.iocoder.yudao.module.aisle.service.aislestatisconfig.AisleStatisConfigService;

@Tag(name = "管理后台 - 柜列计算服务配置")
@RestController
@RequestMapping("/aisle/statis-config")
@Validated
public class AisleStatisConfigController {

    @Resource
    private AisleStatisConfigService statisConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建柜列计算服务配置")
    @PreAuthorize("@ss.hasPermission('aisle:statis-config:create')")
    public CommonResult<Integer> createStatisConfig(@Valid @RequestBody AisleStatisConfigSaveReqVO createReqVO) {
        return success(statisConfigService.createStatisConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新柜列计算服务配置")
    @PreAuthorize("@ss.hasPermission('aisle:statis-config:update')")
    public CommonResult<Boolean> updateStatisConfig(@Valid @RequestBody AisleStatisConfigSaveReqVO updateReqVO) {
        statisConfigService.updateStatisConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除柜列计算服务配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aisle:statis-config:delete')")
    public CommonResult<Boolean> deleteStatisConfig(@RequestParam("id") Integer id) {
        statisConfigService.deleteStatisConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得柜列计算服务配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aisle:statis-config:query')")
    public CommonResult<AisleStatisConfigRespVO> getStatisConfig(@RequestParam("id") Integer id) {
        AisleStatisConfigDO statisConfig = statisConfigService.getStatisConfig(id);
        return success(BeanUtils.toBean(statisConfig, AisleStatisConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得柜列计算服务配置分页")
    @PreAuthorize("@ss.hasPermission('aisle:statis-config:query')")
    public CommonResult<PageResult<AisleStatisConfigRespVO>> getStatisConfigPage(@Valid AisleStatisConfigPageReqVO pageReqVO) {
        PageResult<AisleStatisConfigDO> pageResult = statisConfigService.getStatisConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AisleStatisConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出柜列计算服务配置 Excel")
    @PreAuthorize("@ss.hasPermission('aisle:statis-config:export')")
    @OperateLog(type = EXPORT)
    public void exportStatisConfigExcel(@Valid AisleStatisConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AisleStatisConfigDO> list = statisConfigService.getStatisConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "柜列计算服务配置.xls", "数据", AisleStatisConfigRespVO.class,
                        BeanUtils.toBean(list, AisleStatisConfigRespVO.class));
    }

}