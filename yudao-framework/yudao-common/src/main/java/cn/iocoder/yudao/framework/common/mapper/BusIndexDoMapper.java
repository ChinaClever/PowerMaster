package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.dto.room.RoomMenuDTO;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import cn.iocoder.yudao.framework.common.vo.EquipmentStatisticsResVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱索引
 * @date 2024/5/30 14:55
 */
@Mapper
public interface BusIndexDoMapper extends BaseMapper<BusIndex> {
    List<RoomMenuDTO> queryRoomMenuDTO(@Param("busKeys") List<String> busKeys);

    EquipmentStatisticsResVO equipmentStatisticsQuery(@Param("busKey") List<String> busKey);
}
