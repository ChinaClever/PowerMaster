package cn.iocoder.yudao.framework.common.elasticsearch.repository;

import cn.easyes.core.conditions.interfaces.BaseEsMapper;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.AisleDayPower;

public interface AisleDayPowerMapper extends BaseEsMapper<AisleDayPower> {
    Boolean existsIndex(String indexName);

}
