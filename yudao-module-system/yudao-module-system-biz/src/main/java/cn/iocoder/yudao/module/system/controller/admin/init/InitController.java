package cn.iocoder.yudao.module.system.controller.admin.init;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import cn.iocoder.yudao.module.system.service.init.AdminInitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2025/4/15 11:58
 */

@Tag(name = "管理后台 - 初始化")
@RestController
@RequestMapping("/system")
@Validated
public class InitController {

    @Autowired
    private AdminInitService adminInitService;

    @PostMapping("/init")
    @Operation(summary = "系统--初始化")
    //@PreAuthorize("@ss.hasPermission('system:user:create')")
    public CommonResult<Boolean> systemInit() {
        Boolean init = adminInitService.systemInit();
        return success(init);
    }



}
