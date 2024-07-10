package cn.iocoder.yudao.module.bus.service.buspowerloaddetail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailRespVO;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataDetailsReqVO;

import java.io.IOException;
import java.util.Map;

public interface BusPowerLoadDetailService {

    BusPowerLoadDetailRespVO getDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException;

    Map<String, Object> getLineChartDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException;

    BusPowerLoadDetailRespVO getBoxDetailData(BusPowerLoadDetailReqVO reqVO);

    Map<String, Object> getBoxLineChartDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException;
}
