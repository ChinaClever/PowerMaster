package cn.iocoder.yudao.module.bus.service.buscurbalancecolor;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.bus.controller.admin.buscurbalancecolor.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.buscurbalancecolor.BusCurbalanceColorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.bus.dal.mysql.buscurbalancecolor.BusCurbalanceColorMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.*;

/**
 * 母线不平衡度颜色 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class BusCurbalanceColorServiceImpl implements BusCurbalanceColorService {

    @Resource
    private BusCurbalanceColorMapper curbalanceColorMapper;

    @Override
    public Long createCurbalanceColor(BusCurbalanceColorSaveReqVO createReqVO) {
        // 插入
        BusCurbalanceColorDO curbalanceColor = BeanUtils.toBean(createReqVO, BusCurbalanceColorDO.class);
        curbalanceColorMapper.insert(curbalanceColor);
        // 返回
        return curbalanceColor.getId();
    }

    @Override
    public void updateCurbalanceColor(BusCurbalanceColorSaveReqVO updateReqVO) {
        // 校验存在
        validateCurbalanceColorExists(updateReqVO.getId());
        // 更新
        BusCurbalanceColorDO updateObj = BeanUtils.toBean(updateReqVO, BusCurbalanceColorDO.class);
        curbalanceColorMapper.updateById(updateObj);
    }

    @Override
    public void deleteCurbalanceColor(Long id) {
        // 校验存在
        validateCurbalanceColorExists(id);
        // 删除
        curbalanceColorMapper.deleteById(id);
    }

    private void validateCurbalanceColorExists(Long id) {
        if (curbalanceColorMapper.selectById(id) == null) {
            throw exception(CURBALANCE_COLOR_NOT_EXISTS);
        }
    }

    @Override
    public BusCurbalanceColorDO getCurbalanceColor() {
        BusCurbalanceColorDO busCurbalanceColorDO = curbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(),false);
        if (busCurbalanceColorDO == null){
            busCurbalanceColorDO = new BusCurbalanceColorDO();
            busCurbalanceColorDO.setId(1L);
            busCurbalanceColorDO.setRangeOne(15);
            busCurbalanceColorDO.setRangeTwo(16);
            busCurbalanceColorDO.setRangeThree(30);
            busCurbalanceColorDO.setRangeFour(31);
            curbalanceColorMapper.insert(busCurbalanceColorDO);
        }
        return busCurbalanceColorDO;
    }

    @Override
    public PageResult<BusCurbalanceColorDO> getCurbalanceColorPage(BusCurbalanceColorPageReqVO pageReqVO) {
        return curbalanceColorMapper.selectPage(pageReqVO);
    }

}