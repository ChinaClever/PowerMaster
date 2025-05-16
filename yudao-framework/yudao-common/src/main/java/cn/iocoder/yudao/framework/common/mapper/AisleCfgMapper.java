package cn.iocoder.yudao.framework.common.mapper;


import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.aisle.RoomAisleSaveVo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleCfg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AisleCfgMapper extends BaseMapper<AisleCfg> {


    void updateByAisleCfg(@Param("vo") RoomAisleSaveVo vo);


    @Delete("DELETE FROM aisle_cfg")
    void initaisleCfgData();
}
