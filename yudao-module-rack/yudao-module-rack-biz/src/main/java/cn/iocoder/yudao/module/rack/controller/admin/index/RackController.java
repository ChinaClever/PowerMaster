package cn.iocoder.yudao.module.rack.controller.admin.index;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

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

import cn.iocoder.yudao.module.rack.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.rack.dal.dataobject.index.RackDO;
import cn.iocoder.yudao.module.rack.service.index.RackService;

@Tag(name = "管理后台 - 机架索引")
@RestController
@RequestMapping("/rack/index")
@Validated
public class RackController {

    @Resource
    private RackService rackService;

    @PostMapping("/create")
    @Operation(summary = "创建机架索引")

    public CommonResult<Integer> createIndex(@Valid @RequestBody IndexSaveReqVO createReqVO) {
        return success(rackService.createIndex(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新机架索引")

    public CommonResult<Boolean> updateIndex(@Valid @RequestBody IndexSaveReqVO updateReqVO) {
        rackService.updateIndex(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除机架索引")
    @Parameter(name = "id", description = "编号", required = true)

    public CommonResult<Boolean> deleteIndex(@RequestParam("id") Integer id) {
        rackService.deleteIndex(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得机架索引")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")

    public CommonResult<IndexRespVO> getIndex(@RequestParam("id") Integer id) {
        RackDO index = rackService.getIndex(id);
        return success(BeanUtils.toBean(index, IndexRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得机架索引分页")

    public CommonResult<PageResult<IndexRespVO>> getIndexPage(@Valid IndexPageReqVO pageReqVO) {
        PageResult<RackDO> pageResult = rackService.getIndexPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IndexRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机架索引 Excel")

    @OperateLog(type = EXPORT)
    public void exportIndexExcel(@Valid IndexPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RackDO> list = rackService.getIndexPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机架索引.xls", "数据", IndexRespVO.class,
                        BeanUtils.toBean(list, IndexRespVO.class));
    }

    @GetMapping("/report/ele")
    @Operation(summary = "获得机架报表数据")
    public CommonResult<Map> getReportConsumeDataById(String id, Integer timeType, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(rackService.getReportConsumeDataById(id,timeType,oldTime,newTime));
    }

    @GetMapping("/report/pow")
    @Operation(summary = "获得机架报表数据")
    public CommonResult<Map> getReportPowDataById(String id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(rackService.getReportPowDataById(id,timeType,oldTime,newTime));
    }

    @GetMapping("/report/pfline")
    @Operation(summary = "获得机架报表数据")
    public CommonResult<Map> getRoomPFLine(String id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime)  {
        return success(rackService.getRoomPFLine(id,timeType,oldTime,newTime));
    }

    @GetMapping("/redisData")
    @Operation(summary = "获得机架索引")
    @Parameter(name = "Id", description = "编号", required = true, example = "1024")
    public String getRackRedisById(@RequestParam("id") Integer Id) {
        return rackService.getRackRedisById(Id);
    }

    @GetMapping("/idList")
    @Operation(summary = "获得机架id列表")
    public List<Integer> idList() {
        return rackService.idList();
    }
}