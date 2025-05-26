package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaxCurAndOtherData {

    private BigDecimal maxValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime maxTime;

    private Integer pduId;

    private Integer lineId;

    private String unit;

    private String location;

    private String devKey;
}
