package cn.iocoder.yudao.module.bus.service.buspowerloaddetail;

import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.BoxResBase;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusResBase;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailRespVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BusPowerLoadDetailService {

    BusPowerLoadDetailRespVO getDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException;

    Map<String, Object> getLineChartDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException;

    BusPowerLoadDetailRespVO getBoxDetailData(BusPowerLoadDetailReqVO reqVO);

    Map<String, Object> getBoxLineChartDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException;

    Map<String, Object> getBoxEqData(BusPowerLoadDetailReqVO reqVO) throws IOException;

    Map<String, Object> getBusEqData(BusPowerLoadDetailReqVO reqVO) throws IOException;

    List<String> getBusDevKeyList();

    List<String> getBoxDevKeyList();

    BusResBase getBusIdAndLocationByDevKey(BusPowerLoadDetailReqVO reqVO);

    BoxResBase getBoxIdAndLocationByDevKey(BusPowerLoadDetailReqVO reqVO);
}
