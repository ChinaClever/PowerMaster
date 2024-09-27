package cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailRespVO;
import cn.iocoder.yudao.module.bus.service.buspowerloaddetail.BusPowerLoadDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 母线电力负荷详情")
@RestController
@RequestMapping("/bus/PowerLoad")
@Validated
public class BusPowerLoadDetailController {
    @Resource
    private BusPowerLoadDetailService busPowerLoadDetailService;
    @PostMapping("/detail")
    @Operation(summary = "查询电力负荷详情")
    public CommonResult<BusPowerLoadDetailRespVO> getBusDetailData(@Valid BusPowerLoadDetailReqVO reqVO) throws IOException {
        BusPowerLoadDetailRespVO detailRespVO = busPowerLoadDetailService.getDetailData(reqVO);
        return success(BeanUtils.toBean(detailRespVO, BusPowerLoadDetailRespVO.class));
    }

    @PostMapping("/chart-detail")
    @Operation(summary = "查询电力负荷详情 折线图数据")
    public CommonResult<Map<String, Object>> getBusLineChartDetailData(@Valid BusPowerLoadDetailReqVO reqVO) throws IOException {
        Map<String, Object> resultMap = busPowerLoadDetailService.getLineChartDetailData(reqVO);
        return success(resultMap);
    }

    @PostMapping("/box/detail")
    @Operation(summary = "查询电力负荷详情")
    public CommonResult<BusPowerLoadDetailRespVO> getBoxDetailData(@Valid BusPowerLoadDetailReqVO reqVO) throws IOException {
        BusPowerLoadDetailRespVO detailRespVO = busPowerLoadDetailService.getBoxDetailData(reqVO);
        return success(BeanUtils.toBean(detailRespVO, BusPowerLoadDetailRespVO.class));
    }

    @PostMapping("/box/chart-detail")
    @Operation(summary = "查询电力负荷详情 折线图数据")
    public CommonResult<Map<String, Object>> getBoxLineChartDetailData(@Valid BusPowerLoadDetailReqVO reqVO) throws IOException {
        Map<String, Object> resultMap = busPowerLoadDetailService.getBoxLineChartDetailData(reqVO);
        return success(resultMap);
    }

    @PostMapping("/box/eqData")
    @Operation(summary = "查询电量数据 折线图数据")
    public CommonResult<Map<String, Object>>  getBoxEqData(@Valid BusPowerLoadDetailReqVO reqVO) throws IOException {
        Map<String, Object> resultMap = busPowerLoadDetailService.getBoxEqData(reqVO);
        return success(resultMap);
    }

    @PostMapping("/bus/eqData")
    @Operation(summary = "查询电量数据 折线图数据")
    public CommonResult<Map<String, Object>>  getBusEqData(@Valid BusPowerLoadDetailReqVO reqVO) throws IOException {
        Map<String, Object> resultMap = busPowerLoadDetailService.getBusEqData(reqVO);
        return success(resultMap);
    }

    @PostMapping("/bus/BusId")
    @Operation(summary = "查询电量数据 折线图数据")
    public CommonResult<Long>  getBusId(@Valid BusPowerLoadDetailReqVO reqVO) throws IOException {
        Long id = busPowerLoadDetailService.getBusId(reqVO);
        return success(id);
    }

    @PostMapping("/bus/BoxId")
    @Operation(summary = "查询电量数据 折线图数据")
    public CommonResult<Long>  getBoxId(@Valid BusPowerLoadDetailReqVO reqVO) throws IOException {
        Long id = busPowerLoadDetailService.getBoxId(reqVO);
        return success(id);
    }

    @GetMapping("/bus/devKeyList")
    @Operation(summary = "获得PDU设备devKey列表")
    public List<String> getBusDevKeyList() {
        return busPowerLoadDetailService.getBusDevKeyList();
    }
    @GetMapping("/box/devKeyList")
    @Operation(summary = "获得PDU设备devKey列表")
    public List<String> getBoxDevKeyList() {
        return busPowerLoadDetailService.getBoxDevKeyList();
    }

}
