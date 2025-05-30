package cn.iocoder.yudao.module.cabinet.controller.admin.index;

import org.apache.poi.hpsf.Decimal;
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

    @GetMapping("/report/powByType")
    @Operation(summary = "获得机柜报表数据")
    public CommonResult<Map> getReportPowDataByIdAndType(String Id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime,Integer dataType) {
        return success(indexService.getReportPowDataByIdAndType(Id,timeType,oldTime,newTime,dataType));
    }

    @GetMapping("/report/pfline")
    @Operation(summary = "获得机柜报表数据")
    public CommonResult<Map> getCabinetPFLine(String Id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(indexService.getCabinetPFLine(Id,timeType,oldTime,newTime));
    }

    @GetMapping("/report/pfLineByType")
    @Operation(summary = "获得机柜报表数据")
    public CommonResult<Map> getCabinetPFLineByType(String Id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime,Integer dataType) {
        return success(indexService.getCabinetPFLineByType(Id,timeType,oldTime,newTime,dataType));
    }

    @GetMapping("/env/ice")
    @Operation(summary = "获得机柜冷通道温度和湿度")
    public CommonResult<Map> getCabinetEnvIceTemAndHumData(String id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(indexService.getCabinetEnvIceTemAndHumData(id,timeType,oldTime,newTime));
    }

    @GetMapping("/env/iceByType")
    @Operation(summary = "获得机柜冷通道温度和湿度")
    public CommonResult<Map> getCabinetEnvIceTemAndHumDataByType(String id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime,Integer dataType) {
        return success(indexService.getCabinetEnvIceTemAndHumDataByType(id,timeType,oldTime,newTime,dataType));
    }

    @GetMapping("/env/getIceAndHot")
    @Operation(summary = "获得机柜冷通道温度和湿度")
    public CommonResult<Map> getCabinetEnvIceAndHotTemAndHumData(String id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime,Integer dataType) {
        return success(indexService.getCabinetEnvIceAndHotTemAndHumData(id,timeType,oldTime,newTime,dataType));
    }

    @GetMapping("/env/hot")
    @Operation(summary = "获得机柜热通道温度和湿度")
    public CommonResult<Map> getCabinetEnvHotTemAndHumData(String id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(indexService.getCabinetEnvHotTemAndHumData(id,timeType,oldTime,newTime));
    }
    @GetMapping("/env/hotByType")
    @Operation(summary = "获得机柜热通道温度和湿度")
    public CommonResult<Map> getCabinetEnvHotTemAndHumDataByType(String id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime,Integer dataType) {
        return success(indexService.getCabinetEnvHotTemAndHumDataByType(id,timeType,oldTime,newTime,dataType));
    }

    @GetMapping("/idList")
    @Operation(summary = "获得机柜id列表")
    public List<Integer> idList() {
        return indexService.idList();
    }

    @GetMapping("/getRackByCabinet")
    @Operation(summary = "根据机柜id获取机架")
    public CommonResult<List<CabinetRackRspVO>> getRackByCabinet(@RequestParam("id") Integer id){
        return success(indexService.getRackByCabinet(id));
    }

    @GetMapping("/getEleByCabinet")
    @Operation(summary = "根据机柜id获取耗电量")
    public CommonResult<Map<String, Double>> getEleByCabinet(String Id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) throws IOException {
        return success(indexService.getEleByCabinet(Id,timeType,oldTime,newTime));
    }

    @GetMapping("/getEleByAisle")
    @Operation(summary = "根据机柜id获取耗电量")
    public CommonResult<Map> getEleByAisle(String id,Integer timeType,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) throws IOException {
        return success(indexService.getEleByAisle(id,timeType,oldTime,newTime));
    }
}