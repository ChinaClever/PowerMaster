package cn.iocoder.yudao.framework.common.elasticsearch.repository;

import cn.easyes.core.conditions.interfaces.BaseEsMapper;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.RoomMonthPower;

public interface RoomMonthPowerMapper extends BaseEsMapper<RoomMonthPower> {
    Boolean existsIndex(String indexName);

}
