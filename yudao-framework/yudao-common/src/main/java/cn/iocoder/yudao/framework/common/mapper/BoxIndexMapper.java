package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.dto.room.RoomMenuDTO;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.vo.BoxName;
import cn.iocoder.yudao.framework.common.vo.DeviceStatisticsVO;
import cn.iocoder.yudao.framework.common.vo.EquipmentStatisticsResVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱索引
 * @date 2024/5/30 14:55
 */
@Mapper
public interface BoxIndexMapper extends BaseMapper<BoxIndex> {
    List<RoomMenuDTO> queryRoomMenuDTO(@Param("boxKeys") List<String> boxKeys);

    List<String> findKeys(@Param("key") String key, @Param("flag") Integer flag);

    BoxName selectNameById(Integer boxId);

    EquipmentStatisticsResVO equipmentStatisticsQuery(@Param("boxKey") List<String> boxKey);

    @Delete("DELETE FROM box_index")
    void initBoxData();

    DeviceStatisticsVO deviceStatistics(@Param("boxKeys") List<String> boxKeys);
}
