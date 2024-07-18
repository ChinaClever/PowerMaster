package cn.iocoder.yudao.module.room.controller.admin.roomindex;

import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.*;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.room.controller.admin.roomindex.vo.*;
import cn.iocoder.yudao.module.room.dal.dataobject.roomindex.RoomIndexDO;
import cn.iocoder.yudao.module.room.service.roomindex.RoomIndexService;

@Tag(name = "管理后台 - 机房索引")
@RestController
@RequestMapping("/room/index")
@Validated
public class RoomIndexController {

    @Resource
    private RoomIndexService indexService;

    @PostMapping("/create")
    @Operation(summary = "创建机房索引")
    @PreAuthorize("@ss.hasPermission('room:index:create')")
    public CommonResult<Integer> createIndex(@Valid @RequestBody RoomIndexSaveReqVO createReqVO) {
        return success(indexService.createIndex(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新机房索引")
    @PreAuthorize("@ss.hasPermission('room:index:update')")
    public CommonResult<Boolean> updateIndex(@Valid @RequestBody RoomIndexSaveReqVO updateReqVO) {
        indexService.updateIndex(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除机房索引")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('room:index:delete')")
    public CommonResult<Boolean> deleteIndex(@RequestParam("id") Integer id) {
        indexService.deleteIndex(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得机房索引")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('room:index:query')")
    public CommonResult<RoomIndexRespVO> getIndex(@RequestParam("id") Integer id) {
        RoomIndexDO index = indexService.getIndex(id);
        return success(BeanUtils.toBean(index, RoomIndexRespVO.class));
    }

    @PostMapping("/page")
    @Operation(summary = "获得机房索引分页")
    @PreAuthorize("@ss.hasPermission('room:index:query')")
    public CommonResult<PageResult<RoomIndexRespVO>> getIndexPage(@RequestBody RoomIndexPageReqVO pageReqVO) {
        PageResult<RoomIndexDO> pageResult = indexService.getIndexPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RoomIndexRespVO.class));
    }

    @PostMapping("/balancepage")
    @Operation(summary = "获得机房平衡分页")
    public CommonResult<PageResult<RoomBalanceRes>> getRoomBalancePage(@RequestBody RoomIndexPageReqVO pageReqVO) {
        PageResult<RoomBalanceRes> pageResult = indexService.getRoomBalancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RoomBalanceRes.class));
    }

    /**
     * 通道列用能页面
     *
     * @param pageReqVO
     */
    @Operation(summary = "通道列用能列表分页")
    @PostMapping("/eq/page")
    public CommonResult<PageResult<RoomEQRes>> getEqPage(@RequestBody RoomIndexPageReqVO pageReqVO) {
        PageResult<RoomEQRes> pageResult = indexService.getEqPage(pageReqVO);
        return success(pageResult);
    }

    /**
     * 始端箱有功功率趋势
     *
     * @param id 始端箱id
     */
    @Operation(summary = "始端箱有功功率趋势")
    @GetMapping("/activePowTrend")
    public CommonResult<RoomActivePowDTO> activePowTrend(@Param("id") int id) {
        RoomPowVo vo = new RoomPowVo();
        vo.setId(id);
        RoomActivePowDTO dto = indexService.getActivePow(vo);
        return success(dto);
    }

    /**
     * 始端箱用能趋势
     *
     * @param id 始端箱id
     */
    @Operation(summary = "始端箱用能趋势")
    @GetMapping("/eleTrend")
    public CommonResult<List<RoomEqTrendDTO>> eleTrend(@Param("id") int id, @Param("type") String type) {
        List<RoomEqTrendDTO> dto = indexService.eqTrend(id, type);
        return success(dto);
    }

    /**
     * 始端箱用能环比
     *
     * @param id 始端箱id
     */
    @Operation(summary = "始端箱用能环比")
    @GetMapping("/eleChain")
    public CommonResult<RoomEleChainDTO> eleChain(@Param("id") int id) {
        RoomEleChainDTO dto = indexService.getEleChain(id);
        return success(dto);
    }

//    @GetMapping("/export-excel")
//    @Operation(summary = "导出机房索引 Excel")
//    @PreAuthorize("@ss.hasPermission('room:index:export')")
//    @OperateLog(type = EXPORT)
//    public void exportIndexExcel(@Valid RoomIndexPageReqVO pageReqVO,
//              HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<RoomIndexDO> list = indexService.getIndexPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "机房索引.xls", "数据", RoomIndexRespVO.class,
//                        BeanUtils.toBean(list, RoomIndexRespVO.class));
//    }

    @PostMapping("/report/ele")
    @Operation(summary = "获得机房报表数据")
    public CommonResult<Map> getReportConsumeDataById(@RequestBody RoomIndexPageReqVO pageReqVO) {
        return success(indexService.getReportConsumeDataById(pageReqVO.getId(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pow")
    @Operation(summary = "获得机房报表数据")
    public CommonResult<Map> getReportPowDataById(@RequestBody RoomIndexPageReqVO pageReqVO)  {
        return success(indexService.getReportPowDataById(pageReqVO.getId(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }

    @PostMapping("/report/pfline")
    @Operation(summary = "获得机房报表数据")
    public CommonResult<Map> getRoomPFLine(@RequestBody RoomIndexPageReqVO pageReqVO)  {
        return success(indexService.getRoomPFLine(pageReqVO.getId(),pageReqVO.getTimeType(),pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }
    @GetMapping("/idList")
    @Operation(summary = "获得机房id列表")
    public List<Integer> idList() {
        return indexService.idList();
    }


}