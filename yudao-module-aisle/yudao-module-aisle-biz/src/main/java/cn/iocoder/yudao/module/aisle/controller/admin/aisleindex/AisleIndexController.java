package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
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

import cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo.*;
import cn.iocoder.yudao.module.aisle.service.aisleindex.AisleIndexService;

@Tag(name = "管理后台 - 通道列")
@RestController
@RequestMapping("/aisle/index")
@Validated
public class AisleIndexController {

    @Resource
    private AisleIndexService indexService;

    @PostMapping("/create")
    @Operation(summary = "创建通道列")
    @PreAuthorize("@ss.hasPermission('aisle:index:create')")
    public CommonResult<Integer> createIndex(@Valid @RequestBody AisleIndexSaveReqVO createReqVO) {
        return success(indexService.createIndex(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新通道列")
    @PreAuthorize("@ss.hasPermission('aisle:index:update')")
    public CommonResult<Boolean> updateIndex(@Valid @RequestBody AisleIndexSaveReqVO updateReqVO) {
        indexService.updateIndex(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除通道列")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aisle:index:delete')")
    public CommonResult<Boolean> deleteIndex(@RequestParam("id") Integer id) {
        indexService.deleteIndex(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得通道列")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aisle:index:query')")
    public CommonResult<AisleIndexRespVO> getIndex(@RequestParam("id") Integer id) {
        AisleIndexDO index = indexService.getIndex(id);
        return success(BeanUtils.toBean(index, AisleIndexRespVO.class));
    }

    @PostMapping("/page")
    @Operation(summary = "获得通道列分页")
    public CommonResult<PageResult<AisleIndexRes>> getIndexPage(@RequestBody AisleIndexPageReqVO pageReqVO) {
        PageResult<AisleIndexRes> pageResult = indexService.getIndexPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AisleIndexRes.class));
    }

    @GetMapping("/devKeyList")
    @Operation(summary = "获得通道列devKey列表")
    public List<Integer> getDevKeyList() {
        return indexService.getDevKeyList();
    }

//    @GetMapping("/export-excel")
//    @Operation(summary = "导出通道列 Excel")
//    @PreAuthorize("@ss.hasPermission('aisle:index:export')")
//    @OperateLog(type = EXPORT)
//    public void exportIndexExcel(@Valid AisleIndexPageReqVO pageReqVO,
//              HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<AisleIndex> list = indexService.getIndexPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "通道列.xls", "数据", AisleIndexRespVO.class,
//                        BeanUtils.toBean(list, AisleIndexRespVO.class));
//    }

}