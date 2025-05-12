package cn.iocoder.yudao.framework.common.elasticsearch.repository;

import cn.easyes.core.conditions.interfaces.BaseEsMapper;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.CabinetDayPower;

public interface CabinetDayPowerMapper extends BaseEsMapper<CabinetDayPower> {
    Boolean existsIndex(String indexName);

}
