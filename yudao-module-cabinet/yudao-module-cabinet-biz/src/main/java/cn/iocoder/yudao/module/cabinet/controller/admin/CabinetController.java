package cn.iocoder.yudao.module.cabinet.controller.admin;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexDTO;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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


    /**
     * 机柜主页面
     *
     * @param pageReqVO
     */
    @Operation(summary = "机柜列表分页")
    @PostMapping("/cabinet/page")
    public CommonResult<PageResult<JSONObject>> getCabinetPage(@RequestBody CabinetIndexVo pageReqVO) {
        PageResult<JSONObject> pageResult = cabinetService.getPageCabinet(pageReqVO);
        return success(pageResult);
    }

    /**
     * 机柜详情
     *
     * @param id 机柜id
     */
    @Operation(summary = "机柜功率详情")
    @GetMapping("/cabinet/detail")
    public CommonResult<JSONObject> getCabinetDetail(@Param("id") int id) {
        JSONObject dto = cabinetService.getCabinetDetail(id);
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


    /**
     * 机柜新增/编辑页面
     *
     * @param vo
     */
    @Operation(summary = "机柜新增/编辑")
    @PostMapping("/cabinet/save")
    public CommonResult saveCabinet(@RequestBody CabinetVo vo) throws Exception {
        CommonResult message = cabinetService.saveCabinet(vo);
        return message;
    }


    /**
     * 机柜删除
     *
     * @param id 机柜id
     */
    @Operation(summary = "机柜删除")
    @GetMapping("/cabinet/delete")
    public CommonResult<Integer> deleteCabinet(@Param("id") int id) throws Exception {
        int cabinetId = cabinetService.delCabinet(id);
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
    @Operation(summary = "机柜用能列表分页")
    @PostMapping("/cabinet/eq/page")
    public CommonResult<PageResult<CabinetIndexDTO>> getEqPage(@RequestBody CabinetIndexVo pageReqVO) {
        PageResult<CabinetIndexDTO> pageResult = cabinetService.getEqPage(pageReqVO);
        return success(pageResult);
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

    /**
     * 机柜负载状态统计
     *
     */
    @Operation(summary = "机柜负载状态统计")
    @GetMapping("/cabinet/load/count")
    public CommonResult<Map<Integer,Integer>> loadStatusCount() {
        Map<Integer,Integer> result = cabinetService.loadStatusCount();
        return success(result);
    }
}
