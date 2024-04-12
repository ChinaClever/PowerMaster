package cn.iocoder.yudao.module.pdu.service.historydata;


import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataPageReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.HistoryDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.io.IOException;

/**
 * pdu历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface HistoryDataService {

    /**
     * 获得pdu历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu历史数据分页
     */
    PageResult<HistoryDataDO> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) throws IOException;

}