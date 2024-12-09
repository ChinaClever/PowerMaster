package cn.iocoder.yudao.module.cabinet.controller.admin.temcolor;

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

import cn.iocoder.yudao.module.cabinet.controller.admin.temcolor.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.temcolor.TemColorDO;
import cn.iocoder.yudao.module.cabinet.service.temcolor.TemColorService;

@Tag(name = "管理后台 - 机柜温度颜色")
@RestController
@RequestMapping("/cabinet/tem-color")
@Validated
public class TemColorController {

    @Resource
    private TemColorService temColorService;

    @PostMapping("/create")
    @Operation(summary = "创建机柜温度颜色")

    public CommonResult<Long> createTemColor(@Valid @RequestBody List<TemColorSaveReqVO> colorArr) {
        return success(temColorService.createTemColor(colorArr));
    }

    @PutMapping("/update")
    @Operation(summary = "更新机柜温度颜色")

    public CommonResult<Boolean> updateTemColor(@Valid @RequestBody TemColorSaveReqVO updateReqVO) {
        temColorService.updateTemColor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除机柜温度颜色")
    @Parameter(name = "id", description = "编号", required = true)

    public CommonResult<Boolean> deleteTemColor(@RequestParam("id") Long id) {
        temColorService.deleteTemColor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得机柜温度颜色")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")

    public CommonResult<TemColorRespVO> getTemColor(@RequestParam("id") Long id) {
        TemColorDO temColor = temColorService.getTemColor(id);
        return success(BeanUtils.toBean(temColor, TemColorRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得机柜温度颜色分页")
    public CommonResult<PageResult<TemColorRespVO>> getTemColorPage(@Valid TemColorPageReqVO pageReqVO) {
        PageResult<TemColorDO> pageResult = temColorService.getTemColorPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TemColorRespVO.class));
    }

    @GetMapping("/all")
    @Operation(summary = "获得全部机柜温度颜色")
    public CommonResult<List<TemColorRespVO>> getTemColorAll() {
        List<TemColorDO> pageResult = temColorService.getTemColorAll();
        return success(BeanUtils.toBean(pageResult, TemColorRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机柜温度颜色 Excel")
    @OperateLog(type = EXPORT)
    public void exportTemColorExcel(@Valid TemColorPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TemColorDO> list = temColorService.getTemColorPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机柜温度颜色.xls", "数据", TemColorRespVO.class,
                        BeanUtils.toBean(list, TemColorRespVO.class));
    }

}