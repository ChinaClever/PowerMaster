package cn.iocoder.yudao.module.cabinet.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetIndexDTO;
import cn.iocoder.yudao.module.cabinet.vo.CabinetIndexVo;
import com.alibaba.fastjson2.JSONObject;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/4/28 14:24
 */
public interface CabinetService {

    /**
     * 机柜列表页面
     * @param vo
     * @return
     */
    PageResult<JSONObject>  getPageCabinet(CabinetIndexVo vo);

    /**
     * 机柜详情页面
     * @param id
     * @return
     */
    JSONObject getCabinetDetail(int id);

    /**
     * 机柜详情页面
     * @param vo
     * @return
     */
    int insertCabinet(CabinetIndexVo vo);


}
