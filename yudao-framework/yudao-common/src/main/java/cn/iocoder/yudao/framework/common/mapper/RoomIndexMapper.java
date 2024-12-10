package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房索引表
 * @date 2024/4/17 14:03
 */
@Mapper
public interface RoomIndexMapper extends BaseMapper<RoomIndex> {

    int updateByDeleteRoom(@Param("id") int id);
}
