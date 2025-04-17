package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列母线绑定
 * @date 2024/6/7 10:23
 */
@Mapper
public interface AisleBarMapper extends BaseMapper<AisleBar> {

    @Delete("DELETE FROM aisle_bar")
    void initaisleBarData();
}
