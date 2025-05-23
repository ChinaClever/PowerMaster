package cn.iocoder.yudao.framework.common.elasticsearch.repository;

import cn.easyes.core.conditions.interfaces.BaseEsMapper;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.RoomDayPower;

public interface RoomDayPowerMapper extends BaseEsMapper<RoomDayPower> {
    Boolean existsIndex(String indexName);

}
