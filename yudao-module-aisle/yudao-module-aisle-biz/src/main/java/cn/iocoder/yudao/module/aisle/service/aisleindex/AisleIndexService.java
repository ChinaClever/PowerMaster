package cn.iocoder.yudao.module.aisle.service.aisleindex;

import javax.validation.*;
import cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo.*;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.aisle.dto.AislePowerLoadDetailReqDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通道列 Service 接口
 *
 * @author clever
 */
public interface AisleIndexService {

    /**
     * 创建通道列
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createIndex(@Valid AisleIndexSaveReqVO createReqVO);

    /**
     * 更新通道列
     *
     * @param updateReqVO 更新信息
     */
    void updateIndex(@Valid AisleIndexSaveReqVO updateReqVO);

    /**
     * 删除通道列
     *
     * @param id 编号
     */
    Boolean deleteIndex(Integer id);

    /**
     * 获得通道列
     *
     * @param id 编号
     * @return 通道列
     */
    AisleIndexDO getIndex(Integer id);

    /**
     * 获得通道列分页
     *
     * @param pageReqVO 分页查询
     * @return 通道列分页
     */
    PageResult<AisleIndexRes> getIndexPage(AisleIndexPageReqVO pageReqVO);

    List<Integer> getDevKeyList();

    PageResult<AislePowerRes> getPowerPage(AisleIndexPageReqVO pageReqVO);

    PageResult<AisleEQRes> getEqPage(AisleIndexPageReqVO pageReqVO);

    PageResult<AislePfRes> getAislePFPage(AisleIndexPageReqVO pageReqVO);

    PageResult<AisleLineMaxRes> getAisleLineMaxPage(AisleIndexPageReqVO pageReqVO);

    AisleActivePowDTO getActivePow(AislePowVo vo);

    List<AisleEqTrendDTO> eqTrend(int id, String type);

    AisleEleChainDTO getEleChain(int id);

    AisleLineResBase getAisleLineCurLine(AisleIndexPageReqVO pageReqVO);

    PageResult<AisleBalanceRes> getAisleBalancePage(AisleIndexPageReqVO pageReqVO);

    Map getReportConsumeDataById(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportPowDataById(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getAislePFLine(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getAislePFDetail(AisleIndexPageReqVO pageReqVO);

    List<Integer> idList();

    PageResult<AisleEQRes> getEqPage1(AisleIndexPageReqVO pageReqVO);

    AisleBalanceChartResVO getAisleBalanceChart(Integer id);

    Map getLineChartDetailData(AislePowerLoadDetailReqDTO reqVO);

    List<AisleMaxEqResVO> getMaxEq();

    Map findAisleFactor() throws IOException;

    Map<String, AislePowerFactorMaxResVO> getAisleLineMax(AisleIndexPageReqVO pageReqVO) throws IOException;

    PageResult<AisleIndexDelResVO> getDelPage(AisleIndexPageReqVO pageReqVO);

    Boolean restore(Integer id);

    Map getAislePFLineByType(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType);

    Map getReportPowDataByTypeAndId(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType);

    Map aisleHdaLineHisdataReportKey(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType);

    PageResult<AisleBalanceRes> getAisBasicInformation(AisleIndexPageReqVO pageReqVO);

    Map getReportConsumeEleDataById(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    List<AisleBalanceRes> getAisBasicInformationByRoom(String roomId);

    Map getEleByRoom(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);
}