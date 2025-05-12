package cn.iocoder.yudao.framework.common.elasticsearch.repository;

import cn.easyes.core.conditions.interfaces.BaseEsMapper;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.CabinetMonthPower;

public interface CabinetMonthPowerMapper extends BaseEsMapper<CabinetMonthPower> {
    Boolean existsIndex(String indexName);

}
