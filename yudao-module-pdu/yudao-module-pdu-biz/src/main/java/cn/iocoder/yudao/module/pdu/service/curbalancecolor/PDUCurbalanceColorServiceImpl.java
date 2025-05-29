package cn.iocoder.yudao.module.pdu.service.curbalancecolor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pdu.controller.admin.curbalancecolor.vo.CurbalanceColorPageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.curbalancecolor.vo.CurbalanceColorSaveReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.PDUCurbalanceColorDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.curbalancecolor.PDUCurbalanceColorMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pdu.enums.ErrorCodeConstants.CURBALANCE_COLOR_NOT_EXISTS;

/**
 * PDU不平衡度颜色 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class PDUCurbalanceColorServiceImpl implements PDUCurbalanceColorService {

    @Resource
    private PDUCurbalanceColorMapper PDUCurbalanceColorMapper;

    @Override
    public Long createCurbalanceColor(CurbalanceColorSaveReqVO createReqVO) {
        // 插入
        PDUCurbalanceColorDO curbalanceColor = BeanUtils.toBean(createReqVO, PDUCurbalanceColorDO.class);
        PDUCurbalanceColorMapper.insert(curbalanceColor);
        // 返回
        return curbalanceColor.getId();
    }

    @Override
    public void updateCurbalanceColor(CurbalanceColorSaveReqVO updateReqVO) {
        // 校验存在
        validateCurbalanceColorExists(updateReqVO.getId());
        // 更新
        PDUCurbalanceColorDO updateObj = BeanUtils.toBean(updateReqVO, PDUCurbalanceColorDO.class);
        PDUCurbalanceColorMapper.updateById(updateObj);
    }

    @Override
    public void deleteCurbalanceColor(Long id) {
        // 校验存在
        validateCurbalanceColorExists(id);
        // 删除
        PDUCurbalanceColorMapper.deleteById(id);
    }

    private void validateCurbalanceColorExists(Long id) {
        if (PDUCurbalanceColorMapper.selectById(id) == null) {
            throw exception(CURBALANCE_COLOR_NOT_EXISTS);
        }
    }

    @Override
    public PDUCurbalanceColorDO getCurbalanceColor() {
        PDUCurbalanceColorDO PDUCurbalanceColorDO = PDUCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        if (PDUCurbalanceColorDO == null) {
            PDUCurbalanceColorDO = new PDUCurbalanceColorDO();
            PDUCurbalanceColorDO.setId(1L);
            PDUCurbalanceColorDO.setRangeOne(15);
            PDUCurbalanceColorDO.setRangeTwo(15);
            PDUCurbalanceColorDO.setRangeThree(30);
            PDUCurbalanceColorDO.setRangeFour(30);
            PDUCurbalanceColorDO.setLowCurrent(30);
            PDUCurbalanceColorMapper.insert(PDUCurbalanceColorDO);
        }
        return PDUCurbalanceColorDO;
    }

    @Override
    public PageResult<PDUCurbalanceColorDO> getCurbalanceColorPage(CurbalanceColorPageReqVO pageReqVO) {
        return PDUCurbalanceColorMapper.selectPage(pageReqVO);
    }

}