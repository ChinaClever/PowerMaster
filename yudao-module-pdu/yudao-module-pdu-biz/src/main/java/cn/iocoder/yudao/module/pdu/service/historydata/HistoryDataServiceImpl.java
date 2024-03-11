package cn.iocoder.yudao.module.pdu.service.historydata;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.HistoryDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.pdu.dal.mysql.historydata.HistoryDataMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pdu.enums.ErrorCodeConstants.*;

/**
 * pdu历史数据 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HistoryDataServiceImpl implements HistoryDataService {

    @Resource
    private HistoryDataMapper historyDataMapper;

    @Override
    public Long createHistoryData(HistoryDataSaveReqVO createReqVO) {
        // 插入
        HistoryDataDO historyData = BeanUtils.toBean(createReqVO, HistoryDataDO.class);
        historyDataMapper.insert(historyData);
        // 返回
        return historyData.getId();
    }

    @Override
    public void updateHistoryData(HistoryDataSaveReqVO updateReqVO) {
        // 校验存在
        validateHistoryDataExists(updateReqVO.getId());
        // 更新
        HistoryDataDO updateObj = BeanUtils.toBean(updateReqVO, HistoryDataDO.class);
        historyDataMapper.updateById(updateObj);
    }

    @Override
    public void deleteHistoryData(Long id) {
        // 校验存在
        validateHistoryDataExists(id);
        // 删除
        historyDataMapper.deleteById(id);
    }

    private void validateHistoryDataExists(Long id) {
        if (historyDataMapper.selectById(id) == null) {
            throw exception(HISTORY_DATA_NOT_EXISTS);
        }
    }

    @Override
    public HistoryDataDO getHistoryData(Long id) {
        return historyDataMapper.selectById(id);
    }

    @Override
    public PageResult<HistoryDataDO> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) {
        return historyDataMapper.selectPage(pageReqVO);
    }

}