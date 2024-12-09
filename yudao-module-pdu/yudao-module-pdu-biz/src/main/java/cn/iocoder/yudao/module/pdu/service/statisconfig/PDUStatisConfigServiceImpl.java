package cn.iocoder.yudao.module.pdu.service.statisconfig;

import cn.iocoder.yudao.framework.common.util.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.pdu.controller.admin.statisconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.statisconfig.StatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.pdu.dal.mysql.statisconfig.PDUStatisConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pdu.enums.ErrorCodeConstants.*;

/**
 * pdu计算服务配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class PDUStatisConfigServiceImpl implements PDUStatisConfigService {

    @Value("${pdu-cal-refresh-url}")
    public String adder;
    @Resource
    private PDUStatisConfigMapper PDUStatisConfigMapper;

    @Override
    public Integer createStatisConfig(StatisConfigSaveReqVO createReqVO) {
        try {
            // 插入
            StatisConfigDO statisConfig = BeanUtils.toBean(createReqVO, StatisConfigDO.class);
            PDUStatisConfigMapper.insert(statisConfig);
            // 返回
            return statisConfig.getId();
        }finally {
            HttpUtil.get(adder);
        }

    }

    @Override
    public void updateStatisConfig(StatisConfigSaveReqVO updateReqVO) {
        try {
            // 校验存在
            validateStatisConfigExists(updateReqVO.getId());
            // 更新
            StatisConfigDO updateObj = BeanUtils.toBean(updateReqVO, StatisConfigDO.class);
            PDUStatisConfigMapper.updateById(updateObj);
        }finally {
            HttpUtil.get(adder);
        }

    }

    @Override
    public void deleteStatisConfig(Integer id) {
        try {
            // 校验存在
            validateStatisConfigExists(id);
            // 删除
            PDUStatisConfigMapper.deleteById(id);
        }finally {
            HttpUtil.get(adder);
        }

    }

    private void validateStatisConfigExists(Integer id) {
        if (PDUStatisConfigMapper.selectById(id) == null) {
            throw exception(STATIS_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public StatisConfigDO getStatisConfig(Integer id) {
        return PDUStatisConfigMapper.selectById(id);
    }

    @Override
    public PageResult<StatisConfigDO> getStatisConfigPage(StatisConfigPageReqVO pageReqVO) {
        return PDUStatisConfigMapper.selectPage(pageReqVO);
    }

}