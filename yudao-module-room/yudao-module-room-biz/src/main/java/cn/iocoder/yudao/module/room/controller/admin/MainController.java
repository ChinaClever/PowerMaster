package cn.iocoder.yudao.module.room.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.room.dto.main.DevDataDTO;
import cn.iocoder.yudao.module.room.dto.main.EqDataDTO;
import cn.iocoder.yudao.module.room.dto.main.PowDataDTO;
import cn.iocoder.yudao.module.room.service.MainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 主页面
 * @date 2024/4/28 11:12
 */
@Tag(name = "管理后台 - 首页")
@RestController
@RequestMapping("/main")
public class MainController {

    @Autowired
    MainService mainService;


    @Operation(summary = "主页面用能数据")
    @GetMapping("/eq/data")
    public CommonResult<EqDataDTO> getMainEq() {
        EqDataDTO dto = mainService.getMainEq();
        return success(dto);
    }

    /**
     * 主页面数据
     */
    @Operation(summary = "主页面设备数据")
    @GetMapping("/dev/data")
    public CommonResult<DevDataDTO> getMainDevData() {
        DevDataDTO dto = mainService.getMainDevData();
        return success(dto);
    }

    /**
     * 主页面数据
     */
    @Operation(summary = "主页面功率数据")
    @GetMapping("/pow/data")
    public CommonResult<PowDataDTO> getMainPowData() {
        PowDataDTO dto = mainService.getMainPowData();
        return success(dto);
    }

}
