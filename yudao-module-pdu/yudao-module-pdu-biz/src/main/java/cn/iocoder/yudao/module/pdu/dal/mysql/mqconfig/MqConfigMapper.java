package cn.iocoder.yudao.module.pdu.dal.mysql.mqconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pdu.dal.dataobject.mqconfig.MqConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.pdu.controller.admin.mqconfig.vo.*;

/**
 * 消息队列系统配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MqConfigMapper extends BaseMapperX<MqConfigDO> {

    default PageResult<MqConfigDO> selectPage(MqConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MqConfigDO>()
                .eqIfPresent(MqConfigDO::getIpAndPorts, reqVO.getIpAndPorts())
                .likeIfPresent(MqConfigDO::getUserName, reqVO.getUserName())
                .eqIfPresent(MqConfigDO::getPassWord, reqVO.getPassWord())
                .eqIfPresent(MqConfigDO::getTopic, reqVO.getTopic())
                .eqIfPresent(MqConfigDO::getMq, reqVO.getMq())
                .betweenIfPresent(MqConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(MqConfigDO::getId));
    }

}