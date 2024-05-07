package cn.iocoder.yudao.module.pdu.service.statisconfig;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.pdu.controller.admin.statisconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.statisconfig.StatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * pdu计算服务配置 Service 接口
 *
 * @author clever
 */
public interface StatisConfigService {

    /**
     * 创建pdu计算服务配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createStatisConfig(@Valid StatisConfigSaveReqVO createReqVO);

    /**
     * 更新pdu计算服务配置
     *
     * @param updateReqVO 更新信息
     */
    void updateStatisConfig(@Valid StatisConfigSaveReqVO updateReqVO);

    /**
     * 删除pdu计算服务配置
     *
     * @param id 编号
     */
    void deleteStatisConfig(Integer id);

    /**
     * 获得pdu计算服务配置
     *
     * @param id 编号
     * @return pdu计算服务配置
     */
    StatisConfigDO getStatisConfig(Integer id);

    /**
     * 获得pdu计算服务配置分页
     *
     * @param pageReqVO 分页查询
     * @return pdu计算服务配置分页
     */
    PageResult<StatisConfigDO> getStatisConfigPage(StatisConfigPageReqVO pageReqVO);

}