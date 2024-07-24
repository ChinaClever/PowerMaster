package cn.iocoder.yudao.module.bus.dal.mysql.boxindex;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.BoxIndexPageReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱索引
 * @date 2024/5/30 14:55
 */
@Mapper
public interface BoxIndexCopyMapper extends BaseMapperX<BoxIndex> {

    default PageResult<BoxIndex> selectPage(BoxIndexPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BoxIndex>()
                .eqIfPresent(BoxIndex::getDevKey, reqVO.getDevKey())
                .inIfPresent(BoxIndex::getDevKey,reqVO.getBoxDevKeyList())
                .inIfPresent(BoxIndex::getId,reqVO.getBoxIds())
                .eqIfPresent(BoxIndex::getIpAddr, reqVO.getIpAddr())
                .eqIfPresent(BoxIndex::getDevAddr, reqVO.getDevAddr())
                .eqIfPresent(BoxIndex::getBarId, reqVO.getBarId())
                .eqIfPresent(BoxIndex::getRunStatus, reqVO.getRunStatus())
                .eqIfPresent(BoxIndex::getNodeIp, reqVO.getNodeIp())
                .eqIfPresent(BoxIndex::getIsDeleted, reqVO.getIsDeleted())
                .betweenIfPresent(BoxIndex::getCreateTime, reqVO.getCreateTime())
                .ne(ObjectUtil.isNotEmpty(reqVO.getStatus()), BoxIndex::getRunStatus, 5)
                .orderByAsc(BoxIndex::getId));
    }
}
