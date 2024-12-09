package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectKey;

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

}
