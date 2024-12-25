package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetBox;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜母线配置
 * @date 2024/5/30 9:21
 */
@Mapper
public interface CabinetBusMapper extends BaseMapper<CabinetBox> {
    List<Map<String, String>> findRoomIdA(@Param("busKey") List<String> busKey);

    List<Map<String, String>> findRoomIdB(@Param("busKeys") List<String> busKeys);
}
