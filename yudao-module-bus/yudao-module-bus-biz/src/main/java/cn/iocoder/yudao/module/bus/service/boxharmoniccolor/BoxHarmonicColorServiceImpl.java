package cn.iocoder.yudao.module.bus.service.boxharmoniccolor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor.vo.BoxHarmonicColorPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor.vo.BoxHarmonicColorSaveReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxharmoniccolor.BoxHarmonicColorDO;
import cn.iocoder.yudao.module.bus.dal.mysql.boxharmoniccolor.BoxHarmonicColorMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.BOX_HARMONIC_COLOR_NOT_EXISTS;

/**
 * 插接箱谐波颜色 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class BoxHarmonicColorServiceImpl implements BoxHarmonicColorService {

    @Resource
    private BoxHarmonicColorMapper boxHarmonicColorMapper;

    @Override
    public Long createBoxHarmonicColor(BoxHarmonicColorSaveReqVO createReqVO) {
        // 插入
        BoxHarmonicColorDO boxHarmonicColor = BeanUtils.toBean(createReqVO, BoxHarmonicColorDO.class);
        boxHarmonicColorMapper.insert(boxHarmonicColor);
        // 返回
        return boxHarmonicColor.getId();
    }

    @Override
    public void updateBoxHarmonicColor(BoxHarmonicColorSaveReqVO updateReqVO) {
        // 校验存在
        validateBoxHarmonicColorExists(updateReqVO.getId());
        // 更新
        BoxHarmonicColorDO updateObj = BeanUtils.toBean(updateReqVO, BoxHarmonicColorDO.class);
        boxHarmonicColorMapper.updateById(updateObj);
    }

    @Override
    public void deleteBoxHarmonicColor(Long id) {
        // 校验存在
        validateBoxHarmonicColorExists(id);
        // 删除
        boxHarmonicColorMapper.deleteById(id);
    }

    private void validateBoxHarmonicColorExists(Long id) {
        if (boxHarmonicColorMapper.selectById(id) == null) {
            throw exception(BOX_HARMONIC_COLOR_NOT_EXISTS);
        }
    }

    @Override
    public BoxHarmonicColorDO getBoxHarmonicColor() {
        BoxHarmonicColorDO boxHarmonicColorDO = boxHarmonicColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        if (boxHarmonicColorDO == null) {
            boxHarmonicColorDO = new BoxHarmonicColorDO();
            boxHarmonicColorDO.setId(1L);
            boxHarmonicColorDO.setRangeOne(15);
            boxHarmonicColorDO.setRangeTwo(16);
            boxHarmonicColorDO.setRangeThree(30);
            boxHarmonicColorDO.setRangeFour(31);
            boxHarmonicColorMapper.insert(boxHarmonicColorDO);
        }
        return boxHarmonicColorDO;
    }

    @Override
    public PageResult<BoxHarmonicColorDO> getBoxHarmonicColorPage(BoxHarmonicColorPageReqVO pageReqVO) {
        return boxHarmonicColorMapper.selectPage(pageReqVO);
    }

}