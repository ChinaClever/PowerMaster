package cn.iocoder.yudao.module.cabinet.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetIndexDTO;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import cn.iocoder.yudao.module.cabinet.vo.CabinetIndexVo;
import org.springframework.beans.factory.annotation.Autowired;
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
    public CommonResult<PageResult<CabinetIndexDTO>> getCabinetPage(@RequestBody CabinetIndexVo pageReqVO)  {
        PageResult<CabinetIndexDTO> pageResult = cabinetService.getPageCabinet(pageReqVO);
        return success(pageResult);
    }

}
