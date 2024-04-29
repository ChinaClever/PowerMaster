package cn.iocoder.yudao.module.cabinet.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetIndexDTO;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import cn.iocoder.yudao.module.cabinet.vo.CabinetIndexVo;
import com.alibaba.fastjson2.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜主页面
 * @date 2024/4/28 11:12
 */
@RestController
public class CabinetController {

    @Autowired
    CabinetService cabinetService;


    /**
     *
     * @param pageReqVO
     */
    @PostMapping("/cabinet/page")
    public CommonResult<PageResult<JSONObject>> getCabinetPage(@RequestBody CabinetIndexVo pageReqVO)  {
        PageResult<JSONObject> pageResult = cabinetService.getPageCabinet(pageReqVO);
        return success(pageResult);
    }

    /**
     *
     * @param id 机柜id
     */
    @GetMapping("/cabinet/detail")
    public CommonResult<JSONObject> getCabinetDetail(@Param("id") int id)  {
        JSONObject dto = cabinetService.getCabinetDetail(id);
        return success(dto);
    }


    /**
     *
     * @param indexVo
     */
    @PostMapping("/cabinet/insert")
    public CommonResult<Integer> insertCabinet(@RequestBody CabinetIndexVo indexVo)  {
        int cabinetId = cabinetService.insertCabinet(indexVo);
        return success(cabinetId);
    }

}
