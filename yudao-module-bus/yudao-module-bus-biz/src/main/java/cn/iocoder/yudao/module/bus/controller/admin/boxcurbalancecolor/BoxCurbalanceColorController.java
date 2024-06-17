package cn.iocoder.yudao.module.bus.controller.admin.boxcurbalancecolor;

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

import cn.iocoder.yudao.module.bus.controller.admin.boxcurbalancecolor.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxcurbalancecolor.BoxCurbalanceColorDO;
import cn.iocoder.yudao.module.bus.service.boxcurbalancecolor.BoxCurbalanceColorService;

@Tag(name = "管理后台 - 插接箱不平衡度颜色")
@RestController
@RequestMapping("/bus/box-curbalance-color")
@Validated
public class BoxCurbalanceColorController {

    @Resource
    private BoxCurbalanceColorService boxCurbalanceColorService;

    @PostMapping("/create")
    @Operation(summary = "创建插接箱不平衡度颜色")
    public CommonResult<Long> createBoxCurbalanceColor(@Valid @RequestBody BoxCurbalanceColorSaveReqVO createReqVO) {
        return success(boxCurbalanceColorService.createBoxCurbalanceColor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新插接箱不平衡度颜色")
    public CommonResult<Boolean> updateBoxCurbalanceColor(@Valid @RequestBody BoxCurbalanceColorSaveReqVO updateReqVO) {
        boxCurbalanceColorService.updateBoxCurbalanceColor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除插接箱不平衡度颜色")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteBoxCurbalanceColor(@RequestParam("id") Long id) {
        boxCurbalanceColorService.deleteBoxCurbalanceColor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得插接箱不平衡度颜色")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<BoxCurbalanceColorRespVO> getBoxCurbalanceColor(@RequestParam("id") Long id) {
        BoxCurbalanceColorDO boxCurbalanceColor = boxCurbalanceColorService.getBoxCurbalanceColor(id);
        return success(BeanUtils.toBean(boxCurbalanceColor, BoxCurbalanceColorRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得插接箱不平衡度颜色分页")
    public CommonResult<PageResult<BoxCurbalanceColorRespVO>> getBoxCurbalanceColorPage(@Valid BoxCurbalanceColorPageReqVO pageReqVO) {
        PageResult<BoxCurbalanceColorDO> pageResult = boxCurbalanceColorService.getBoxCurbalanceColorPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxCurbalanceColorRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出插接箱不平衡度颜色 Excel")
    @OperateLog(type = EXPORT)
    public void exportBoxCurbalanceColorExcel(@Valid BoxCurbalanceColorPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<BoxCurbalanceColorDO> list = boxCurbalanceColorService.getBoxCurbalanceColorPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "插接箱不平衡度颜色.xls", "数据", BoxCurbalanceColorRespVO.class,
                        BeanUtils.toBean(list, BoxCurbalanceColorRespVO.class));
    }

}