package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import cn.hutool.core.date.DateTime;
import lombok.Data;

@Data
public class MaxCurAndOtherData {
    Double maxValue;

    DateTime maxTime;

    Integer pdu_id;

    Integer line_id;

    public MaxCurAndOtherData(Double maxValue, DateTime maxTime, Integer pdu_id, Integer line_id) {
        this.maxValue = maxValue;
        this.maxTime = maxTime;
        this.pdu_id = pdu_id;
        this.line_id = line_id;
    }
    public MaxCurAndOtherData() {
    }
}
