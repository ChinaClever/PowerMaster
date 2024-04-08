package cn.iocoder.yudao.module.statis.service.historydata;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;

import cn.iocoder.yudao.module.statis.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.statis.dal.dataobject.historydata.HistoryDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.statis.dal.mysql.historydata.HistoryDataMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.statis.enums.ErrorCodeConstants.*;

/**
 * pdu历史数据 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class HistoryDataServiceImpl implements HistoryDataService {

    @Resource
    private HistoryDataMapper historyDataMapper;

//    private final HistoryDataRepository historyDataRepository;
//
//    @Autowired
//    public HistoryDataServiceImpl(HistoryDataRepository historyDataRepository) {
//        this.historyDataRepository = historyDataRepository;
//    }

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

    @Override
    public Void insertHistoryDataDO(HistoryDataDO historyDataDO) {
        historyDataMapper.insert(historyDataDO);
        return null;
    }

//    @Override
//    public void save(EsHistoryDataDO esHistoryDataDO) {
//        historyDataRepository.save(esHistoryDataDO);
//    }
//
//    @Override
//    public EsHistoryDataDO findById(Long id) {
//        return historyDataRepository.findById(id).orElse(new EsHistoryDataDO());
//    }
//
//    @Override
//    public void deleteById(Long id) {
//        historyDataRepository.deleteById(id);
//    }
//
//    @Override
//    public long count() {
//        return historyDataRepository.count();
//    }
//
//    @Override
//    public boolean existsById(Long id) {
//        return historyDataRepository.existsById(id);
//    }

}