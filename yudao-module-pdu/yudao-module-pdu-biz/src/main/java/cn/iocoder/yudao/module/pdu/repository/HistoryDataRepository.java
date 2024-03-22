package cn.iocoder.yudao.module.pdu.repository;

import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.EsHistoryDataDO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryDataRepository extends ElasticsearchRepository<EsHistoryDataDO, Long> {

}
