package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.dto.room.RoomMenuDTO;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduIndexDo;
import cn.iocoder.yudao.framework.common.vo.EquipmentStatisticsResVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * PDU设备 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface PduIndexDoMapper extends BaseMapper<PduIndexDo> {


    List<RoomMenuDTO> queryRoomMenuDTO(@Param("pduKeys") List<String> pduKeys);

    EquipmentStatisticsResVO equipmentStatisticsQuery(@Param("pduKey") List<String> pduKey);

    //初始化
    @Delete("DELETE FROM pdu_index")
    void initPduData();
}