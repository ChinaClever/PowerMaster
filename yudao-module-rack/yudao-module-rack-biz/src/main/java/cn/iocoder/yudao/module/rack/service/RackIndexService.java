package cn.iocoder.yudao.module.rack.service;

import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.PduIndex;
import cn.iocoder.yudao.module.rack.dto.RackEqTrendDTO;
import cn.iocoder.yudao.module.rack.dto.RackIndexDTO;
import cn.iocoder.yudao.module.rack.dto.RackPowDTO;
import cn.iocoder.yudao.module.rack.vo.RackIndexVo;
import cn.iocoder.yudao.module.rack.vo.RackSaveVo;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架基础业务
 * @date 2024/5/27 13:37
 */
public interface RackIndexService {


    /**
     * 机架列表页面
     *
     * @param vo 搜索参数
     */
    PageResult<JSONObject> getRackPage(RackIndexVo vo);

    /**
     * 机架数据详情页面
     *
     * @param id 机柜id
     */
    JSONObject getRackDataDetail(int id);


    /**
     * 获取机架信息
     *
     * @param id 机柜id
     */
    RackIndexDTO getRackDetail(int id);


    /**
     * 批量新增修改
     * @param vo
     */
    List<RackIndex> batchSave(RackSaveVo vo);

    /**
     * 批量删除
     * @param ids
     */
    void batchDel(List<Integer> ids);


    /**
     * 获取详情实时曲线
     * @param id 机架id
     * @param type 类型
     * @return
     */
    List<RackPowDTO> rackPowTrend(Integer id, String type);


    /**
     * 获取机架能耗趋势
     *
     * @param id   机架id
     * @param type 数据时间类型
     * @return
     */
    List<RackEqTrendDTO> eqTrend(int id, String type);


    void deleteData(String startTime,String endTime);

    IPage<RackIndex> findRackIndexAll(int pageNo, int pageSize, String[] ipArray);

    List<RackIndex> findRackIndexToList(String[] ipArray);

    String getAddressById(String devKey);
}
