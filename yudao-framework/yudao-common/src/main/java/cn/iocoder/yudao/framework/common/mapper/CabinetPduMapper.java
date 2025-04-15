package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜pdu绑定关系
 * @date 2024/4/29 15:16
 */
@Mapper
public interface CabinetPduMapper extends BaseMapper<CabinetPdu> {

    @Delete("DELETE FROM cabinet_pdu")
    void initCabinePduData();
}
