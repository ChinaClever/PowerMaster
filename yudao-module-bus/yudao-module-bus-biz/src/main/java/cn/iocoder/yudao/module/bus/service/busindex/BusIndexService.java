package cn.iocoder.yudao.module.bus.service.busindex;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailRespVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.module.bus.vo.BalanceStatisticsVO;
import cn.iocoder.yudao.module.bus.vo.LoadRateStatus;
import cn.iocoder.yudao.module.bus.vo.ReportBasicInformationResVO;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 始端箱索引 Service 接口
 *
 * @author clever
 */
public interface BusIndexService {

    /**
     * 创建始端箱索引
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createIndex(@Valid BusIndexSaveReqVO createReqVO);

    /**
     * 更新始端箱索引
     *
     * @param updateReqVO 更新信息
     */
    void updateIndex(@Valid BusIndexSaveReqVO updateReqVO);

    /**
     * 删除始端箱索引
     *
     * @param id 编号
     */
    void deleteIndex(Long id);

    /**
     * 获得始端箱索引
     *
     * @param id 编号
     * @return 始端箱索引
     */
    BusIndexDO getIndex(Long id);

    /**
     * 获得始端箱索引分页
     *
     * @param pageReqVO 分页查询
     * @return 始端箱索引分页
     */
    PageResult<BusIndexRes> getIndexPage(BusIndexPageReqVO pageReqVO);

    PageResult<BusRedisDataRes> getBusRedisPage(BusIndexPageReqVO pageReqVO);

    PageResult<BusIndexDTO> getEqPage(BusIndexPageReqVO pageReqVO);

    PageResult<BusBalanceDataRes> getBusBalancePage(BusIndexPageReqVO pageReqVO);

    PageResult<BusTemRes> getBusTemPage(BusIndexPageReqVO pageReqVO);

    PageResult<BusPFRes> getBusPFPage(BusIndexPageReqVO pageReqVO);

    Map<String, Object> getBusPFLowest();

    PageResult<BusHarmonicRes> getBusHarmonicPage(BusIndexPageReqVO pageReqVO);

    List<String> getDevKeyList();

    PageResult<BusLineRes> getBusLineDevicePage(BusIndexPageReqVO pageReqVO);

    Map getBusTemDetail(BusIndexPageReqVO pageReqVO);

    Map getBusPFDetail(BusIndexPageReqVO pageReqVO);

    BusHarmonicRedisRes getHarmonicRedis(BusIndexPageReqVO pageReqVO);

    BusHarmonicLineRes getHarmonicLine(BusIndexPageReqVO pageReqVO);

    Integer getBusIdByDevKey(String devKey);

    BusActivePowDTO getActivePow(BusPowVo vo);

    List<BusEqTrendDTO> eqTrend(int id, String type);

    BusEleChainDTO getEleChain(int id) throws IOException;

    BusBalanceDeatilRes getBusBalanceDetail(String devKey);

    List<BusTrendDTO> getBusBalanceTrend(Integer busId);

    BusLineResBase getBusLineCurLine(BusIndexPageReqVO pageReqVO);

    PowerRedisDataRes getBusPowerRedisData(String devKey);

    BusLineResBase getBusLoadRateLine(BusIndexPageReqVO pageReqVO);

    BusLineResBase getBusPowActiveLine(BusIndexPageReqVO pageReqVO);

    BusLineResBase getBusPowReactiveLine(BusIndexPageReqVO pageReqVO);

    Map getReportConsumeDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getBusPFLine(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    String getBusRedisByDevKey(String devKey);

    PageResult<BusIndexRes> getDeletedPage(BusIndexPageReqVO pageReqVO);

    void restoreIndex(Long id);

    BusPowerLoadDetailRespVO getPeakDemand(BusIndexPageReqVO pageReqVO) throws IOException;

    List<BusIndexMaxEqResVO> getMaxEq();

    PageResult<BusCurLinePageResVO> getBusLineCurLinePage(BusIndexPageReqVO pageReqVO) throws IOException;

    List<BusCurLinePageResVO> getBusLineCurLineExcel(BusIndexPageReqVO pageReqVO) throws IOException;

    List<BusIndexRes> getIndexPageExcel(BusIndexPageReqVO pageReqVO);

    Map getAvgBusHdaLineForm(BusIndexPageReqVO pageReqVO) throws IOException;

    LineMaxResVO getBusLineMax(BusIndexPageReqVO pageReqVO) throws IOException;

    BusIndexStatisticsResVO getBusIndexStatistics();

    LoadRateStatus getBusIndexLoadRateStatus();


    ReportBasicInformationResVO getReportBasicInformationResVO(BusIndexPageReqVO pageReqVO);

    List<BoxReportcopyResVO> getReportBasicInformationByBusResVO(BusIndexPageReqVO pageReqVO) throws IOException;

    BalanceStatisticsVO getBusBalanceStatistics();

    PageResult<BusIndexDTO> getEqPage1(BusIndexPageReqVO pageReqVO);

    List<String> findKeys(String key);

    List<String> getReportBasicInformationByBusResVO(BusIndexPageReqVO pageReqVO);
}