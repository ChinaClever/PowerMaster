package cn.iocoder.yudao.module.pdu.repository;

import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.PduHdaTotalRealtimeDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  HdaRepository  extends ElasticsearchRepository<PduHdaTotalRealtimeDO, String> {
}
