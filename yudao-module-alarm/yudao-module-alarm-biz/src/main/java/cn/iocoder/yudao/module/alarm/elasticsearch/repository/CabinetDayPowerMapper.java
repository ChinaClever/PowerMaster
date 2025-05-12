package cn.iocoder.yudao.module.alarm.elasticsearch.repository;

import cn.easyes.core.conditions.interfaces.BaseEsMapper;
import cn.iocoder.yudao.module.alarm.elasticsearch.mapping.CabinetDayPower;

public interface CabinetDayPowerMapper extends BaseEsMapper<CabinetDayPower> {
    Boolean existsIndex(String indexName);

}
