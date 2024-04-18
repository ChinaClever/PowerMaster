package cn.iocoder.yudao.module.pdu.service.historydata;


import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataPageReqVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.io.IOException;
import java.util.List;

/**
 * pdu历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface HistoryDataService {

//    public PageResult<Object> scrollQuery(Integer pageSize, String index, String key, Object value) throws IOException;

    /**
     * 获得pdu历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu历史数据分页
     */
    PageResult<Object> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu历史数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    PageResult<Object> getHistoryDataDetails(HistoryDataDetailsReqVO reqVO) throws IOException;

}