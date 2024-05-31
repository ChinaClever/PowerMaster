package cn.iocoder.yudao.module.bus.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱索引
 * @date 2024/5/30 14:55
 */
@Mapper
public interface BoxIndexMapper extends BaseMapper<BoxIndex> {
}
