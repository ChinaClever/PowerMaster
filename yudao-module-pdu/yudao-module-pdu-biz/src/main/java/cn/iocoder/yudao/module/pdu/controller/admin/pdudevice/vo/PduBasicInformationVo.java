package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PduBasicInformationVo {

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "总有功功率")
    private Double powActive;

    @Schema(description = "总无功功率")
    private Double powReactive;

    @Schema(description = "总视在功率")
    private Double powApparent;

    @Schema(description = "总功率因数")
    private Double powerFactor;

    @Schema(description = "耗电量")
    private Double eleActive;

    @Schema(description = "电压不平衡度")
    private Double volUnbalance;

    @Schema(description = "电流不平衡度")
    private Double curUnbalance;
}
