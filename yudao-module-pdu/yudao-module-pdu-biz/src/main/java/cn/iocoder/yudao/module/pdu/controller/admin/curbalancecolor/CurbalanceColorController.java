package cn.iocoder.yudao.module.pdu.controller.admin.curbalancecolor;

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

import cn.iocoder.yudao.module.pdu.controller.admin.curbalancecolor.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.CurbalanceColorDO;
import cn.iocoder.yudao.module.pdu.service.curbalancecolor.CurbalanceColorService;

@Tag(name = "管理后台 - PDU不平衡度颜色")
@RestController
@RequestMapping("/pdu/curbalance-color")
@Validated
public class CurbalanceColorController {

    @Resource
    private CurbalanceColorService curbalanceColorService;

    @PostMapping("/create")
    @Operation(summary = "创建PDU不平衡度颜色")

    public CommonResult<Long> createCurbalanceColor(@Valid @RequestBody CurbalanceColorSaveReqVO createReqVO) {
        return success(curbalanceColorService.createCurbalanceColor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新PDU不平衡度颜色")

    public CommonResult<Boolean> updateCurbalanceColor(@Valid @RequestBody CurbalanceColorSaveReqVO updateReqVO) {
        curbalanceColorService.updateCurbalanceColor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除PDU不平衡度颜色")
    @Parameter(name = "id", description = "编号", required = true)

    public CommonResult<Boolean> deleteCurbalanceColor(@RequestParam("id") Long id) {
        curbalanceColorService.deleteCurbalanceColor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得PDU不平衡度颜色")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<CurbalanceColorRespVO> getCurbalanceColor() {
        CurbalanceColorDO curbalanceColor = curbalanceColorService.getCurbalanceColor();
        return success(BeanUtils.toBean(curbalanceColor, CurbalanceColorRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得PDU不平衡度颜色分页")

    public CommonResult<PageResult<CurbalanceColorRespVO>> getCurbalanceColorPage(@Valid CurbalanceColorPageReqVO pageReqVO) {
        PageResult<CurbalanceColorDO> pageResult = curbalanceColorService.getCurbalanceColorPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CurbalanceColorRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出PDU不平衡度颜色 Excel")

    @OperateLog(type = EXPORT)
    public void exportCurbalanceColorExcel(@Valid CurbalanceColorPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CurbalanceColorDO> list = curbalanceColorService.getCurbalanceColorPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "PDU不平衡度颜色.xls", "数据", CurbalanceColorRespVO.class,
                        BeanUtils.toBean(list, CurbalanceColorRespVO.class));
    }

}