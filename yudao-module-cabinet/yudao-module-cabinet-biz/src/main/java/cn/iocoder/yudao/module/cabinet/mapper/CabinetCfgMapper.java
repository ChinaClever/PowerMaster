package cn.iocoder.yudao.module.cabinet.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCfg;
import cn.iocoder.yudao.module.cabinet.dto.CabinetIndexDTO;
import cn.iocoder.yudao.module.cabinet.vo.CabinetIndexVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/4/28 14:04
 */
@Mapper
public interface CabinetCfgMapper extends BaseMapper<CabinetCfg> {

    Page<CabinetIndexDTO> selectList(Page<CabinetIndexDTO> page, @Param("indexVo")CabinetIndexVo indexVo);
}
