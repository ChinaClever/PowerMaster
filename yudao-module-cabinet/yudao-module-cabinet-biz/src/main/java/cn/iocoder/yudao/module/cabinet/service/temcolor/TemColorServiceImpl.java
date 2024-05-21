package cn.iocoder.yudao.module.cabinet.service.temcolor;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.cabinet.controller.admin.temcolor.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.temcolor.TemColorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.cabinet.dal.mysql.temcolor.TemColorMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.cabinet.enums.ErrorCodeConstants.*;

/**
 * 机柜温度颜色 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class TemColorServiceImpl implements TemColorService {

    @Resource
    private TemColorMapper temColorMapper;

    @Override
    public Long createTemColor(List<TemColorSaveReqVO> createReqVO) {
        temColorMapper.delete(new LambdaQueryWrapperX<>());
        // 插入
        List<TemColorDO> bean = BeanUtils.toBean(createReqVO, TemColorDO.class);
        boolean success = temColorMapper.insertBatch(bean);
        // 返回
        return success ? 1L : 0L;
    }

    @Override
    public void updateTemColor(TemColorSaveReqVO updateReqVO) {
        // 校验存在
        validateTemColorExists(updateReqVO.getId());
        // 更新
        TemColorDO updateObj = BeanUtils.toBean(updateReqVO, TemColorDO.class);
        temColorMapper.updateById(updateObj);
    }

    @Override
    public void deleteTemColor(Long id) {
        // 校验存在
        validateTemColorExists(id);
        // 删除
        temColorMapper.deleteById(id);
    }

    private void validateTemColorExists(Long id) {
        if (temColorMapper.selectById(id) == null) {
            throw exception(TEM_COLOR_NOT_EXISTS);
        }
    }

    @Override
    public TemColorDO getTemColor(Long id) {
        return temColorMapper.selectById(id);
    }

    @Override
    public PageResult<TemColorDO> getTemColorPage(TemColorPageReqVO pageReqVO) {
        return temColorMapper.selectPage(pageReqVO);
    }

    @Override
    public List<TemColorDO> getTemColorAll() {
        List<TemColorDO> temColorDOS = temColorMapper.selectList();
        return temColorDOS;
    }

}