package cn.iocoder.yudao.module.rack.controller.admin;

import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.exception.ServerException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rack.dto.RackEqTrendDTO;
import cn.iocoder.yudao.module.rack.dto.RackIndexDTO;
import cn.iocoder.yudao.module.rack.dto.RackPowDTO;
import cn.iocoder.yudao.module.rack.service.RackIndexService;
import cn.iocoder.yudao.module.rack.vo.RackIndexVo;
import cn.iocoder.yudao.module.rack.vo.RackSaveVo;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架基础页面
 * @date 2024/5/27 13:25
 */
@Tag(name = "管理后台 - 机架页面")
@RestController
public class RackIndexController {


    @Autowired
    RackIndexService rackIndexService;

    /**
     * 机架主页面
     *
     * @param pageReqVO 分页参数
     */
    @Operation(summary = "机架列表分页")
    @PostMapping("/rack/data/page")
    public CommonResult<PageResult<JSONObject>> getRackPage(@RequestBody RackIndexVo pageReqVO) {
        PageResult<JSONObject> pageResult = rackIndexService.getRackPage(pageReqVO);
        return success(pageResult);
    }

    /**
     * 机架数据详情
     *
     * @param id 机柜id
     */
    @Operation(summary = "机架数据详情")
    @GetMapping("/rack/data/detail")
    public CommonResult<JSONObject> getRackDataDetail(@Param("id") int id) {
        JSONObject dto = rackIndexService.getRackDataDetail(id);
        return success(dto);
    }


    /**
     * 机架详情
     *
     * @param id 机柜id
     */
    @Operation(summary = "机架详情")
    @GetMapping("/rack/detail")
    public CommonResult<RackIndexDTO> getRackDetail(@Param("id") int id) {
        RackIndexDTO dto = rackIndexService.getRackDetail(id);
        return success(dto);
    }

    /**
     * 机架新增/编辑页面
     *
     * @param vo 新增参数
     */
    @Operation(summary = "机架新增/编辑")
    @PostMapping("/rack/batch/save")
    public CommonResult<List<RackIndex>> batchSave(@RequestBody RackSaveVo vo)  {
        try {
            List<RackIndex> list = rackIndexService.batchSave(vo);
            return success(list);
        }catch (ServerException e){
            return error(e.getCode(),e.getMessage());
        }
    }


    /**
     * 机架删除
     *
     * @param ids 机柜id
     */
    @Operation(summary = "机架删除")
    @GetMapping("/rack/batch/delete")
    public CommonResult<Integer> batchDel(@RequestParam("ids") List<Integer> ids) {
        rackIndexService.batchDel(ids);
        return success(0);
    }

    /**
     * 机柜实时曲线
     *
     * @param id 机柜id
     */
    @Operation(summary = "机架功率实时曲线")
    @GetMapping("/rack/pow/trend")
    public CommonResult<List<RackPowDTO>> rackPowTrend(@Param("id") int id, @Param("type") String type) {
        List<RackPowDTO> dto = rackIndexService.rackPowTrend(id,type);
        return success(dto);
    }

    /**
     * 机柜用能趋势
     *
     * @param id 机柜id
     */
    @Operation(summary = "机柜用能趋势")
    @GetMapping("/rack/ele/trend")
    public CommonResult<List<RackEqTrendDTO>> eleTrend(@Param("id") int id, @Param("type") String type) {
        List<RackEqTrendDTO> dto = rackIndexService.eqTrend(id, type);
        return success(dto);
    }


    @Operation(summary = "数据删除")
    @GetMapping("/data/del")
    public void delete(@Param("startTime") String startTime,@Param("endTime")String endTime) {
        rackIndexService.deleteData(startTime,endTime);
    }

}
