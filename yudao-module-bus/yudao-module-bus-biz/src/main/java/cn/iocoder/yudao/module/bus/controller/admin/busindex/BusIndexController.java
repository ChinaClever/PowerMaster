package cn.iocoder.yudao.module.bus.controller.admin.busindex;

import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusTemDetailRes;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.module.bus.service.busindex.BusIndexService;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/page")
    @Operation(summary = "获得始端箱索引分页")
    public CommonResult<PageResult<BusIndexRes>> getIndexPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexRes> pageResult = indexService.getIndexPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusIndexRes.class));
    }

    @PostMapping("/line/page")
    @Operation(summary = "获得始端箱需量分页")
    public CommonResult<PageResult<BusLineRes>> getBusLineMaxPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusLineDevicePage(pageReqVO));
    }

    @Operation(summary = "始端箱需量ES数据图表")
    @PostMapping("/line/cur")
    public CommonResult<BusLineResBase> getBusLineCurLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusLineResBase pageResult = indexService.getBusLineCurLine(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/powerpage")
    @Operation(summary = "获得始端箱索引分页")
    public CommonResult<PageResult<BusRedisDataRes>> getBusPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        PageResult<BusRedisDataRes> pageResult = indexService.getBusRedisPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusRedisDataRes.class));
    }

    @PostMapping("/bustempage")
    @Operation(summary = "获得始端箱温度分页")
    public CommonResult<PageResult<BusTemRes>> getBusTemPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        PageResult<BusTemRes> pageResult = indexService.getBusTemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusTemRes.class));
    }

    @Operation(summary = "始端箱温度详情分页")
    @PostMapping("/tem/detail")
    public CommonResult<Map> getBusTemDetail(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusTemDetail(pageReqVO));
    }

    @PostMapping("/buspfpage")
    @Operation(summary = "获得始端箱功率因素分页")
    public CommonResult<PageResult<BusPFRes>> getBusPFPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        PageResult<BusPFRes> pageResult = indexService.getBusPFPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusPFRes.class));
    }

    @Operation(summary = "始端箱功率因素详情分页")
    @PostMapping("/pf/detail")
    public CommonResult<Map> getBusPFDetail(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusPFDetail(pageReqVO));
    }
    @PostMapping("/busharmonicpage")
    @Operation(summary = "获得始端箱谐波监测分页")
    public CommonResult<PageResult<BusHarmonicRes>> getBusHarmonicPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        PageResult<BusHarmonicRes> pageResult = indexService.getBusHarmonicPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusHarmonicRes.class));
    }

    @Operation(summary = "始端箱谐波监测实时数据图表")
    @PostMapping("/harmonic/redis")
    public CommonResult<BusHarmonicRedisRes> getHarmonicRedis(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusHarmonicRedisRes pageResult = indexService.getHarmonicRedis(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "始端箱谐波监测ES数据图表")
    @PostMapping("/harmonic/line")
    public CommonResult<BusHarmonicLineRes> getHarmonicLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes pageResult = indexService.getHarmonicLine(pageReqVO);
        return success(pageResult);
    }

    /**
     * 始端箱用能页面
     *
     * @param pageReqVO
     */
    @Operation(summary = "始端箱用能列表分页")
    @PostMapping("/eq/page")
    public CommonResult<PageResult<BusIndexDTO>> getEqPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDTO> pageResult = indexService.getEqPage(pageReqVO);
        return success(pageResult);
    }

    /**
     * 始端箱有功功率趋势
     *
     * @param id 始端箱id
     */
    @Operation(summary = "始端箱有功功率趋势")
    @GetMapping("/activePowTrend")
    public CommonResult<BusActivePowDTO> activePowTrend(@Param("id") int id) {
        BusPowVo vo = new BusPowVo();
        vo.setId(id);
        BusActivePowDTO dto = indexService.getActivePow(vo);
        return success(dto);
    }

    /**
     * 始端箱用能趋势
     *
     * @param id 始端箱id
     */
    @Operation(summary = "始端箱用能趋势")
    @GetMapping("/eleTrend")
    public CommonResult<List<BusEqTrendDTO>> eleTrend(@Param("id") int id, @Param("type") String type) {
        List<BusEqTrendDTO> dto = indexService.eqTrend(id, type);
        return success(dto);
    }

    /**
     * 始端箱用能环比
     *
     * @param id 始端箱id
     */
    @Operation(summary = "始端箱用能环比")
    @GetMapping("/eleChain")
    public CommonResult<BusEleChainDTO> eleChain(@Param("id") int id) {
        BusEleChainDTO dto = indexService.getEleChain(id);
        return success(dto);
    }

    @PostMapping("/balance")
    @Operation(summary = "获得始端箱不平衡度分页")
    public CommonResult<PageResult<BusBalanceDataRes>> getBusBalancePage(@RequestBody BusIndexPageReqVO pageReqVO) {
        PageResult<BusBalanceDataRes> pageResult = indexService.getBusBalancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusBalanceDataRes.class));
    }

    @PostMapping("/balance/detail")
    @Operation(summary = "获得始端箱不平衡度分页")
    public CommonResult<BusBalanceDeatilRes> getBusBalanceDetail(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusBalanceDeatilRes result = indexService.getBusBalanceDetail(pageReqVO.getDevKey());
        return success(result);
    }

    @PostMapping("/balance/trend")
    @Operation(summary = "获得始端箱不平衡度分页")
    public CommonResult<List<BusTrendDTO>> getBusBalanceTrend(@RequestBody BusIndexPageReqVO pageReqVO) {
        List<BusTrendDTO> result = indexService.getBusBalanceTrend(pageReqVO.getBusId());
        return success(result);
    }

    @GetMapping("/devKeyList")
    @Operation(summary = "获得始端箱devKey列表")
    public List<String> getDevKeyList() {
        return indexService.getDevKeyList();
    }

    @Operation(summary = "始端箱通过devKey获取id")
    @PostMapping("/getid")
    public CommonResult<Integer> getBusIdByDevKey(@RequestBody BusIndexPageReqVO pageReqVO) {
        Integer result = indexService.getBusIdByDevKey(pageReqVO.getDevKey());
        return success(result);
    }

    @Operation(summary = "始端箱通过devKey获取id")
    @PostMapping("/power/detail")
    public CommonResult<PowerRedisDataRes> getBusPowerRedisData(@RequestBody BusIndexPageReqVO pageReqVO) {
        PowerRedisDataRes result = indexService.getBusPowerRedisData(pageReqVO.getDevKey());
        return success(result);
    }

    @Operation(summary = "始端箱通过devKey获取id")
    @PostMapping("/power/loadrate")
    public CommonResult<BusLineResBase> getBusLoadRateLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusLineResBase result = indexService.getBusLoadRateLine(pageReqVO);
        return success(result);
    }

    @Operation(summary = "始端箱通过devKey获取id")
    @PostMapping("/power/powactive")
    public CommonResult<BusLineResBase> getBusPowActiveLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusLineResBase result = indexService.getBusPowActiveLine(pageReqVO);
        return success(result);
    }

    @Operation(summary = "始端箱通过devKey获取id")
    @PostMapping("/power/powreactive")
    public CommonResult<BusLineResBase> getBusPowReactiveLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusLineResBase result = indexService.getBusPowReactiveLine(pageReqVO);
        return success(result);
    }

    @PostMapping("/report/ele")
    @Operation(summary = "获得母线报表数据")
    public CommonResult<Map> getReportConsumeDataByDevKey(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getReportConsumeDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pfline")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getBusPFLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusPFLine(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pow")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportPowDataByDevKey(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getReportPowDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/tem")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportTemDataByDevKey(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getReportTemDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/redisbydevkey")
    @Operation(summary = "获得PDU设备详细信息")
    public CommonResult<String> getBusRedisByDevKey(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusRedisByDevKey(pageReqVO.getDevKey()));
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