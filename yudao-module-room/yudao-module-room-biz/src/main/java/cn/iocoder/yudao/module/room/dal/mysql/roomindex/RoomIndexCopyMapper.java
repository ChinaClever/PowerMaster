package cn.iocoder.yudao.module.room.dal.mysql.roomindex;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.room.dal.dataobject.roomindex.RoomIndexDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.room.controller.admin.roomindex.vo.*;

/**
 * 机房索引 Mapper
 *
 * @author clever
 */
@Mapper
public interface RoomIndexCopyMapper extends BaseMapperX<RoomIndexDO> {

    default PageResult<RoomIndexDO> selectPage(RoomIndexPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RoomIndexDO>()
                .likeIfPresent(RoomIndexDO::getName, reqVO.getName())
                .eqIfPresent(RoomIndexDO::getIsDelete, reqVO.getIsDelete())
                .eqIfPresent(RoomIndexDO::getPowerCapacity, reqVO.getPowerCapacity())
                .betweenIfPresent(RoomIndexDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(RoomIndexDO::getEleAlarmDay, reqVO.getEleAlarmDay())
                .eqIfPresent(RoomIndexDO::getEleLimitDay, reqVO.getEleLimitDay())
                .eqIfPresent(RoomIndexDO::getEleAlarmMonth, reqVO.getEleAlarmMonth())
                .eqIfPresent(RoomIndexDO::getEleLimitMonth, reqVO.getEleLimitMonth())
                .orderByDesc(RoomIndexDO::getId));
    }

}