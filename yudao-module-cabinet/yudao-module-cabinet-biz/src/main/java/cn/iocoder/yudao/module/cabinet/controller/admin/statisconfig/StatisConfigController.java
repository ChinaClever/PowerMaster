package cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig;

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

import cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.statisconfig.StatisConfigDO;
import cn.iocoder.yudao.module.cabinet.service.statisconfig.StatisConfigService;

@Tag(name = "管理后台 - 机柜计算服务配置")
@RestController
@RequestMapping("/cabinet/statis-config")
@Validated
public class StatisConfigController {

    @Resource
    private StatisConfigService statisConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建机柜计算服务配置")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:create')")
    public CommonResult<Integer> createStatisConfig(@Valid @RequestBody StatisConfigSaveReqVO createReqVO) {
        return success(statisConfigService.createStatisConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新机柜计算服务配置")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:update')")
    public CommonResult<Boolean> updateStatisConfig(@Valid @RequestBody StatisConfigSaveReqVO updateReqVO) {
        statisConfigService.updateStatisConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除机柜计算服务配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:delete')")
    public CommonResult<Boolean> deleteStatisConfig(@RequestParam("id") Integer id) {
        statisConfigService.deleteStatisConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得机柜计算服务配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:query')")
    public CommonResult<StatisConfigRespVO> getStatisConfig(@RequestParam("id") Integer id) {
        StatisConfigDO statisConfig = statisConfigService.getStatisConfig(id);
        return success(BeanUtils.toBean(statisConfig, StatisConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得机柜计算服务配置分页")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:query')")
    public CommonResult<PageResult<StatisConfigRespVO>> getStatisConfigPage(@Valid StatisConfigPageReqVO pageReqVO) {
        PageResult<StatisConfigDO> pageResult = statisConfigService.getStatisConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StatisConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机柜计算服务配置 Excel")
    @PreAuthorize("@ss.hasPermission('cabinet:statis-config:export')")
    @OperateLog(type = EXPORT)
    public void exportStatisConfigExcel(@Valid StatisConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<StatisConfigDO> list = statisConfigService.getStatisConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机柜计算服务配置.xls", "数据", StatisConfigRespVO.class,
                        BeanUtils.toBean(list, StatisConfigRespVO.class));
    }

}