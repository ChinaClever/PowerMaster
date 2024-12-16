package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndexVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
}
