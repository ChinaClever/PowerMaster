package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.dto.room.RoomCabinetDTO;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCfg;
import cn.iocoder.yudao.framework.common.vo.CabinetRunStatusResVO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/4/28 14:04
 */
@Mapper
public interface CabinetCfgMapper extends BaseMapper<CabinetCfg> {

    @InterceptorIgnore(tenantLine = "true")
    Page<CabinetIndexDTO> selectCabList(@Param("page") Page<CabinetIndexDTO> page, @Param("indexVo") CabinetIndexVo indexVo);

    /**
     * 获得已删除机柜分页
     * @param page
     * @param
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    Page<CabinetIndexDTO> selectCabdeleteList(@Param("page") Page<CabinetIndexDTO> page, @Param("indexVo") CabinetIndexVo pageReqVO);

    /**
     * 查询全部的机柜配电状态
     * @return
     */
    CabinetRunStatusResVO selectRunStatus();

    /**
     * 设备恢复
     * @param id
     * @return
     */
    int updaterestorerCabinet(@Param("id") Integer id);

    void updateByCabinetCfg(@Param("vo") CabinetSaveVo vo);

    List<RoomCabinetDTO> roomCabinetList(@Param("id") int id, @Param("aisleIds") List<Integer> aisleIds);

    void updateBatchByCabinetId(@Param("cabinetList") List<CabinetVo> cabinetList);

    List<RoomCabinetDTO> roomCabinetListByIds(@Param("roomIds") List<Integer> roomIds);
}
