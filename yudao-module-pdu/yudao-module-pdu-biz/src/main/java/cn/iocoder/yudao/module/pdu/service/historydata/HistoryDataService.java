package cn.iocoder.yudao.module.pdu.service.historydata;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.EnvDataDetailsReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.EnvDataPageReqVo;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataPageReqVO;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndex;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * pdu历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface HistoryDataService {

//    public PageResult<Object> scrollQuery(Integer pageSize, String index, String key, Object value) throws IOException;

    List<Object> getLocationsByPduIds(List<Map<String, Object>> mapList);

    List<Object> getSensorLocationsByPduIds(List<Map<String, Object>> mapList);

    String getAddressByIpAddr(String location);

    Map<String, Object> getSensorAddressByIpAddr(String location, Integer sensorId);

    Integer getPduIdByAddr(String ipAddr, String cascadeAddr);

    List<String> getPduIdsByIps(String[] ips);

    Map getHistoryDataTypeMaxValue() throws IOException;

    Map getSensorIdMaxValue() throws IOException;

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

    /**
     * 获得pdu环境数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu环境数据分页
     */
    PageResult<Object> getEnvDataPage(EnvDataPageReqVo pageReqVO) throws IOException;

    /**
     * 获得pdu环境数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    Map<String, Object> getEnvDataDetails(EnvDataDetailsReqVO reqVO) throws IOException;

    Map<String, Object> getNavNewData(String granularity) throws IOException;

    Map<String, Object> getEnvNavNewData() throws IOException;

    List<Object> getEnExcelList(List<Object> list);

    List<Object> getNewHistoryDataDetails(List<Object> list);

    List<Object> getNewExcelList(List<Object> list1, String ob);

    void getEnvExcelList(List<Object> list);

    PduIndex findPduIndex(int pduId);

    IPage<PduIndex> findPduIndexAll(int pageNo, int pageSize, String[] ipArray);

    List<PduIndex> findPduIndexAllToList(String[] ipArray);

    PageResult<Object> getEnvDataPageByCabinet(EnvDataPageReqVo pageReqVO) throws IOException;
}