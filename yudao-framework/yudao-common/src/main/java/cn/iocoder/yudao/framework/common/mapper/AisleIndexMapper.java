package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.dto.room.AisleDataDTO;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndexVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 通道处理
 * @date 2024/5/6 9:03
 */
@Mapper
public interface AisleIndexMapper extends BaseMapper<AisleIndex> {
    List<AisleIndexVo> selectAisleIndexByCfgList(@Param("roomId") Integer roomId);

    List<AisleDataDTO> selectRoomAisleList(int id);

    //柜列删除
    Integer roomAisleDeleteById(@Param("aisleId") int id);

    @Delete("DELETE FROM aisle_index")
    void initaisleData();

    Integer findAreaById(@Param("xLength") Integer xLength, @Param("yLength") Integer yLength, @Param("roomId") Integer roomId);

    AisleIndex selectByBusKey(String busKey);
}
