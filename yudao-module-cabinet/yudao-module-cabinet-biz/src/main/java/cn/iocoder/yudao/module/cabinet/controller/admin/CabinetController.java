package cn.iocoder.yudao.module.cabinet.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.ThreadPoolConfig;
import cn.iocoder.yudao.framework.common.vo.CabinetCapacityStatisticsResVO;
import cn.iocoder.yudao.framework.common.vo.CabinetRunStatusResVO;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.CabinetEnvAndHumRes;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import cn.iocoder.yudao.module.cabinet.vo.*;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜主页面
 * @date 2024/4/28 11:12
 */
@Tag(name = "管理后台 - 机柜页面")
@RestController
public class CabinetController {

    @Autowired
    CabinetService cabinetService;

    @Value("${cabinet-refresh-url}")
    public String adder;

    /**
     * 机柜主页面
     *
     * @param pageReqVO
     */
    @Operation(summary = "机柜配电列表分页")
    @PostMapping("/cabinet/page")
    public CommonResult<PageResult<JSONObject>> getCabinetPage(@RequestBody CabinetIndexVo pageReqVO) {
        PageResult<JSONObject> pageResult = cabinetService.getPageCabinet(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "机柜配电详情")
    @GetMapping("/cabinet/distributionDetails")
    public CommonResult<CabinetDistributionDetailsResVO> getCabinetDistributionDetails(
            @RequestParam(value = "id", required = true) @Parameter(description = "机柜id") int id,
            @RequestParam(value = "roomId", required = true) @Parameter(description = "机房id") int roomId,
            @RequestParam(value = "type", required = true) @Parameter(description = "近一小时/近一天 hour,day") String type) throws IOException {
        return success(cabinetService.getCabinetdistributionDetails(id, roomId, type));
    }

    @Operation(summary = "机柜配电详情-负载率")
    @GetMapping("/cabinet/distributionFactor")
    public CommonResult<Map> getCabinetDistributionFactor(
            @RequestParam(value = "id", required = true) @Parameter(description = "机柜id") int id,
            @RequestParam(value = "roomId", required = true) @Parameter(description = "机房id") int roomId,
            @RequestParam(value = "type", required = true) @Parameter(description = "近一小时/近一天/今天/近三天 hour,day,today,threeDay") String type) throws IOException {
        return success(cabinetService.getCabinetDistributionFactor(id, roomId, type));
    }


    @Operation(summary = "机柜配电状态统计")
    @PostMapping("/cabinet/runStatus")
    public CommonResult<CabinetRunStatusResVO> getCabinetRunStatus() {
        return success(cabinetService.getCabinetRunStatus());
    }

    @Operation(summary = "获得已删除机柜分页")
    @PostMapping("/cabinet/deletedCabinetPage")
    public CommonResult<PageResult<JSONObject>> getDeletedCabinetPage(@RequestBody CabinetIndexVo pageReqVO) {
        PageResult<JSONObject> pageResult = cabinetService.getDeletedCabinetPage(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "恢复设备")
    @GetMapping("/cabinet/restorerCabinet")
    public CommonResult getrestorerCabinet(@Param("id") int id) {
        int restorerCabinetResult = cabinetService.getrestorerCabinet(id);
        if (restorerCabinetResult == -1) {
            return error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "设备恢复失败");
        }
        return success(restorerCabinetResult);
    }

    /**
     * 机柜详情
     *
     * @param id 机柜id
     */
    @Operation(summary = "机柜功率详情")
    @GetMapping("/cabinet/detail")
    public CommonResult<Map> getCabinetDetail(@Param("id") int id) {
        Map dto = cabinetService.getCabinetDetail(id);
        return success(dto);
    }

    /**
     * 机柜详情
     *
     * @param id 机柜id
     */
    @Operation(summary = "单个机柜数据详情")
    @GetMapping("/cabinet/detailV2")
    public CommonResult<CabinetDTO> getCabinetDetailV2(@Param("id") int id) {
        CabinetDTO dto = cabinetService.getCabinetDetailV2(id);
        return success(dto);
    }

    @PostMapping("/cabinet/loadPage")
    @Operation(summary = "获得机柜负荷分页")
    public CommonResult<PageResult<CabinetIndexLoadResVO>> getIndexLoadPage(@RequestBody CabinetIndexVo pageReqVO) {
        PageResult<CabinetIndexLoadResVO> pageResult = cabinetService.getIndexLoadPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/cabinet/eq/page")
    @Operation(summary = "获得机柜用能分页")
    public CommonResult<PageResult<CabinetEnergyStatisticsResVO>> getEnergyStatisticsPage(@RequestBody CabinetIndexVo pageReqVO) throws IOException {
        PageResult<CabinetEnergyStatisticsResVO> pageResult;
        if (ObjectUtil.isEmpty(pageReqVO.getTimeGranularity()) || !CollectionUtils.isEmpty(pageReqVO.getCabinetIds()) || ObjectUtil.isNotEmpty(pageReqVO.getCompany())) {
            pageResult = cabinetService.getEnergyStatisticsPage(pageReqVO);
        } else {
            pageResult = cabinetService.getEqPage1(pageReqVO);
            if (ObjectUtil.isEmpty(pageResult)) {
                pageResult = cabinetService.getEnergyStatisticsPage(pageReqVO);
            }
        }
        return success(pageResult);
    }

    @GetMapping("/cabinet/eq/max")
    @Operation(summary = "获得机柜用能最大")
    public CommonResult<List<CabinetEnergyMaxResVO>> getEnergyMax() {
        return success(cabinetService.getEnergyMax());
    }

    /**
     * 机柜新增/编辑页面
     *
     * @param vo
     */
    @Operation(summary = "机柜新增/编辑")
    @PostMapping("/cabinet/save")
    public CommonResult saveCabinet(@RequestBody CabinetVo vo) throws Exception {
        CommonResult message = cabinetService.saveCabinet(vo);
        ThreadPoolConfig.getTHreadPool().execute(()->{
            HttpUtil.get(adder);
        });

        return message;
    }


    /**
     * 机柜删除
     *
     * @param id 机柜id
     */
    @Operation(summary = "机柜删除")
    @GetMapping("/cabinet/delete")
    public CommonResult<Integer> deleteCabinet(@RequestParam("id") int id,
                                               @RequestParam(value = "type", required = false) @Parameter(description = "删除类型：1-解绑pdu  2-解绑bus(母线) 3-解绑机架  4-删除机柜") Integer type) throws Exception {
        int cabinetId = cabinetService.delCabinet(id, type);
        if (cabinetId == -1) {
            return error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "删除失败");
        }
        return success(cabinetId);
    }


    /**
     * 机柜环境新增/编辑页面
     *
     * @param vo
     */
    @Operation(summary = "机柜环境新增/编辑")
    @PostMapping("/cabinet/env/save")
    public CommonResult saveEnvCabinet(@RequestBody CabinetVo vo) throws Exception {
        CommonResult message = cabinetService.saveEnvCabinet(vo);
        return message;
    }

    /**
     * 机柜用能页面
     *
     * @param pageReqVO
     */
    @Operation(summary = "机柜容量列表分页")
    @PostMapping("/cabinet/capacity/page")
    public CommonResult<PageResult<CabinetIndexDTO>> getCapacityPage(@RequestBody CabinetIndexVo pageReqVO) {
        PageResult<CabinetIndexDTO> pageResult = cabinetService.getCapacityPage(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "单个机柜数据详情")
    @GetMapping("/cabinet/capacity/detail")
    public CommonResult<CabinetDTO> getCabinetCapacityDetail(@Param("id") int id) {
        return success(cabinetService.getCabinetCapacityDetail(id));
    }


    @Operation(summary = "机柜容量列表统计")
    @GetMapping("/cabinet/capacity/statistics")
    public CommonResult<CabinetCapacityStatisticsResVO> getCapacitystatistics() {
        return CommonResult.success(cabinetService.getCapacitystatistics());
    }

    @Operation(summary = "机柜环境详情")
    @PostMapping("/cabinet/env")
    public CommonResult<PageResult<CabinetIndexEnvResVO>> getCabinetEnv(@RequestBody CabinetIndexVo pageReqVO) {
        return success(cabinetService.getCabinetEnv(pageReqVO));
    }

    /**
     * 机柜负载状态统计
     */
    @Operation(summary = "机柜负载状态统计")
    @GetMapping("/cabinet/load/count")
    public CommonResult<Map<String, Integer>> loadStatusCount() {
        Map<String, Integer> result = cabinetService.loadStatusCount();
        return success(result);
    }

    @Operation(summary = "机柜平衡列表分页")
    @PostMapping("/cabinet/balance/page")
    public CommonResult<PageResult<CabinetIndexBalanceResVO>> getCabinetIndexBalancePage(@RequestBody CabinetIndexVo pageReqVO) {
        return success(cabinetService.getCabinetIndexBalancePage(pageReqVO));
    }

    @PostMapping("/cabinet/loadPage/detail")
    @Operation(summary = "查询电力负荷详情")
    public CommonResult<CabinetPowerLoadDetailRespVO> getBusDetailData(@RequestBody @Valid CabinetPowerLoadDetailReqVO reqVO) throws IOException {
        CabinetPowerLoadDetailRespVO detailRespVO = cabinetService.getDetailData(reqVO);
        return success(detailRespVO);
    }

    @PostMapping("/cabinet/loadPage/chart-detail")
    @Operation(summary = "查询电力负荷详情 折线图数据")
    public CommonResult<Map<String, List>> getBusLineChartDetailData(@RequestBody @Valid CabinetPowerLoadDetailReqVO reqVO) throws IOException {
        Map<String, List> resultMap = cabinetService.getLineChartDetailData(reqVO);
        return success(resultMap);
    }

    @PostMapping("/cabinet/env/page")
    @Operation(summary = "获得机柜环境分页")
    public CommonResult<PageResult<CabinetEnvAndHumRes>> getCabinetEnvPage(@RequestBody @Valid CabinetIndexVo pageReqVO) {
        return success(cabinetService.getCabinetEnvPage(pageReqVO));
    }

    @Operation(summary = "机柜功率因素详情")
    @PostMapping("/pf/detail")
    public CommonResult<Map> getCabinetPFDetail(@RequestBody CabinetIndexVo pageReqVO) {
        Map map = cabinetService.getCabinetPFDetail(pageReqVO);
        return success(map);
    }

    @Operation(summary = "机柜功率因素详情-导出")
    @PostMapping("/pf/detailExcel")
    public void getBusPFDetailExcel(@RequestBody CabinetIndexVo pageReqVO, HttpServletResponse response) throws IOException {
        Map map = cabinetService.getCabinetPFDetail(pageReqVO);
        List<CabinetPFDetailVO> tableList = (List<CabinetPFDetailVO>) map.get("table");
        ExcelUtils.write(response, "机柜功率因素详情.xlsx", "数据", CabinetPFDetailVO.class, tableList);
    }
}
