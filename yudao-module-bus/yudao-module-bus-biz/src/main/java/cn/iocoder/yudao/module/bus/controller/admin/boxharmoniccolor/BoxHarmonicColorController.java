package cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor.vo.BoxHarmonicColorPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor.vo.BoxHarmonicColorRespVO;
import cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor.vo.BoxHarmonicColorSaveReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxharmoniccolor.BoxHarmonicColorDO;
import cn.iocoder.yudao.module.bus.service.boxharmoniccolor.BoxHarmonicColorService;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 插接箱谐波颜色")
@RestController
@RequestMapping("/bus/box-harmonic-color")
@Validated
public class BoxHarmonicColorController {

    @Resource
    private BoxHarmonicColorService boxHarmonicColorService;

    @PostMapping("/create")
    @Operation(summary = "创建插接箱谐波颜色")
    public CommonResult<Long> createBoHarmonicColor(@Valid @RequestBody BoxHarmonicColorSaveReqVO createReqVO) {
        return success(boxHarmonicColorService.createBoxHarmonicColor(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新插接箱谐波颜色")
    public CommonResult<Boolean> updateBoxHarmonicolor(@Valid @RequestBody BoxHarmonicColorSaveReqVO updateReqVO) {
        boxHarmonicColorService.updateBoxHarmonicColor(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除插接箱谐波颜色")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteBoxHarmonicColor(@RequestParam("id") Long id) {
        boxHarmonicColorService.deleteBoxHarmonicColor(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得插接箱谐波颜色")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<BoxHarmonicColorRespVO> getBoxHarmonicColor() {
        BoxHarmonicColorDO boxCHarmonicColor = boxHarmonicColorService.getBoxHarmonicColor();
        return success(BeanUtils.toBean(boxCHarmonicColor, BoxHarmonicColorRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得插接箱谐波颜色分页")
    public CommonResult<PageResult<BoxHarmonicColorRespVO>> getBoxHarmonicColorPage(@Valid BoxHarmonicColorPageReqVO pageReqVO) {
        PageResult<BoxHarmonicColorDO> pageResult = boxHarmonicColorService.getBoxHarmonicColorPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxHarmonicColorRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出插接箱谐波颜色 Excel")
    @OperateLog(type = EXPORT)
    public void exportBoxHarmonicColorExcel(@Valid BoxHarmonicColorPageReqVO pageReqVO,
                                              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<BoxHarmonicColorDO> list = boxHarmonicColorService.getBoxHarmonicColorPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "插接箱谐波颜色.xls", "数据", BoxHarmonicColorRespVO.class,
                BeanUtils.toBean(list, BoxHarmonicColorRespVO.class));
    }

}
