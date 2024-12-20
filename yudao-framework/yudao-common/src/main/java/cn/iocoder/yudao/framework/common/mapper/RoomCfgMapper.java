package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomSavesVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 机房配置 Mapper
 */

@Mapper
public interface RoomCfgMapper extends BaseMapper<RoomCfg> {
    RoomCfg selectRoomCfgByRoomId(@Param("roomId") Integer roomId);

    void deleteByRoomCfg(@Param("roomId") int id);

    void updateByRoomCfg(@Param("vo") RoomSavesVo vo);

    void restoreByRoomCfg(@Param("roomId")int id);
}
