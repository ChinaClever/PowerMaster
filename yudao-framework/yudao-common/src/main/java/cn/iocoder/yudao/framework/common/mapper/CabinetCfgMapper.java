package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetRunStatusVo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCfg;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/4/28 14:04
 */
@Mapper
public interface CabinetCfgMapper extends BaseMapper<CabinetCfg> {

    @InterceptorIgnore(tenantLine = "true")
    Page<CabinetIndexDTO> selectCabList(@Param("page") Page<CabinetIndexDTO> page, @Param("indexVo") CabinetIndexVo indexVo);

    /**
     * 查询全部的机柜配电状态
     * @return
     */
    Map<String,Integer> selectRunStatus();
}
