package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexVo;
import cn.iocoder.yudao.framework.common.dto.room.RoomIndexDTO;
import cn.iocoder.yudao.framework.common.dto.room.RoomIndexVo;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房索引表
 * @date 2024/4/17 14:03
 */
@Mapper
public interface RoomIndexMapper extends BaseMapper<RoomIndex> {

    int updateByDeleteRoom(@Param("id") int id);

    @InterceptorIgnore(tenantLine = "true")
    Page<RoomIndexDTO> selectRoomleteList(@Param("page") Page<RoomIndexDTO> page, @Param("indexVo") RoomIndexVo pageReqVO);

    int restoreByDeleteRoom(@Param("id")int id);

    List<String> getRoomAddrList();

}
