package cn.iocoder.yudao.module.alarm.elasticsearch.repository;

import cn.easyes.core.core.BaseEsMapper;
import cn.iocoder.yudao.module.alarm.elasticsearch.mapping.CabinetMonthPower;

public interface CabinetMonthPowerMapper extends BaseEsMapper<CabinetMonthPower> {
    Boolean existsIndex(String indexName);

}
