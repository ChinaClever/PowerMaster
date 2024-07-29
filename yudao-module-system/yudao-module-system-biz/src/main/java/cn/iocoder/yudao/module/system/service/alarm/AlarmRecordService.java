package cn.iocoder.yudao.module.system.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordRespVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordSaveReqVO;

import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 告警记录 Service 接口
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface AlarmRecordService {


    /**
     * 修改记录
     *
     * @param updateReqVO 邮箱账号信息
     */
    Integer updateRecord(@Valid AlarmRecordSaveReqVO updateReqVO);

    /**
     * 删除记录
     *
     * @param ids 编号
     */
    void deleteRecord(List<Integer> ids);

    /**
     * 获取告警记录分页信息
     *
     * @param pageReqVO 告警记录分页参数
     * @return 告警记录分页信息
     */
    PageResult<AlarmRecordRespVO> getRecordPage(AlarmRecordPageReqVO pageReqVO);

    /**
     * 告警等级统计
     * @return
     */
    Map<Object,Object> levelCount();

}
