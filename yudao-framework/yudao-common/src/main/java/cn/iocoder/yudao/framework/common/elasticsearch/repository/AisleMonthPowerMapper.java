package cn.iocoder.yudao.framework.common.elasticsearch.repository;

import cn.easyes.core.conditions.interfaces.BaseEsMapper;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.AisleMonthPower;

public interface AisleMonthPowerMapper extends BaseEsMapper<AisleMonthPower> {
    Boolean existsIndex(String indexName);

}
