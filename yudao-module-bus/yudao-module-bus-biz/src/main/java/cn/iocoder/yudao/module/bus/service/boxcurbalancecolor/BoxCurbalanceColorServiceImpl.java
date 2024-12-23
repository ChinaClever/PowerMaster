package cn.iocoder.yudao.module.bus.service.boxcurbalancecolor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.controller.admin.boxcurbalancecolor.vo.BoxCurbalanceColorPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.boxcurbalancecolor.vo.BoxCurbalanceColorSaveReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxcurbalancecolor.BoxCurbalanceColorDO;
import cn.iocoder.yudao.module.bus.dal.mysql.boxcurbalancecolor.BoxCurbalanceColorMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.BOX_CURBALANCE_COLOR_NOT_EXISTS;

/**
 * 插接箱不平衡度颜色 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class BoxCurbalanceColorServiceImpl implements BoxCurbalanceColorService {

    @Resource
    private BoxCurbalanceColorMapper boxCurbalanceColorMapper;

    @Override
    public Long createBoxCurbalanceColor(BoxCurbalanceColorSaveReqVO createReqVO) {
        // 插入
        BoxCurbalanceColorDO boxCurbalanceColor = BeanUtils.toBean(createReqVO, BoxCurbalanceColorDO.class);
        boxCurbalanceColorMapper.insert(boxCurbalanceColor);
        // 返回
        return boxCurbalanceColor.getId();
    }

    @Override
    public void updateBoxCurbalanceColor(BoxCurbalanceColorSaveReqVO updateReqVO) {
        // 校验存在
        validateBoxCurbalanceColorExists(updateReqVO.getId());
        // 更新
        BoxCurbalanceColorDO updateObj = BeanUtils.toBean(updateReqVO, BoxCurbalanceColorDO.class);
        boxCurbalanceColorMapper.updateById(updateObj);
    }

    @Override
    public void deleteBoxCurbalanceColor(Long id) {
        // 校验存在
        validateBoxCurbalanceColorExists(id);
        // 删除
        boxCurbalanceColorMapper.deleteById(id);
    }

    private void validateBoxCurbalanceColorExists(Long id) {
        if (boxCurbalanceColorMapper.selectById(id) == null) {
            throw exception(BOX_CURBALANCE_COLOR_NOT_EXISTS);
        }
    }

    @Override
    public BoxCurbalanceColorDO getBoxCurbalanceColor() {
        BoxCurbalanceColorDO boxCurbalanceColorDO = boxCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        if (boxCurbalanceColorDO == null) {
            boxCurbalanceColorDO = new BoxCurbalanceColorDO();
            boxCurbalanceColorDO.setId(1L);
            boxCurbalanceColorDO.setRangeOne(15);
            boxCurbalanceColorDO.setRangeTwo(16);
            boxCurbalanceColorDO.setRangeThree(30);
            boxCurbalanceColorDO.setRangeFour(31);
            boxCurbalanceColorMapper.insert(boxCurbalanceColorDO);
        }
        return boxCurbalanceColorDO;
    }

    @Override
    public PageResult<BoxCurbalanceColorDO> getBoxCurbalanceColorPage(BoxCurbalanceColorPageReqVO pageReqVO) {
        return boxCurbalanceColorMapper.selectPage(pageReqVO);
    }

}