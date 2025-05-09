package cn.iocoder.yudao.module.aisle.service;

import cn.iocoder.yudao.module.aisle.dto.AisleDataDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleDetailDTO;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.module.aisle.dto.AisleEqDataDTO;
import cn.iocoder.yudao.module.aisle.dto.AisleListDTO;
import cn.iocoder.yudao.module.aisle.dto.AisleMainDataDTO;
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
//    void   aisleBusSave(AisleBusSaveVo busSaveVo);

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
     * @return List<AisleIndex>
     */
    List<AisleListDTO> getAisleList();


    /**
     * 柜列数据详情
     * @param aisleId
     * @return
     */
    AisleDataDTO getAisleDataDetail(int aisleId);

    /**
     * 柜列主页面数据
     * @param aisleId
     * @return
     */
    AisleMainDataDTO getMainData(int aisleId);

    /**
     * 主页面用能数据
     * @param aisleId
     * @return
     */
    AisleEqDataDTO getMainEq(int aisleId);

    /**
     * 柜列始端箱单个删除
     * @param id
     */
    int deleteAisleSingleBox(int id);
}
