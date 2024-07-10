package cn.iocoder.yudao.module.aisle.service;

import cn.iocoder.yudao.module.aisle.dto.AisleDataDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleDetailDTO;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.module.aisle.vo.AisleBusSaveVo;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;

import java.io.IOException;
import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列操作
 * @date 2024/6/21 10:31
 */
public interface AisleService {


    /**
     * 柜列保存
     */
    Integer  aisleSave(AisleSaveVo aisleSaveVo);


    /**
     * 柜列绑定母线保存
     */
    void   aisleBusSave(AisleBusSaveVo busSaveVo);

    /**
     * 删除绑定
     */
    void batchDeleteBox(List<Integer> boxIds);

    /**
     * 删除绑定
     */
    void deleteBus(Integer barId);

    /**
     * 删除柜列
     * @param aisleId 柜列id
     */
    void deleteAisle(Integer aisleId);


    /**
     * 获取柜列详情
     * @param aisleId 柜列id
     */
    AisleDetailDTO getAisleDetail(Integer aisleId) throws IOException;


    /**
     * 获取柜列列表
     * @param roomId 机房id
     * @return List<AisleIndex>
     */
    List<AisleIndex> getAisleList(int roomId);


    AisleDataDTO getAisleDataDetail(int aisleId);

}
