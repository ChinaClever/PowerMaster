package cn.iocoder.yudao.module.pdu.service.dcconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo.DcConfigPageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo.DcConfigSaveReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.dcconfig.DcConfigDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.dcconfig.PDUDcConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pdu.enums.ErrorCodeConstants.DC_CONFIG_NOT_EXISTS;

/**
 * pdu数据采集配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class PDUDcConfigServiceImpl implements PDUDcConfigService {

    @Resource
    private PDUDcConfigMapper PDUDcConfigMapper;

    @Override
    public Short createDcConfig(DcConfigSaveReqVO createReqVO) {
        // 插入
        DcConfigDO dcConfig = BeanUtils.toBean(createReqVO, DcConfigDO.class);
        PDUDcConfigMapper.insert(dcConfig);
        // 返回
        return dcConfig.getId();
    }

    @Override
    public void updateDcConfig(DcConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateDcConfigExists(updateReqVO.getId());
        // 更新
        DcConfigDO updateObj = BeanUtils.toBean(updateReqVO, DcConfigDO.class);
        PDUDcConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteDcConfig(Short id) {
        // 校验存在
        validateDcConfigExists(id);
        // 删除
        PDUDcConfigMapper.deleteById(id);
    }

    private void validateDcConfigExists(Short id) {
        if (PDUDcConfigMapper.selectById(id) == null) {
            throw exception(DC_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public DcConfigDO getDcConfig(Short id) {
        return PDUDcConfigMapper.selectById(id);
    }

    @Override
    public PageResult<DcConfigDO> getDcConfigPage(DcConfigPageReqVO pageReqVO) {
        return PDUDcConfigMapper.selectPage(pageReqVO);
    }

}