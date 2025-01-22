package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexVo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.vo.CabineIndexCfgVO;
import cn.iocoder.yudao.framework.common.vo.CabinetCapacityStatisticsResVO;
import cn.iocoder.yudao.framework.common.vo.CabinetPduResVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectKey;

import java.util.List;
import java.util.Map;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜索引表
 * @date 2024/4/17 14:03
 */
@Mapper
public interface CabinetIndexMapper extends BaseMapper<CabinetIndex> {

    @Insert("INSERT INTO cabinet_index" +
            "(room_id, name, aisle_id, pow_capacity, pdu_box, is_disabled, is_deleted)" +
            "VALUES(#{roomId}, #{name}, #{aisleId}, #{powCapacity}, #{pduBox}, #{isDisabled}, #{isDeleted} );")
    @SelectKey(statement="SELECT LAST_INSERT_ID()",keyProperty="id",before=false,resultType=Integer.class)
    public void addIndex(CabinetIndex index);


    CabineIndexCfgVO selectCabineIndexCfgById(int id);

    Page<CabineIndexCfgVO> selectIndexLoadPage(@Param("page") Page page, @Param("req") CabinetIndexVo req);

    List<CabineIndexCfgVO> selectIndexLoadPage(@Param("req") CabinetIndexVo req);

    Map<String, Integer> selectLoadStatusCount();

    CabinetCapacityStatisticsResVO getCapacitystatistics();

    List<CabinetPduResVO> selectListAndPdu();

}
