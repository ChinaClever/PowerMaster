package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBox;
import cn.iocoder.yudao.framework.common.vo.AisleBoxResVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列母线绑定
 * @date 2024/6/7 10:23
 */
@Mapper
public interface AisleBoxMapper extends BaseMapper<AisleBox> {
    List<AisleBoxResVO> selectAisleByBoxKey(@Param("keys") List<String> keys);
}
