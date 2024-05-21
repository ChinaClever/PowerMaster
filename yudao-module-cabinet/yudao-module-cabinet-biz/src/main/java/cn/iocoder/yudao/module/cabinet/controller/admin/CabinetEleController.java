package cn.iocoder.yudao.module.cabinet.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetEleChainDTO;
import cn.iocoder.yudao.module.cabinet.dto.CabinetEqTrendDTO;
import cn.iocoder.yudao.module.cabinet.dto.CabinetPduCurTrendDTO;
import cn.iocoder.yudao.module.cabinet.service.CabinetEleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 电能数据
 * @date 2024/4/30 11:11
 */
@Tag(name = "管理后台 - 机柜用能")
@RestController
public class CabinetEleController {


    @Autowired
    CabinetEleService eleService;

    /**
     * 机柜用能趋势
     *
     * @param id 机柜id
     */
    @Operation(summary = "机柜用能趋势")
    @GetMapping("/cabinet/eleTrend")
    public CommonResult<List<CabinetEqTrendDTO>> eleTrend(@Param("id") int id, @Param("type") String type) {
        List<CabinetEqTrendDTO> dto = eleService.eqTrend(id, type);
        return success(dto);
    }

    /**
     * 机柜用能环比
     *
     * @param id 机柜id
     */
    @Operation(summary = "机柜用能环比")
    @GetMapping("/cabinet/eleChain")
    public CommonResult<CabinetEleChainDTO> eleChain(@Param("id") int id) {
        CabinetEleChainDTO dto = eleService.getEleChain(id);
        return success(dto);
    }


    @Operation(summary = "机柜平衡pdu电流趋势")
    @GetMapping("/cabinet/curTrend")
    public CommonResult<List<CabinetPduCurTrendDTO>> curTrend(@Param("id") int id) {
        List<CabinetPduCurTrendDTO> dto = eleService.curTrend(id);
        return success(dto);
    }

}
