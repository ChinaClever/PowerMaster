package cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig;

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

import cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.eqbillconfig.EqBillConfigDO;
import cn.iocoder.yudao.module.pdu.service.eqbillconfig.EqBillConfigService;

@Tag(name = "管理后台 - pdu电量计费方式配置")
@RestController
@RequestMapping("/pdu/eq-bill-config")
@Validated
public class EqBillConfigController {

    @Resource
    private EqBillConfigService eqBillConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建pdu电量计费方式配置")
    @PreAuthorize("@ss.hasPermission('pdu:eq-bill-config:create')")
    public CommonResult<Integer> createEqBillConfig(@Valid @RequestBody EqBillConfigSaveReqVO createReqVO) {
        return success(eqBillConfigService.createEqBillConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新pdu电量计费方式配置")
    @PreAuthorize("@ss.hasPermission('pdu:eq-bill-config:update')")
    public CommonResult<Boolean> updateEqBillConfig(@Valid @RequestBody EqBillConfigSaveReqVO updateReqVO) {
        eqBillConfigService.updateEqBillConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除pdu电量计费方式配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pdu:eq-bill-config:delete')")
    public CommonResult<Boolean> deleteEqBillConfig(@RequestParam("id") Integer id) {
        eqBillConfigService.deleteEqBillConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得pdu电量计费方式配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pdu:eq-bill-config:query')")
    public CommonResult<EqBillConfigRespVO> getEqBillConfig(@RequestParam("id") Integer id) {
        EqBillConfigDO eqBillConfig = eqBillConfigService.getEqBillConfig(id);
        return success(BeanUtils.toBean(eqBillConfig, EqBillConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得pdu电量计费方式配置分页")
    @PreAuthorize("@ss.hasPermission('pdu:eq-bill-config:query')")
    public CommonResult<PageResult<EqBillConfigRespVO>> getEqBillConfigPage(@Valid EqBillConfigPageReqVO pageReqVO) {
        PageResult<EqBillConfigDO> pageResult = eqBillConfigService.getEqBillConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EqBillConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出pdu电量计费方式配置 Excel")
    @PreAuthorize("@ss.hasPermission('pdu:eq-bill-config:export')")
    @OperateLog(type = EXPORT)
    public void exportEqBillConfigExcel(@Valid EqBillConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<EqBillConfigDO> list = eqBillConfigService.getEqBillConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "pdu电量计费方式配置.xls", "数据", EqBillConfigRespVO.class,
                        BeanUtils.toBean(list, EqBillConfigRespVO.class));
    }

}