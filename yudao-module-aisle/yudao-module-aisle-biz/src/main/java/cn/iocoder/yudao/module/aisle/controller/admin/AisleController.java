package cn.iocoder.yudao.module.aisle.controller.admin;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleDetailDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.aisle.dto.AisleDataDTO;
import cn.iocoder.yudao.module.aisle.dto.AisleEqDataDTO;
import cn.iocoder.yudao.module.aisle.dto.AisleListDTO;
import cn.iocoder.yudao.module.aisle.dto.AisleMainDataDTO;
import cn.iocoder.yudao.module.aisle.service.AisleService;
import cn.iocoder.yudao.module.aisle.vo.AisleBusSaveVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列主页面
 * @date 2024/4/28 11:12
 */
@Slf4j
@Tag(name = "管理后台 - 柜列页面")
@RestController
public class AisleController {

    @Autowired
    AisleService aisleService;

    /**
     * 柜列列表
     *
     */
    @Operation(summary = "柜列列表")
    @GetMapping("/aisle/list")
    public CommonResult<List<AisleListDTO>> getAisleList() {
        List<AisleListDTO> list = aisleService.getAisleList();
        return success(list);
    }

    /**
     * 柜列详情
     *
     * @param id 柜列id
     */
    @Operation(summary = "柜列详情")
    @GetMapping("/aisle/detail")
    public CommonResult<AisleDetailDTO> getDetail(@Param("id") int id) throws IOException {
        AisleDetailDTO dto = aisleService.getAisleDetail(id);
        return success(dto);
    }


    /**
     * 柜列新增/编辑页面
     *
     * @param vo
     */
    @Operation(summary = "柜列新增/编辑")
    @PostMapping("/aisle/save")
    public CommonResult<Integer> saveAisle(@RequestBody AisleSaveVo vo) {
        return CommonResult.success( aisleService.aisleSave(vo));
    }


    /**
     * 柜列删除
     *
     * @param id 柜列id
     */
    @Operation(summary = "柜列删除")
    @GetMapping("/aisle/delete")
    public CommonResult<Integer> deleteAisle(@Param("id") int id) {
         aisleService.deleteAisle(id);
        return success(id);
    }

    /**
     * 删除绑定插接箱删除
     *
     * @param ids 始端箱id
     */
    @Operation(summary = "柜列插接箱删除")
    @GetMapping("/aisle/box/delete")
    public CommonResult<Integer> deleteAisleBox(@RequestParam("ids") List<Integer> ids) {
        aisleService.batchDeleteBox(ids);
        return success(0);
    }

    /**
     * 柜列删除
     *
     * @param id 柜列id
     */
    @Operation(summary = "柜列始端箱删除")
    @GetMapping("/aisle/bus/delete")
    public CommonResult<Integer> deleteAisleBus(@Param("id") int id) {
        aisleService.deleteBus(id);
        return success(id);
    }


    @Operation(summary = "柜列始端箱单个删除")
    @GetMapping("/aisle/box/singleDelete")
    public CommonResult<Integer> deleteAisleSingleBox(@Param("id") int id) {
        int  deleteAisleSingleBox = aisleService.deleteAisleSingleBox(id);
        return success(deleteAisleSingleBox);
    }


    /**
     * 柜列母线新增/编辑页面
     *
     * @param vo
     */
    @Operation(summary = "柜列母线新增/编辑")
    @PostMapping("/aisle/bus/save")
    public CommonResult<Integer> saveBusAisle(@RequestBody AisleBusSaveVo vo)  {
            aisleService.aisleBusSave(vo);
        return CommonResult.success(vo.getAisleId());
    }



    /**
     * 柜列数据详情
     *
     * @param id 柜列id
     */
    @Operation(summary = "柜列数据详情")
    @GetMapping("/aisle/data/detail")
    public CommonResult<AisleDataDTO> getDataDetail(@Param("id") int id) throws IOException {
        AisleDataDTO dto = aisleService.getAisleDataDetail(id);
        return success(dto);
    }

    /**
     * 柜列主页面数据
     *
     * @param id 柜列id
     */
    @Operation(summary = "柜列主页面数据")
    @GetMapping("/aisle/main/data")
    public CommonResult<AisleMainDataDTO> getMainData(@Param("id") int id) {
        AisleMainDataDTO dto = aisleService.getMainData(id);
        return success(dto);
    }

    @Operation(summary = "柜列主页面用能数据")
    @GetMapping("/aisle/main/eq")
    public CommonResult<AisleEqDataDTO> getMainEq(@Param("id") int id) {
        AisleEqDataDTO dto = aisleService.getMainEq(id);
        return success(dto);
    }
}
