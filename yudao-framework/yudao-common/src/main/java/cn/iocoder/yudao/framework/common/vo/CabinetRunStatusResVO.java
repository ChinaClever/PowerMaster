package cn.iocoder.yudao.framework.common.vo;

import lombok.Data;

@Data
public class CabinetRunStatusResVO {
/*0：未绑定 -> 未绑定任何设备
1：正常   -> 负载率
2：预警   —> 负载率大于80% 小于100%
3：告警   -> 负载率过大，大于100%
4：离线   -> 绑定的设备未在线*/
    private Integer unbound;
    private Integer normal;
    private Integer warning;
    private Integer alarm;
    private Integer offline;
    private Integer total;

}



