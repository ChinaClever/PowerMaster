package cn.iocoder.yudao.module.bus.service.boxindex;

import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.BusActivePowDTO;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.BusEleChainDTO;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.BusEqTrendDTO;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.BusTrendDTO;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
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
public interface BoxIndexService {

    /**
     * 创建始端箱索引
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createIndex(@Valid BoxIndexSaveReqVO createReqVO);

    /**
     * 更新始端箱索引
     *
     * @param updateReqVO 更新信息
     */
    void updateIndex(@Valid BoxIndexSaveReqVO updateReqVO);

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
    BoxIndex getIndex(Long id);

    /**
     * 获得始端箱索引分页
     *
     * @param pageReqVO 分页查询
     * @return 始端箱索引分页
     */
    PageResult<BoxIndexRes> getIndexPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxRedisDataRes> getBoxRedisPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxIndexDTO> getEqPage(BoxIndexPageReqVO pageReqVO) throws IOException;

    PageResult<BoxIndexDTO> getMaxEq(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxBalanceDataRes> getBoxBalancePage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxTemRes> getBoxTemPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxPFRes> getBoxPFPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxHarmonicRes> getBoxHarmonicPage(BoxIndexPageReqVO pageReqVO);

    List<String> getDevKeyList();

    PageResult<BoxLineRes> getBoxLineDevicePage(BoxIndexPageReqVO pageReqVO);

    BusLineResBase getBoxLineCurLine(BoxIndexPageReqVO pageReqVO);

    Map getBoxTemDetail(BoxIndexPageReqVO pageReqVO);

    Map getBoxPFDetail(BoxIndexPageReqVO pageReqVO);

    BusBalanceDeatilRes getBoxBalanceDetail(String devKey);

    List<BusTrendDTO> getBoxBalanceTrend(Integer boxId);

    BusActivePowDTO getActivePow(BusPowVo vo);

    List<BusEqTrendDTO> eqTrend(int id, String type);

    BusEleChainDTO getEleChain(int id);

    BusHarmonicRedisRes getHarmonicRedis(BoxIndexPageReqVO pageReqVO);

    BusHarmonicLineRes getHarmonicLine(BoxIndexPageReqVO pageReqVO);

    Integer getBoxIdByDevKey(String devKey);

    BoxPowerDetailRedisResVO getBoxPowerRedisData(String devKey,String type) throws IOException;

    BusLineResBase getBoxLoadRateLine(BoxIndexPageReqVO pageReqVO);

    BusLineResBase getBoxPowActiveLine(BoxIndexPageReqVO pageReqVO);

    BusLineResBase getBoxPowReactiveLine(BoxIndexPageReqVO pageReqVO);

    Map getReportConsumeDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getBoxPFLine(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    String getBoxRedisByDevKey(String devKey);

    void restoreIndex(Long id);

    PageResult<BoxIndexRes> getDeletedPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BusCurLinePageResVO> getBoxLineCurLinePage(BoxIndexPageReqVO pageReqVO) throws IOException;

    List<BusCurLinePageResVO> getBoxLineCurLineExcel(BoxIndexPageReqVO pageReqVO) throws IOException;

    Map getAvgBoxHdaLineForm(BoxIndexPageReqVO pageReqVO) throws IOException;

    LineBoxMaxResVO getBoxLineMax(BusIndexPageReqVO pageReqVO) throws IOException;

    BusIndexStatisticsResVO getBoxIndexStatistics();

    BalanceStatisticsVO getBoxBalanceStatistics();

    ReportBasicInformationResVO getReportBasicInformationResVO(BoxIndexPageReqVO pageReqVO);

    LoadRateStatus getBoxIndexLoadRateStatus();

    PageResult<BoxIndexDTO> getEqPage1(BoxIndexPageReqVO pageReqVO);

    Map getAvgBoxHdaLoopForm(BoxIndexPageReqVO pageReqVO) throws IOException;

    BusIndexStatisticsResVO getBoxIndexStatisticsAll();

}