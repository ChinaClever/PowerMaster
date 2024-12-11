package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.vo.RackIndexRoomVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架索引
 * @date 2024/5/27 14:08
 */
@Mapper
public interface RackIndexDoMapper extends BaseMapper<RackIndex> {
    IPage<RackIndexRoomVO> selectQueryPage(@Param("page") Page<RackIndex> page, @Param("ipArray") String[] ipArray);

    List<RackIndexRoomVO> selectQueryPage(@Param("ipArray") String[] ipArray);
}
