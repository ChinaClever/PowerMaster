package cn.iocoder.yudao.module.room.service.roomstatisconfig;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.room.controller.admin.roomstatisconfig.vo.*;
import cn.iocoder.yudao.module.room.dal.dataobject.roomstatisconfig.RoomStatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 机房计算服务配置 Service 接口
 *
 * @author clever
 */
public interface RoomStatisConfigService {

    /**
     * 创建机房计算服务配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createStatisConfig(@Valid RoomStatisConfigSaveReqVO createReqVO);

    /**
     * 更新机房计算服务配置
     *
     * @param updateReqVO 更新信息
     */
    void updateStatisConfig(@Valid RoomStatisConfigSaveReqVO updateReqVO);

    /**
     * 删除机房计算服务配置
     *
     * @param id 编号
     */
    void deleteStatisConfig(Integer id);

    /**
     * 获得机房计算服务配置
     *
     * @param id 编号
     * @return 机房计算服务配置
     */
    RoomStatisConfigDO getStatisConfig(Integer id);

    /**
     * 获得机房计算服务配置分页
     *
     * @param pageReqVO 分页查询
     * @return 机房计算服务配置分页
     */
    PageResult<RoomStatisConfigDO> getStatisConfigPage(RoomStatisConfigPageReqVO pageReqVO);

}