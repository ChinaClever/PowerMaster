package cn.iocoder.yudao.module.cabinet.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetIndexVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜页面
 * @date 2024/4/28 14:24
 */
public interface CabinetService {

    /**
     * 机柜列表页面
     *
     * @param vo 搜索参数
     */
    PageResult<JSONObject> getPageCabinet(CabinetIndexVo vo);

    /**
     * 机柜详情页面
     *
     * @param id 机柜id
     */
    JSONObject getCabinetDetail(int id);

    /**
     * 获取机柜信息
     *
     * @param id 机柜id
     */
    CabinetDTO getCabinetDetailV2(int id);

    /**
     * 机柜新增/编辑页面
     *
     * @param vo 新增/编辑参数
     */
    CommonResult saveCabinet(CabinetVo vo) throws Exception;


    /**
     * 机柜删除
     *
     * @param id 机柜id
     */
    int delCabinet(int id) throws Exception;

    /**
     * 机柜环境新增/编辑页面
     *
     * @param vo 环境新增/编辑参数
     */
    CommonResult saveEnvCabinet(CabinetVo vo) throws Exception;



    /**
     * 机柜用电列表页面
     *
     * @param vo 搜索参数
     */
    PageResult<CabinetIndexDTO> getEqPage(CabinetIndexVo vo);

    /**
     * 机柜容量列表页面
     *
     * @param vo 搜索参数
     */
    PageResult<CabinetIndexDTO> getCapacityPage(CabinetIndexVo vo);

    /**
     * 根据负载状态统计
     * @return
     */
    Map<Integer,Integer>  loadStatusCount();

    /**
     * 获得已删除机柜分页
     * @param pageReqVO
     * @return
     */
    PageResult<CabinetIndexVo> getDeletedCabinetPage(CabinetIndexVo pageReqVO);

    /**
     * 机柜配电状态统计
     * @return
     */
    PageResult<JSONObject> getCabinetRunStatus();
}
