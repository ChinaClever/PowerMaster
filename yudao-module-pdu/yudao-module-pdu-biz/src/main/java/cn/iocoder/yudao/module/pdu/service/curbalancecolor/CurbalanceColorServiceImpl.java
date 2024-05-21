package cn.iocoder.yudao.module.pdu.service.curbalancecolor;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.pdu.controller.admin.curbalancecolor.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.CurbalanceColorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.pdu.dal.mysql.curbalancecolor.CurbalanceColorMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pdu.enums.ErrorCodeConstants.*;

/**
 * PDU不平衡度颜色 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class CurbalanceColorServiceImpl implements CurbalanceColorService {

    @Resource
    private CurbalanceColorMapper curbalanceColorMapper;

    @Override
    public Long createCurbalanceColor(CurbalanceColorSaveReqVO createReqVO) {
        // 插入
        CurbalanceColorDO curbalanceColor = BeanUtils.toBean(createReqVO, CurbalanceColorDO.class);
        curbalanceColorMapper.insert(curbalanceColor);
        // 返回
        return curbalanceColor.getId();
    }

    @Override
    public void updateCurbalanceColor(CurbalanceColorSaveReqVO updateReqVO) {
        // 校验存在
        validateCurbalanceColorExists(updateReqVO.getId());
        // 更新
        CurbalanceColorDO updateObj = BeanUtils.toBean(updateReqVO, CurbalanceColorDO.class);
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
    public CurbalanceColorDO getCurbalanceColor() {
        CurbalanceColorDO curbalanceColorDO = curbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        if(curbalanceColorDO == null){
            curbalanceColorDO = new CurbalanceColorDO();
            curbalanceColorDO.setId(1L);
            curbalanceColorDO.setRangeOne(15);
            curbalanceColorDO.setRangeTwo(15);
            curbalanceColorDO.setRangeThree(30);
            curbalanceColorDO.setRangeFour(30);
            curbalanceColorMapper.insert(curbalanceColorDO);
        }
        return curbalanceColorDO;
    }

    @Override
    public PageResult<CurbalanceColorDO> getCurbalanceColorPage(CurbalanceColorPageReqVO pageReqVO) {
        return curbalanceColorMapper.selectPage(pageReqVO);
    }

}