package cn.iocoder.yudao.module.cabinet.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetIndexDTO;
import cn.iocoder.yudao.module.cabinet.vo.CabinetIndexVo;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/4/28 14:24
 */
public interface CabinetService {

    PageResult<CabinetIndexDTO>  getPageCabinet(CabinetIndexVo vo);

}
