package cn.iocoder.yudao.module.bus.controller.admin.boxindex;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.*;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.service.boxindex.BoxIndexService;
import cn.iocoder.yudao.module.bus.vo.BalanceStatisticsVO;
import cn.iocoder.yudao.module.bus.vo.LoadRateStatus;
import cn.iocoder.yudao.module.bus.vo.ReportBasicInformationResVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 插接箱索引")
@RestController
@RequestMapping("/box/index")
@Validated
public class BoxIndexController {

    @Resource
    private BoxIndexService indexService;

    @PostMapping("/create")
    @Operation(summary = "创建插接箱索引")

    public CommonResult<Long> createIndex(@Valid @RequestBody BoxIndexSaveReqVO createReqVO) {
        return success(indexService.createIndex(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新插接箱索引")

    public CommonResult<Boolean> updateIndex(@Valid @RequestBody BoxIndexSaveReqVO updateReqVO) {
        indexService.updateIndex(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除插接箱索引")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteIndex(@RequestParam("id") Long id) {
        indexService.deleteIndex(id);
        return success(true);
    }

    @PutMapping("/restore")
    @Operation(summary = "恢复插接箱索引")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> restoreIndex(@RequestParam("id") Long id) {
        indexService.restoreIndex(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得插接箱索引")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<BoxIndexRespVO> getIndex(@RequestParam("id") Long id) {
        BoxIndex index = indexService.getIndex(id);
        return success(BeanUtils.toBean(index, BoxIndexRespVO.class));
    }

    @PostMapping("/line/page")
    @Operation(summary = "获得插接箱需量分页")
    public CommonResult<PageResult<BoxLineRes>> getBoxLineDevicePage(@RequestBody BoxIndexPageReqVO pageReqVO) {
        return success(indexService.getBoxLineDevicePage(pageReqVO));
    }
    @PostMapping("/line/max")
    @Operation(summary = "获得始端箱需量最大")
    public CommonResult<LineBoxMaxResVO> getBoxLineMax(@RequestBody BusIndexPageReqVO pageReqVO) throws IOException {
        return success(indexService.getBoxLineMax(pageReqVO));
    }
    @Operation(summary = "插接箱需量ES数据图表")
    @PostMapping("/line/cur")
    public CommonResult<BusLineResBase> getBoxLineCurLine(@RequestBody BoxIndexPageReqVO pageReqVO) {
        BusLineResBase pageResult = indexService.getBoxLineCurLine(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "插接箱需量数据图表数据")
    @PostMapping("/line/cur/page")
    public CommonResult<PageResult<BusCurLinePageResVO>> getBoxLineCurLinePage(@RequestBody BoxIndexPageReqVO pageReqVO) throws IOException {
        PageResult<BusCurLinePageResVO> pageResult = indexService.getBoxLineCurLinePage(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "插接箱需量数据图表数据导出")
    @PostMapping("/line/cur/excel")
    public void getBoxLineCurLineExcel(@RequestBody BoxIndexPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        List<BusCurLinePageResVO> list = indexService.getBoxLineCurLineExcel(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "始端箱需量数据图表数据.xlsx", "数据", BusCurLinePageResVO.class,
                list);
    }

    @PostMapping("/page")
    @Operation(summary = "获得插接箱负荷分页")
    public CommonResult<PageResult<BoxIndexRes>> getIndexPage(@RequestBody BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndexRes> pageResult = indexService.getIndexPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxIndexRes.class));
    }

    @PostMapping("/getDeletedPage")
    @Operation(summary = "获得已经删除插接箱负荷分页")
    public CommonResult<PageResult<BoxIndexRes>> getDeletedPage(@RequestBody BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndexRes> pageResult = indexService.getDeletedPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxIndexRes.class));
    }

    @PostMapping("/boxpage")
    @Operation(summary = "获得插接箱电力分页")
    public CommonResult<PageResult<BoxRedisDataRes>> getBoxPage(@RequestBody BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxRedisDataRes> pageResult = indexService.getBoxRedisPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxRedisDataRes.class));
    }

    @PostMapping("/boxtempage")
    @Operation(summary = "获得插接箱温度索引分页")
    public CommonResult<PageResult<BoxTemRes>> getBoxTemPage(@RequestBody BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxTemRes> pageResult = indexService.getBoxTemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxTemRes.class));
    }

    @Operation(summary = "插接箱温度详情")
    @PostMapping("/tem/detail")
    public CommonResult<Map> getBoxTemDetail(@RequestBody BoxIndexPageReqVO pageReqVO) {
        return success(indexService.getBoxTemDetail(pageReqVO));
    }
    @Operation(summary = "插接箱温度详情-导出")
    @PostMapping("/tem/detailExcel")
    public void getBoxTemDetailExcel(@RequestBody BoxIndexPageReqVO pageReqVO,HttpServletResponse response) throws IOException {
        Map busTemDetail = indexService.getBoxTemDetail(pageReqVO);
        List<BusTemTableRes> tableList  = (List<BusTemTableRes>) busTemDetail.get("table");
        ExcelUtils.write(response, "插接箱温度详情.xlsx", "数据", BusTemTableRes.class,tableList);
    }

    @PostMapping("/boxpfpage")
    @Operation(summary = "获得插接箱功率因素分页")
    public CommonResult<PageResult<BoxPFRes>> getBoxPFPage(@RequestBody BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxPFRes> pageResult = indexService.getBoxPFPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxPFRes.class));
    }

    @Operation(summary = "插接箱功率因数详情")
    @PostMapping("/pf/detail")
    public CommonResult<Map> getBoxPFDetail(@RequestBody BoxIndexPageReqVO pageReqVO) {
        return success(indexService.getBoxPFDetail(pageReqVO));
    }

    @Operation(summary = "插接箱功率因数详情-导出")
    @PostMapping("/pf/detailExcel")
    public void getBoxPFDetailExcel(@RequestBody BoxIndexPageReqVO pageReqVO,HttpServletResponse response) throws IOException {
        Map boxPFDetail = indexService.getBoxPFDetail(pageReqVO);
        List<BusPFTableRes> tableList = (List<BusPFTableRes>) boxPFDetail.get("table");
        ExcelUtils.write(response, "插接箱功率因数详情.xlsx", "数据", BusPFTableRes.class,tableList);
    }

    @PostMapping("/boxharmonicpage")
    @Operation(summary = "获得插接箱谐波监测分页")
    public CommonResult<PageResult<BoxHarmonicRes>> getBoxHarmonicPage(@RequestBody BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxHarmonicRes> pageResult = indexService.getBoxHarmonicPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxHarmonicRes.class));
    }

    @Operation(summary = "插接箱谐波监测实时数据图表")
    @PostMapping("/harmonic/redis")
    public CommonResult<BusHarmonicRedisRes> getHarmonicRedis(@RequestBody BoxIndexPageReqVO pageReqVO) {
        BusHarmonicRedisRes pageResult = indexService.getHarmonicRedis(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "插接箱谐波监测ES数据图表")
    @PostMapping("/harmonic/line")
    public CommonResult<BusHarmonicLineRes> getHarmonicLine(@RequestBody BoxIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes pageResult = indexService.getHarmonicLine(pageReqVO);
        return success(pageResult);
    }

    /**
     * 机柜用能页面
     *
     * @param pageReqVO
     */
    @Operation(summary = "插接箱用能列表分页")
    @PostMapping("/eq/page")
    public CommonResult<PageResult<BoxIndexDTO>> getEqPage(@RequestBody BoxIndexPageReqVO pageReqVO) throws IOException {
        PageResult<BoxIndexDTO> pageResult;
        if (ObjectUtil.isEmpty(pageReqVO.getTimeGranularity())){
            pageResult =  indexService.getEqPage(pageReqVO);
        }else {
            pageResult = indexService.getEqPage1(pageReqVO);
            if (ObjectUtil.isEmpty(pageResult)){
                pageResult =  indexService.getEqPage(pageReqVO);
            }
        }
        return success(pageResult);
    }

    @Operation(summary = "插接箱用能列表最多")
    @PostMapping("/eq/maxEq")
    public CommonResult<List<BoxIndexMaxEqResVO>> getMaxEq() {
        List<BoxIndexMaxEqResVO> pageResult = indexService.getMaxEq();
        return success(pageResult);
    }
    /**
     * 插接箱有功功率趋势
     *
     * @param id 插接箱id
     */
    @Operation(summary = "插接箱有功功率趋势")
    @GetMapping("/activePowTrend")
    public CommonResult<BusActivePowDTO> activePowTrend(@Param("id") int id) {
        BusPowVo vo = new BusPowVo();
        vo.setId(id);
        BusActivePowDTO dto = indexService.getActivePow(vo);
        return success(dto);
    }

    /**
     * 插接箱用能趋势
     *
     * @param id 插接箱id
     */
    @Operation(summary = "插接箱用能趋势")
    @GetMapping("/eleTrend")
    public CommonResult<List<BusEqTrendDTO>> eleTrend(@Param("id") int id, @Param("type") String type) {
        List<BusEqTrendDTO> dto = indexService.eqTrend(id, type);
        return success(dto);
    }

    /**
     * 插接箱用能环比
     *
     * @param id 插接箱id
     */
    @Operation(summary = "插接箱用能环比")
    @GetMapping("/eleChain")
    public CommonResult<BusEleChainDTO> eleChain(@Param("id") int id) {
        BusEleChainDTO dto = indexService.getEleChain(id);
        return success(dto);
    }

    @PostMapping("/balance")
    @Operation(summary = "获得插接箱平衡分页")
    public CommonResult<PageResult<BoxBalanceDataRes>> getBoxBalancePage(@RequestBody BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxBalanceDataRes> pageResult = indexService.getBoxBalancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, BoxBalanceDataRes.class));
    }

    @PostMapping("/balance/detail")
    @Operation(summary = "获得插接箱不平衡度详情")
    public CommonResult<BusBalanceDeatilRes> getBoxBalanceDetail(@RequestBody BoxIndexPageReqVO pageReqVO) {
        BusBalanceDeatilRes result = indexService.getBoxBalanceDetail(pageReqVO.getDevKey());
        return success(result);
    }

    @PostMapping("/balance/trend")
    @Operation(summary = "获得插接箱不平衡度详情图表")
    public CommonResult<List<BusTrendDTO>> getBoxBalanceTrend(@RequestBody BoxIndexPageReqVO pageReqVO) {
        List<BusTrendDTO> result = indexService.getBoxBalanceTrend(pageReqVO.getBoxId());
        return success(result);
    }

    @GetMapping("/devKeyList")
    @Operation(summary = "获得插接箱devKey列表")
    public List<String> getDevKeyList() {
        return indexService.getDevKeyList();
    }

    @Operation(summary = "插接箱通过devKey获取id")
    @PostMapping("/getid")
    public CommonResult<Integer> getBoxIdByDevKey(@RequestBody BoxIndexPageReqVO pageReqVO) {
        Integer result = indexService.getBoxIdByDevKey(pageReqVO.getDevKey());
        return success(result);
    }

    @Operation(summary = "插接箱通过devKey获取redis数据")
    @PostMapping("/power/detail")
    public CommonResult<BoxPowerDetailRedisResVO> getBoxPowerRedisData(@RequestBody BoxIndexPageReqVO pageReqVO) throws IOException {
        BoxPowerDetailRedisResVO result = indexService.getBoxPowerRedisData(pageReqVO.getDevKey(), pageReqVO.getTimeGranularity());
        return success(result);
    }

    @Operation(summary = "插接箱电力详情负载率图表")
    @PostMapping("/power/loadrate")
    public CommonResult<BusLineResBase> getBoxLoadRateLine(@RequestBody BoxIndexPageReqVO pageReqVO) {
        BusLineResBase result = indexService.getBoxLoadRateLine(pageReqVO);
        return success(result);
    }

    @Operation(summary = "插接箱电力详情有功功率曲线图表")
    @PostMapping("/power/powactive")
    public CommonResult<BusLineResBase> getBoxPowActiveLine(@RequestBody BoxIndexPageReqVO pageReqVO) {
        BusLineResBase result = indexService.getBoxPowActiveLine(pageReqVO);
        return success(result);
    }

    @Operation(summary = "插接箱电力详情无功功率曲线图表")
    @PostMapping("/power/powreactive")
    public CommonResult<BusLineResBase> getBoxPowReactiveLine(@RequestBody BoxIndexPageReqVO pageReqVO) {
        BusLineResBase result = indexService.getBoxPowReactiveLine(pageReqVO);
        return success(result);
    }

    @PostMapping("/report/ele")
    @Operation(summary = "获得插接箱报表电能数据")
    public CommonResult<Map> getReportConsumeDataByDevKey(@RequestBody BoxIndexPageReqVO pageReqVO) {
        return success(indexService.getReportConsumeDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }
    @PostMapping("/report/basicInformation")
    @Operation(summary = "获得插接箱报表数据-基础数据")
    public CommonResult<ReportBasicInformationResVO> getReportBasicInformationResVO(@RequestBody BoxIndexPageReqVO pageReqVO) {
        return success(indexService.getReportBasicInformationResVO(pageReqVO));
    }


    @PostMapping("/report/pfline")
    @Operation(summary = "获得插接箱报表数据")
    public CommonResult<Map> getBoxPFLine(@RequestBody BoxIndexPageReqVO pageReqVO) {
        return success(indexService.getBoxPFLine(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pow")
    @Operation(summary = "获得插接箱报表数据")
    public CommonResult<Map> getReportPowDataByDevKey(@RequestBody BoxIndexPageReqVO pageReqVO) {
        return success(indexService.getReportPowDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/tem")
    @Operation(summary = "获得插接箱报表数据")
    public CommonResult<Map> getReportTemDataByDevKey(@RequestBody BoxIndexPageReqVO pageReqVO) {
        return success(indexService.getReportTemDataByDevKey(pageReqVO.getDevKey(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/redisbydevkey")
    @Operation(summary = "获得插接箱设备详细信息")
    public CommonResult<String> getBoxRedisByDevKey(@RequestBody BoxIndexPageReqVO pageReqVO) {
        return success(indexService.getBoxRedisByDevKey(pageReqVO.getDevKey()));
    }

    @PostMapping("/avg/boxHdaLine/form")
    @Operation(summary = "获得插接箱报表平均电流电压详细信息")
    public CommonResult<Map> getAvgBoxHdaLineForm(@RequestBody BoxIndexPageReqVO pageReqVO) throws IOException {
        return success(indexService.getAvgBoxHdaLineForm(pageReqVO));
    }
    @PostMapping("/avg/boxHdaLoop/form")
    @Operation(summary = "获得插接箱报表平均电流电压详细信息")
    public CommonResult<Map> getAvgBoxHdaLoopForm(@RequestBody BoxIndexPageReqVO pageReqVO) throws IOException {
        return success(indexService.getAvgBoxHdaLoopForm(pageReqVO));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得插接箱设备运行状态统计-不带连接器")
    public CommonResult<BusIndexStatisticsResVO> getBoxIndexStatistics() {
        return success(indexService.getBoxIndexStatistics());
    }
    @GetMapping("/statisticsAll")
    @Operation(summary = "获得插接箱设备运行状态统计-所有")
    public CommonResult<BusIndexStatisticsResVO> getBoxIndexStatisticsAll() {
        return success(indexService.getBoxIndexStatisticsAll());
    }

    @GetMapping("balance/statistics")
    @Operation(summary = "获得插接箱设备不平衡度统计")
    public CommonResult<BalanceStatisticsVO> getBoxBalanceStatistics() {
        return success(indexService.getBoxBalanceStatistics());
    }
    @GetMapping("/loadRateStatus")
    @Operation(summary = "获得始端箱设备负载量状态统计")
    public CommonResult<LoadRateStatus> getBoxIndexLoadRateStatus() {
        return success(indexService.getBoxIndexLoadRateStatus());
    }

}