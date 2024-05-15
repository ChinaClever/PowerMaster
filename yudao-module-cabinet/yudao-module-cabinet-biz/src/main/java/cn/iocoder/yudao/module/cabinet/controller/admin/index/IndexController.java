package cn.iocoder.yudao.module.cabinet.controller.admin.index;

import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDateTime;
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

import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.module.cabinet.service.index.IndexService;

@Tag(name = "管理后台 - 机柜索引")
@RestController
@RequestMapping("/cabinet/index")
@Validated
public class IndexController {

    @Resource
    private IndexService indexService;

    @PostMapping("/create")
    @Operation(summary = "创建机柜索引")
    @PreAuthorize("@ss.hasPermission('cabinet:index:create')")
    public CommonResult<Integer> createIndex(@Valid @RequestBody IndexSaveReqVO createReqVO) {
        return success(indexService.createIndex(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新机柜索引")
    @PreAuthorize("@ss.hasPermission('cabinet:index:update')")
    public CommonResult<Boolean> updateIndex(@Valid @RequestBody IndexSaveReqVO updateReqVO) {
        indexService.updateIndex(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除机柜索引")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('cabinet:index:delete')")
    public CommonResult<Boolean> deleteIndex(@RequestParam("id") Integer id) {
        indexService.deleteIndex(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得机柜索引")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cabinet:index:query')")
    public CommonResult<IndexRespVO> getIndex(@RequestParam("id") Integer id) {
        IndexDO index = indexService.getIndex(id);
        return success(BeanUtils.toBean(index, IndexRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得机柜索引分页")
    @PreAuthorize("@ss.hasPermission('cabinet:index:query')")
    public CommonResult<PageResult<IndexRespVO>> getIndexPage(@Valid IndexPageReqVO pageReqVO) {
        PageResult<IndexDO> pageResult = indexService.getIndexPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IndexRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机柜索引 Excel")
    @PreAuthorize("@ss.hasPermission('cabinet:index:export')")
    @OperateLog(type = EXPORT)
    public void exportIndexExcel(@Valid IndexPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IndexDO> list = indexService.getIndexPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机柜索引.xls", "数据", IndexRespVO.class,
                        BeanUtils.toBean(list, IndexRespVO.class));
    }


    @GetMapping("/report/ele")
    @Operation(summary = "获得机柜报表数据")
    public CommonResult<Map> getReportConsumeDataById(String Id, Integer timeType, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(indexService.getReportConsumeDataById(Id,timeType,oldTime,newTime));
    }

    @GetMapping("/report/pow")
    @Operation(summary = "获得机柜报表数据")
    public CommonResult<Map> getReportPowDataById(String Id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(indexService.getReportPowDataById(Id,timeType,oldTime,newTime));
    }

//    @GetMapping("/report/outlet")
//    @Operation(summary = "获得PDU报表数据")
//    public CommonResult<Map> getReportOutLetDataById(String Id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
//        return success(indexService.getReportOutLetDataById(Id,timeType,oldTime,newTime));
//    }

    @GetMapping("/report/tem")
    @Operation(summary = "获得机柜报表数据")
    public CommonResult<Map> getReportTemDataById(String Id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(indexService.getReportTemDataById(Id,timeType,oldTime,newTime));
    }

    @GetMapping("/env/page")
    @Operation(summary = "获得机柜索引分页")
    public CommonResult<PageResult<CabinetEnvAndHumRes>> getCabinetEnvPage(@Valid IndexPageReqVO pageReqVO) {
        return success(indexService.getCabinetEnvPage(pageReqVO));
    }
}