package cn.iocoder.yudao.module.bus.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.dto.BusDataDTO;
import cn.iocoder.yudao.module.bus.service.BusService;
import cn.iocoder.yudao.module.bus.vo.BusIndexVo;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 母线业务
 * @date 2024/5/30 14:34
 */
@Tag(name = "管理后台 - 母线拓扑")
@RestController
public class BusController {


    @Autowired
    BusService busService;


    /**
     * 机柜主页面
     *
     * @param vo
     */
    @Operation(summary = "母线拓扑页面")
    @PostMapping("/bus/topology")
    public CommonResult<List<BusDataDTO>> getBusData(@RequestBody BusIndexVo vo) {
        List<BusDataDTO> result = busService.getBusData(vo);
        return success(result);
    }



}
