package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexVo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.vo.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

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
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = Integer.class)
    public void addIndex(CabinetIndex index);


    CabineIndexCfgVO selectCabineIndexCfgById(int id);

    Page<CabineIndexCfgVO> selectIndexLoadPage(@Param("page") Page page, @Param("req") CabinetIndexVo req);

    List<CabineIndexCfgVO> selectIndexLoadPage(@Param("req") CabinetIndexVo req);

    Map<String, Integer> selectLoadStatusCount();

    CabinetCapacityStatisticsResVO getCapacitystatistics();

    List<CabinetPduResVO> selectListAndPdu();

    List<CabinetIndexBoxResVO> selectCabinetBox();

    Page<CabinetCfg> selectCabinetEnvPage(Page page, CabinetIndexVo pageReqVO);

    List<CabineIndexCfgVO> selectCabineIndexCfgByAisleId(Integer aisleId);

    List<CabinetPduResVO> selectCabinetPduList(@Param("pduKey") List<String> pduKey);

    List<CabinetPduResVO> selectCabinetPduByPduKey(String pduKey);

    @Delete("DELETE FROM cabinet_index")
    void initCabinetData();

    DeviceStatisticsVO deviceStatistics(Integer roomId);

    List<CabinetPdu> getFindCabinetPduList(Integer roomId);

    List<CabinetPdu> getFindCabinetBoxList(Integer roomId);

    Integer findAreaById(@Param("xLength") Integer xLength, @Param("yLength") Integer yLength, @Param("roomId") Integer roomId);

    int findAddAisleVerifyx(AisleSaveVo vo);

    int findAddAisleVerifyy(AisleSaveVo vo);

    CabinetIndex selectByPduKey(String pduKey);
}
