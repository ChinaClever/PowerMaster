package cn.iocoder.yudao.module.cabinet.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架索引
 * @date 2024/5/13 14:57
 */
@Mapper
public interface RackIndexMapper extends BaseMapper<RackIndex> {
    int updateByCabinetId(int id);
}
