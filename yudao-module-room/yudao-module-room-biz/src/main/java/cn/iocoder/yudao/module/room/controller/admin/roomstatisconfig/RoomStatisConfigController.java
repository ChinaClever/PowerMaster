package cn.iocoder.yudao.module.room.controller.admin.roomstatisconfig;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.room.controller.admin.roomstatisconfig.vo.*;
import cn.iocoder.yudao.module.room.dal.dataobject.roomstatisconfig.RoomStatisConfigDO;
import cn.iocoder.yudao.module.room.service.roomstatisconfig.RoomStatisConfigService;

@Tag(name = "管理后台 - 机房计算服务配置")
@RestController
@RequestMapping("/room/statis-config")
@Validated
public class RoomStatisConfigController {

    @Resource
    private RoomStatisConfigService statisConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建机房计算服务配置")
    @PreAuthorize("@ss.hasPermission('room:statis-config:create')")
    public CommonResult<Integer> createStatisConfig(@Valid @RequestBody RoomStatisConfigSaveReqVO createReqVO) {
        return success(statisConfigService.createStatisConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新机房计算服务配置")
    @PreAuthorize("@ss.hasPermission('room:statis-config:update')")
    public CommonResult<Boolean> updateStatisConfig(@Valid @RequestBody RoomStatisConfigSaveReqVO updateReqVO) {
        statisConfigService.updateStatisConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除机房计算服务配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('room:statis-config:delete')")
    public CommonResult<Boolean> deleteStatisConfig(@RequestParam("id") Integer id) {
        statisConfigService.deleteStatisConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得机房计算服务配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('room:statis-config:query')")
    public CommonResult<RoomStatisConfigRespVO> getStatisConfig(@RequestParam("id") Integer id) {
        RoomStatisConfigDO statisConfig = statisConfigService.getStatisConfig(id);
        return success(BeanUtils.toBean(statisConfig, RoomStatisConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得机房计算服务配置分页")
    @PreAuthorize("@ss.hasPermission('room:statis-config:query')")
    public CommonResult<PageResult<RoomStatisConfigRespVO>> getStatisConfigPage(@Valid RoomStatisConfigPageReqVO pageReqVO) {
        PageResult<RoomStatisConfigDO> pageResult = statisConfigService.getStatisConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RoomStatisConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机房计算服务配置 Excel")
    @PreAuthorize("@ss.hasPermission('room:statis-config:export')")
    @OperateLog(type = EXPORT)
    public void exportStatisConfigExcel(@Valid RoomStatisConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RoomStatisConfigDO> list = statisConfigService.getStatisConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机房计算服务配置.xls", "数据", RoomStatisConfigRespVO.class,
                        BeanUtils.toBean(list, RoomStatisConfigRespVO.class));
    }

}