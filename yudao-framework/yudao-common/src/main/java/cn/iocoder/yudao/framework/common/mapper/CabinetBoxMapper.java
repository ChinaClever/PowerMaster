package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetBox;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2025/4/15 15:28
 */
@Mapper
public interface CabinetBoxMapper extends BaseMapper<CabinetBox> {
    @Delete("DELETE FROM cabinet_box")
    void initCabineBoxData();

    void updateBatch(@Param("list") List<CabinetBox> list);

    void insertBatch(@Param("list") List<CabinetBox> list);
}
