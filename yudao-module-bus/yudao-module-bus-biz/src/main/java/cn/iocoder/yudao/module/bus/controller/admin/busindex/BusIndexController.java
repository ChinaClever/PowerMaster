package cn.iocoder.yudao.module.bus.controller.admin.busindex;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.BoxIndexMaxEqResVO;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.*;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailRespVO;
import cn.iocoder.yudao.module.bus.vo.BalanceStatisticsVO;
import cn.iocoder.yudao.module.bus.vo.LoadRateStatus;
import cn.iocoder.yudao.module.bus.vo.ReportBasicInformationResVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.servlet.http.HttpServletResponse;
import javax.validation.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.module.bus.service.busindex.BusIndexService;

import java.io.IOException;
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

    @PutMapping("/restore")
    @Operation(summary = "恢复始端箱索引")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> restoreIndex(@RequestParam("id") Long id) {
        indexService.restoreIndex(id);
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
    @Operation(summary = "获得始端箱负荷分页")
    public CommonResult<PageResult<BusIndexRes>> getIndexPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getIndexPage(pageReqVO));
    }

    @PostMapping("/pageExcel")
    @Operation(summary = "获得始端箱负荷分页")
    public void getIndexPageExcel(@RequestBody BusIndexPageReqVO pageReqVO,
                                                                   HttpServletResponse response) throws IOException {
        List<BusIndexRes> list = indexService.getIndexPageExcel(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "始端箱能耗趋势数据.xlsx", "数据", BusIndexRes.class,
                list);
    }

    @PostMapping("/getDeletedPage")
    @Operation(summary = "获得已经删除始端箱负荷分页")
    public CommonResult<PageResult<BusIndexRes>> getDeletedPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexRes> pageResult = indexService.getDeletedPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusIndexRes.class));
    }

    @PostMapping("/line/page")
    @Operation(summary = "获得始端箱需量分页")
    public CommonResult<PageResult<BusLineRes>> getBusLineMaxPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusLineDevicePage(pageReqVO));
    }

    @PostMapping("/line/max")
    @Operation(summary = "获得始端箱需量最大")
    public CommonResult<LineMaxResVO> getBusLineMax(@RequestBody BusIndexPageReqVO pageReqVO) throws IOException {
        return success(indexService.getBusLineMax(pageReqVO));
    }

    @Operation(summary = "始端箱需量ES数据图表")
    @PostMapping("/line/cur")
    public CommonResult<BusLineResBase> getBusLineCurLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusLineResBase pageResult = indexService.getBusLineCurLine(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "始端箱需量数据图表数据")
    @PostMapping("/line/cur/page")
    public CommonResult<PageResult<BusCurLinePageResVO>> getBusLineCurLinePage(@RequestBody BusIndexPageReqVO pageReqVO) throws IOException {
        PageResult<BusCurLinePageResVO> pageResult = indexService.getBusLineCurLinePage(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "始端箱需量数据图表数据导出")
    @PostMapping("/line/cur/excel")
    public void getBusLineCurLineExcel(@RequestBody BusIndexPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        List<BusCurLinePageResVO> list = indexService.getBusLineCurLineExcel(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "始端箱需量数据图表数据.xlsx", "数据", BusCurLinePageResVO.class,
                list);
    }

    @PostMapping("/powerpage")
    @Operation(summary = "获得始端箱电力分页")
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

    @Operation(summary = "始端箱温度详情")
    @PostMapping("/tem/detail")
    public CommonResult<Map> getBusTemDetail(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusTemDetail(pageReqVO));
    }

    @Operation(summary = "始端箱温度详情-导出")
    @PostMapping("/tem/detailExcel")
    public void getBusTemDetailExcel(@RequestBody BusIndexPageReqVO pageReqVO,HttpServletResponse response) throws IOException {
        Map busTemDetail = indexService.getBusTemDetail(pageReqVO);
        List<BusTemTableRes> tableList  = (List<BusTemTableRes>) busTemDetail.get("table");
        ExcelUtils.write(response, "始端箱温度详情.xlsx", "数据", BusTemTableRes.class,tableList);
    }

    @PostMapping("/buspfpage")
    @Operation(summary = "获得始端箱功率因素分页")
    public CommonResult<PageResult<BusPFRes>> getBusPFPage(@RequestBody BusIndexPageReqVO pageReqVO) {
        PageResult<BusPFRes> pageResult = indexService.getBusPFPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BusPFRes.class));
    }

    @GetMapping("/pf/lowest")
    @Operation(summary = "获得始端箱最低功率因素设备信息")
    public CommonResult<Map> getBusPFLowest() {
        Map<String, Object> map = indexService.getBusPFLowest();
        return success(map);
    }

    @Operation(summary = "始端箱功率因素详情")
    @PostMapping("/pf/detail")
    public CommonResult<Map> getBusPFDetail(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusPFDetail(pageReqVO));
    }

    @Operation(summary = "始端箱功率因素详情-导出")
    @PostMapping("/pf/detailExcel")
    public void getBusPFDetailExcel(@RequestBody BusIndexPageReqVO pageReqVO,HttpServletResponse response) throws IOException {
        Map busPFDetail = indexService.getBusPFDetail(pageReqVO);
        List<BusPFTableRes> tableList = (List<BusPFTableRes>) busPFDetail.get("table");
        ExcelUtils.write(response, "始端箱功率因素详情.xlsx", "数据", BusPFTableRes.class,tableList);
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
        PageResult<BusIndexDTO> pageResult;
        if (ObjectUtil.isEmpty(pageReqVO.getTimeGranularity()) || !CollectionUtils.isEmpty(pageReqVO.getBusDevKeyList()) || ObjectUtil.isNotEmpty(pageReqVO.getDevKey())){
            pageResult =  indexService.getEqPage(pageReqVO);
        }else {
            pageResult = indexService.getEqPage1(pageReqVO);
            if (ObjectUtil.isEmpty(pageResult)){
                pageResult =  indexService.getEqPage(pageReqVO);
            }
        }
        return success(pageResult);
    }

    @Operation(summary = "始端箱用能列表分页")
    @PostMapping("/eq/maxEq")
    public CommonResult<List<BusIndexMaxEqResVO>> getMaxEq() {
        List<BusIndexMaxEqResVO> pageResult = indexService.getMaxEq();
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
    public CommonResult<BusEleChainDTO> eleChain(@Param("id") int id) throws IOException {
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
    @Operation(summary = "获得始端箱不平衡度详情")
    public CommonResult<BusBalanceDeatilRes> getBusBalanceDetail(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusBalanceDeatilRes result = indexService.getBusBalanceDetail(pageReqVO.getDevKey());
        return success(result);
    }

    @PostMapping("/balance/trend")
    @Operation(summary = "获得始端箱不平衡度详情图表")
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

    @Operation(summary = "始端箱通过devKey获取redis数据")
    @PostMapping("/power/detail")
    public CommonResult<PowerRedisDataRes> getBusPowerRedisData(@RequestBody BusIndexPageReqVO pageReqVO) {
        PowerRedisDataRes result = indexService.getBusPowerRedisData(pageReqVO.getDevKey());
        return success(result);
    }

    @PostMapping("/peakDemand")
    @Operation(summary = "获得最大需量")
    public CommonResult<BusPowerLoadDetailRespVO> getPeakDemand(@RequestBody BusIndexPageReqVO pageReqVO) throws IOException {
        BusPowerLoadDetailRespVO detailRespVO = indexService.getPeakDemand(pageReqVO);
        return success(detailRespVO);
    }

    @Operation(summary = "始端箱电力详情负载率图表")
    @PostMapping("/power/loadrate")
    public CommonResult<BusLineResBase> getBusLoadRateLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusLineResBase result = indexService.getBusLoadRateLine(pageReqVO);
        return success(result);
    }

    @Operation(summary = "始端箱电力详情有功功率曲线图表")
    @PostMapping("/power/powactive")
    public CommonResult<BusLineResBase> getBusPowActiveLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusLineResBase result = indexService.getBusPowActiveLine(pageReqVO);
        return success(result);
    }

    @Operation(summary = "始端箱电力详情无功功率曲线图表")
    @PostMapping("/power/powreactive")
    public CommonResult<BusLineResBase> getBusPowReactiveLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        BusLineResBase result = indexService.getBusPowReactiveLine(pageReqVO);
        return success(result);
    }

    @PostMapping("/report/ele")
    @Operation(summary = "获得始端箱报表数据")
    public CommonResult<Map> getReportConsumeDataByDevKey(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getReportConsumeDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pfline")
    @Operation(summary = "获得始端箱报表数据-功率因素")
    public CommonResult<Map> getBusPFLine(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusPFLine(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pow")
    @Operation(summary = "获得始端箱报表数据")
    public CommonResult<Map> getReportPowDataByDevKey(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getReportPowDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/tem")
    @Operation(summary = "获得始端箱报表数据")
    public CommonResult<Map> getReportTemDataByDevKey(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getReportTemDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/redisbydevkey")
    @Operation(summary = "获得始端箱设备详细信息")
    public CommonResult<String> getBusRedisByDevKey(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getBusRedisByDevKey(pageReqVO.getDevKey()));
    }

    @PostMapping("/avg/busHdaLine/form")
    @Operation(summary = "获得始端箱报表平均电流电压详细信息")
    public CommonResult<Map> getAvgBusHdaLineForm(@RequestBody BusIndexPageReqVO pageReqVO) throws IOException {
        return success(indexService.getAvgBusHdaLineForm(pageReqVO));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得始端箱设备状态统计")
    public CommonResult<BusIndexStatisticsResVO> getBusIndexStatistics() {
        return success(indexService.getBusIndexStatistics());
    }

    @GetMapping("/loadRateStatus")
    @Operation(summary = "获得始端箱设备负载量状态统计")
    public CommonResult<LoadRateStatus> getBusIndexLoadRateStatus() {
        return success(indexService.getBusIndexLoadRateStatus());
    }

    @PostMapping("/report/basicInformation")
    @Operation(summary = "获得始端箱报表数据-基础数据")
    public CommonResult<ReportBasicInformationResVO> getReportBasicInformationResVO(@RequestBody BusIndexPageReqVO pageReqVO) {
        return success(indexService.getReportBasicInformationResVO(pageReqVO));
    }

    @PostMapping("/report/basicInformationbybus")
    @Operation(summary = "获得插接箱报表数据-基础数据")
    public CommonResult<List<BoxReportcopyResVO>> getReportBasicInformationByBusResVO(@RequestBody BusIndexPageReqVO pageReqVO) throws IOException {
        return success(indexService.getReportBasicInformationByBusResVO(pageReqVO));
    }

    @GetMapping("balance/statistics")
    @Operation(summary = "获得始端箱设备不平衡度统计")
    public CommonResult<BalanceStatisticsVO> getBusBalanceStatistics() {
        return success(indexService.getBusBalanceStatistics());
    }


    @GetMapping("/findKeys")
    @Operation(summary = "模糊查询")
    public CommonResult<List<String>> findKeys(@RequestParam(value = "key") String key) {
        return success(indexService.findKeys(key));
    }
}