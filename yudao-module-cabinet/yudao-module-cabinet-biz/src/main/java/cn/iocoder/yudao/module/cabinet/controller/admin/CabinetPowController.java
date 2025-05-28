package cn.iocoder.yudao.module.cabinet.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetActivePowDTO;
import cn.iocoder.yudao.module.cabinet.dto.CabinetMaxActivePowDTO;
import cn.iocoder.yudao.module.cabinet.dto.CabinetPowDTO;
import cn.iocoder.yudao.module.cabinet.service.CabinetPowService;
import cn.iocoder.yudao.module.cabinet.vo.CabinetPowVo;
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
 * @description: 功率数据
 * @date 2024/4/30 11:11
 */
@Tag(name = "管理后台 - 机柜功率数据")
@RestController
public class CabinetPowController {


    @Autowired
    CabinetPowService powService;

    /**
     * 机柜实时曲线
     *
     * @param id 机柜id
     */
    @Operation(summary = "机柜功率实时曲线")
    @GetMapping("/cabinet/powTrend")
    public CommonResult<List<CabinetPowDTO>> powTrend(@Param("id") int id, @Param("type") String type) {
        CabinetPowVo vo = new CabinetPowVo();
        vo.setId(id);
        vo.setType(type);
        List<CabinetPowDTO> dto = powService.getPowList(vo);
        return success(dto);
    }

    /**
     * 机柜有功功率趋势
     *
     * @param id 机柜id
     */
    @Operation(summary = "机柜有功功率趋势")
    @GetMapping("/cabinet/activePowTrend")
    public CommonResult<CabinetMaxActivePowDTO> activePowTrend(@Param("id") int id) {
        CabinetPowVo vo = new CabinetPowVo();
        vo.setId(id);
        CabinetMaxActivePowDTO dto = powService.getActivePow(vo);
        return success(dto);
    }

}
