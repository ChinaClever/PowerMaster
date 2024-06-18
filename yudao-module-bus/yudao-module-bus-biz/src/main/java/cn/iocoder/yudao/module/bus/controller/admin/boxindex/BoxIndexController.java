package cn.iocoder.yudao.module.bus.controller.admin.boxindex;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.*;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.module.bus.service.boxindex.BoxIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 始端箱索引")
@RestController
@RequestMapping("/box/index")
@Validated
public class BoxIndexController {

    @Resource
    private BoxIndexService indexService;

    @PostMapping("/create")
    @Operation(summary = "创建始端箱索引")

    public CommonResult<Long> createIndex(@Valid @RequestBody BoxIndexSaveReqVO createReqVO) {
        return success(indexService.createIndex(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新始端箱索引")

    public CommonResult<Boolean> updateIndex(@Valid @RequestBody BoxIndexSaveReqVO updateReqVO) {
        indexService.updateIndex(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除始端箱索引")
    @Parameter(name = "id", description = "编号", required = true)

    public CommonResult<Boolean> deleteIndex(@RequestParam("id") Long id) {
        indexService.deleteIndex(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得始端箱索引")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")

    public CommonResult<BoxIndexRespVO> getIndex(@RequestParam("id") Long id) {
        BoxIndex index = indexService.getIndex(id);
        return success(BeanUtils.toBean(index, BoxIndexRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得始端箱索引分页")
    public CommonResult<PageResult<BoxIndexRes>> getIndexPage(@Valid BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndexRes> pageResult = indexService.getIndexPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxIndexRes.class));
    }

    @GetMapping("/boxpage")
    @Operation(summary = "获得始端箱索引分页")
    public CommonResult<PageResult<BoxRedisDataRes>> getBoxPage(@Valid BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxRedisDataRes> pageResult = indexService.getBoxRedisPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxRedisDataRes.class));
    }

    @GetMapping("/boxtempage")
    @Operation(summary = "获得始端箱索引分页")
    public CommonResult<PageResult<BoxTemRes>> getBoxTemPage(@Valid BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxTemRes> pageResult = indexService.getBoxTemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxTemRes.class));
    }

    @GetMapping("/boxpfpage")
    @Operation(summary = "获得始端箱索引分页")
    public CommonResult<PageResult<BoxPFRes>> getBoxPFPage(@Valid BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxPFRes> pageResult = indexService.getBoxPFPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxPFRes.class));
    }

    @GetMapping("/boxharmonicpage")
    @Operation(summary = "获得始端箱索引分页")
    public CommonResult<PageResult<BoxHarmonicRes>> getBoxHarmonicPage(@Valid BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxHarmonicRes> pageResult = indexService.getBoxHarmonicPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxHarmonicRes.class));
    }

    /**
     * 机柜用能页面
     *
     * @param pageReqVO
     */
    @Operation(summary = "机柜用能列表分页")
    @PostMapping("/eq/page")
    public CommonResult<PageResult<BoxIndexDTO>> getEqPage(@RequestBody BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndexDTO> pageResult = indexService.getEqPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/balance")
    @Operation(summary = "获得始端箱索引分页")
    public CommonResult<PageResult<BoxBalanceDataRes>> getBoxBalancePage(@Valid BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxBalanceDataRes> pageResult = indexService.getBoxBalancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxBalanceDataRes.class));
    }

//    @GetMapping("/export-excel")
//    @Operation(summary = "导出始端箱索引 Excel")
//    @PreAuthorize("@ss.hasPermission('box:index:export')")
//    @OperateLog(type = EXPORT)
//    public void exportIndexExcel(@Valid BoxIndexPageReqVO pageReqVO,
//              HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<BoxIndexDO> list = indexService.getIndexPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "始端箱索引.xls", "数据", BoxIndexRespVO.class,
//                        BeanUtils.toBean(list, BoxIndexRespVO.class));
//    }

}