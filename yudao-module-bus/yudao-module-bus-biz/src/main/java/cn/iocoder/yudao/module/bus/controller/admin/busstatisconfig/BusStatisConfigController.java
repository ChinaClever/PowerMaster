package cn.iocoder.yudao.module.bus.controller.admin.busstatisconfig;

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

import cn.iocoder.yudao.module.bus.controller.admin.busstatisconfig.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busstatisconfig.BusStatisConfigDO;
import cn.iocoder.yudao.module.bus.service.busstatisconfig.BusStatisConfigService;

@Tag(name = "管理后台 - 母线计算服务配置")
@RestController
@RequestMapping("/bus/statis-config")
@Validated
public class BusStatisConfigController {

    @Resource
    private BusStatisConfigService statisConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建母线计算服务配置")
    @PreAuthorize("@ss.hasPermission('bus:statis-config:create')")
    public CommonResult<Integer> createStatisConfig(@Valid @RequestBody BusStatisConfigSaveReqVO createReqVO) {
        return success(statisConfigService.createStatisConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新母线计算服务配置")
    @PreAuthorize("@ss.hasPermission('bus:statis-config:update')")
    public CommonResult<Boolean> updateStatisConfig(@Valid @RequestBody BusStatisConfigSaveReqVO updateReqVO) {
        statisConfigService.updateStatisConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除母线计算服务配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('bus:statis-config:delete')")
    public CommonResult<Boolean> deleteStatisConfig(@RequestParam("id") Integer id) {
        statisConfigService.deleteStatisConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得母线计算服务配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bus:statis-config:query')")
    public CommonResult<BusStatisConfigRespVO> getStatisConfig(@RequestParam("id") Integer id) {
        BusStatisConfigDO statisConfig = statisConfigService.getStatisConfig(id);
        return success(BeanUtils.toBean(statisConfig, BusStatisConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得母线计算服务配置分页")
    @PreAuthorize("@ss.hasPermission('bus:statis-config:query')")
    public CommonResult<PageResult<BusStatisConfigRespVO>> getStatisConfigPage(@Valid BusStatisConfigPageReqVO pageReqVO) {
        PageResult<BusStatisConfigDO> pageResult = statisConfigService.getStatisConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusStatisConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出母线计算服务配置 Excel")
    @PreAuthorize("@ss.hasPermission('bus:statis-config:export')")
    @OperateLog(type = EXPORT)
    public void exportStatisConfigExcel(@Valid BusStatisConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<BusStatisConfigDO> list = statisConfigService.getStatisConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "母线计算服务配置.xls", "数据", BusStatisConfigRespVO.class,
                        BeanUtils.toBean(list, BusStatisConfigRespVO.class));
    }

}