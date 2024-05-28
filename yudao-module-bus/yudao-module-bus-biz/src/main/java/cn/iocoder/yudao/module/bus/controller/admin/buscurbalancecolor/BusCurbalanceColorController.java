package cn.iocoder.yudao.module.bus.controller.admin.buscurbalancecolor;

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

import cn.iocoder.yudao.module.bus.controller.admin.buscurbalancecolor.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.buscurbalancecolor.BusCurbalanceColorDO;
import cn.iocoder.yudao.module.bus.service.buscurbalancecolor.BusCurbalanceColorService;

@Tag(name = "管理后台 - 母线不平衡度颜色")
@RestController
@RequestMapping("/bus/curbalance-color")
@Validated
public class BusCurbalanceColorController {

    @Resource
    private BusCurbalanceColorService curbalanceColorService;

    @PostMapping("/create")
    @Operation(summary = "创建母线不平衡度颜色")

    public CommonResult<Long> createCurbalanceColor(@Valid @RequestBody BusCurbalanceColorSaveReqVO createReqVO) {
        return success(curbalanceColorService.createCurbalanceColor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新母线不平衡度颜色")

    public CommonResult<Boolean> updateCurbalanceColor(@Valid @RequestBody BusCurbalanceColorSaveReqVO updateReqVO) {
        curbalanceColorService.updateCurbalanceColor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除母线不平衡度颜色")
    @Parameter(name = "id", description = "编号", required = true)

    public CommonResult<Boolean> deleteCurbalanceColor(@RequestParam("id") Long id) {
        curbalanceColorService.deleteCurbalanceColor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得母线不平衡度颜色")
    public CommonResult<BusCurbalanceColorRespVO> getCurbalanceColor() {
        BusCurbalanceColorDO curbalanceColor = curbalanceColorService.getCurbalanceColor();
        return success(BeanUtils.toBean(curbalanceColor, BusCurbalanceColorRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得母线不平衡度颜色分页")
    public CommonResult<PageResult<BusCurbalanceColorRespVO>> getCurbalanceColorPage(@Valid BusCurbalanceColorPageReqVO pageReqVO) {
        PageResult<BusCurbalanceColorDO> pageResult = curbalanceColorService.getCurbalanceColorPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusCurbalanceColorRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出母线不平衡度颜色 Excel")
    @OperateLog(type = EXPORT)
    public void exportCurbalanceColorExcel(@Valid BusCurbalanceColorPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<BusCurbalanceColorDO> list = curbalanceColorService.getCurbalanceColorPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "母线不平衡度颜色.xls", "数据", BusCurbalanceColorRespVO.class,
                        BeanUtils.toBean(list, BusCurbalanceColorRespVO.class));
    }

}