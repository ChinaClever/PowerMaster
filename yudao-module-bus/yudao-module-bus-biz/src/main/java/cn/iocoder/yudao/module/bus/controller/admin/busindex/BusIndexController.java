package cn.iocoder.yudao.module.bus.controller.admin.busindex;

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

import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.module.bus.service.busindex.BusIndexService;

@Tag(name = "管理后台 - 始端箱索引")
@RestController
@RequestMapping("/bus/index")
@Validated
public class BusIndexController {

    @Resource
    private BusIndexService indexService;

    @PostMapping("/create")
    @Operation(summary = "创建始端箱索引")

    public CommonResult<Long> createIndex(@Valid @RequestBody BusIndexSaveReqVO createReqVO) {
        return success(indexService.createIndex(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新始端箱索引")

    public CommonResult<Boolean> updateIndex(@Valid @RequestBody BusIndexSaveReqVO updateReqVO) {
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

    public CommonResult<BusIndexRespVO> getIndex(@RequestParam("id") Long id) {
        BusIndexDO index = indexService.getIndex(id);
        return success(BeanUtils.toBean(index, BusIndexRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得始端箱索引分页")
    public CommonResult<PageResult<BusIndexRes>> getIndexPage(@Valid BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexRes> pageResult = indexService.getIndexPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusIndexRes.class));
    }

//    @GetMapping("/export-excel")
//    @Operation(summary = "导出始端箱索引 Excel")
//    @PreAuthorize("@ss.hasPermission('bus:index:export')")
//    @OperateLog(type = EXPORT)
//    public void exportIndexExcel(@Valid BusIndexPageReqVO pageReqVO,
//              HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<BusIndexDO> list = indexService.getIndexPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "始端箱索引.xls", "数据", BusIndexRespVO.class,
//                        BeanUtils.toBean(list, BusIndexRespVO.class));
//    }

}