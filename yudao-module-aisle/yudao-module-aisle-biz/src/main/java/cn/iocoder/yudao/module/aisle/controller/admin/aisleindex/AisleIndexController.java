package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.servlet.http.HttpServletResponse;
import javax.validation.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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
        return success(pageResult);
    }

    @PostMapping("/powerpage")
    @Operation(summary = "获得通道列电力索引分页")
    public CommonResult<PageResult<AislePowerRes>> getPowerPage(@RequestBody AisleIndexPageReqVO pageReqVO) {
        PageResult<AislePowerRes> pageResult = indexService.getPowerPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/line/page")
    @Operation(summary = "获得通道列需量分页")
    public CommonResult<PageResult<AisleLineMaxRes>> getAisleLineMaxPage(@RequestBody AisleIndexPageReqVO pageReqVO) {
        return success(indexService.getAisleLineMaxPage(pageReqVO));
    }

    @Operation(summary = "柜列需量ES数据图表")
    @PostMapping("/line/cur")
    public CommonResult<AisleLineResBase> getAisleLineCurLine(@RequestBody AisleIndexPageReqVO pageReqVO) {
        AisleLineResBase pageResult = indexService.getAisleLineCurLine(pageReqVO);
        return success(pageResult);
    }

    /**
     * 通道列用能页面
     *
     * @param pageReqVO
     */
    @Operation(summary = "通道列用能列表分页")
    @PostMapping("/eq/page")
    public CommonResult<PageResult<AisleEQRes>> getEqPage(@RequestBody AisleIndexPageReqVO pageReqVO) {
//        PageResult<AisleEQRes> pageResult = indexService.getEqPage(pageReqVO);

        PageResult<AisleEQRes> pageResult;
        if (ObjectUtil.isEmpty(pageReqVO.getTimeGranularity()) || !CollectionUtils.isEmpty(pageReqVO.getAisleIds()) || ObjectUtil.isNotEmpty(pageReqVO.getName())){
            pageResult =  indexService.getEqPage(pageReqVO);
        }else {
            pageResult = indexService.getEqPage1(pageReqVO);
            if (ObjectUtil.isEmpty(pageResult)){
                pageResult =  indexService.getEqPage(pageReqVO);
            }
        }
        return success(pageResult);
    }

    /**
     * 柜列有功功率趋势
     *
     * @param id 柜列id
     */
    @Operation(summary = "柜列有功功率趋势")
    @GetMapping("/activePowTrend")
    public CommonResult<AisleActivePowDTO> activePowTrend(@Param("id") int id) {
        AislePowVo vo = new AislePowVo();
        vo.setId(id);
        AisleActivePowDTO dto = indexService.getActivePow(vo);
        return success(dto);
    }

    /**
     * 柜列用能趋势
     *
     * @param id 柜列id
     */
    @Operation(summary = "柜列用能趋势")
    @GetMapping("/eleTrend")
    public CommonResult<List<AisleEqTrendDTO>> eleTrend(@Param("id") int id, @Param("type") String type) {
        List<AisleEqTrendDTO> dto = indexService.eqTrend(id, type);
        return success(dto);
    }

    /**
     * 柜列用能环比
     *
     * @param id 柜列id
     */
    @Operation(summary = "柜列用能环比")
    @GetMapping("/eleChain")
    public CommonResult<AisleEleChainDTO> eleChain(@Param("id") int id) {
        AisleEleChainDTO dto = indexService.getEleChain(id);
        return success(dto);
    }

    @PostMapping("/buspfpage")
    @Operation(summary = "获得通道列功率因素分页")
    public CommonResult<PageResult<AislePfRes>> getAislePFPage(@RequestBody AisleIndexPageReqVO pageReqVO) {
        PageResult<AislePfRes> pageResult = indexService.getAislePFPage(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "柜列功率因素详情分页")
    @PostMapping("/pf/detail")
    public CommonResult<Map> getAislePFDetail(@RequestBody AisleIndexPageReqVO pageReqVO) {
        return success(indexService.getAislePFDetail(pageReqVO));
    }

    @Operation(summary = "柜列功率因素详情导出")
    @PostMapping("/pf/detail/excel")
    public void getAislePFDetailExcel(@RequestBody AisleIndexPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        Map aislePFDetail = indexService.getAislePFDetail(pageReqVO);
        List<AislePFTableRes> res =(List<AislePFTableRes>) aislePFDetail.get("table");
        if (ObjectUtil.isEmpty(res)){
            return;
        }
        if (ObjectUtil.isNotEmpty(pageReqVO.getLocation())) {
            res.forEach(iter -> {
                iter.setLocation(pageReqVO.getLocation());
            });
        }
        ExcelUtils.write(response, "通道列详情.xls", "通道列详情", AislePFTableRes.class, res);
    }


    @GetMapping("/devKeyList")
    @Operation(summary = "获得通道列devKey列表")
    public List<Integer> getDevKeyList() {
        return indexService.getDevKeyList();
    }

    @PostMapping("/balancepage")
    @Operation(summary = "获得通道列平衡分页")
    public CommonResult<PageResult<AisleBalanceRes>> getAisleBalancePage(@RequestBody AisleIndexPageReqVO pageReqVO) {
        PageResult<AisleBalanceRes> pageResult = indexService.getAisleBalancePage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/report/ele")
    @Operation(summary = "获得通道列报表数据")
    public CommonResult<Map> getReportConsumeDataById(@RequestBody AisleIndexPageReqVO pageReqVO) {
        return success(indexService.getReportConsumeDataById(pageReqVO.getId(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pow")
    @Operation(summary = "获得通道列报表数据")
    public CommonResult<Map> getReportPowDataById(@RequestBody AisleIndexPageReqVO pageReqVO)  {
        return success(indexService.getReportPowDataById(pageReqVO.getId(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pfline")
    @Operation(summary = "获得通道列报表数据")
    public CommonResult<Map> getAislePFLine(@RequestBody AisleIndexPageReqVO pageReqVO)  {
        return success(indexService.getAislePFLine(pageReqVO.getId(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @GetMapping("/idList")
    @Operation(summary = "获得通道列id列表")
    public List<Integer> idList() {
        return indexService.idList();
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