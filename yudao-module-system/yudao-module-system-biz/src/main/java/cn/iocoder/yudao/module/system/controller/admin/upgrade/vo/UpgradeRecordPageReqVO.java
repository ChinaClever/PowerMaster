package cn.iocoder.yudao.module.system.controller.admin.upgrade.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 升级记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UpgradeRecordPageReqVO extends PageParam {

    /**
     * 设备ip
     */
    private String devIp;


}
