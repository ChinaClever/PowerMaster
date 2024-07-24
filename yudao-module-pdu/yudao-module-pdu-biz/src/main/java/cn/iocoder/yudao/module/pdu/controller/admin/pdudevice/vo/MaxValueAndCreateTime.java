package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import cn.hutool.core.date.DateTime;
import lombok.Data;

@Data
public class MaxValueAndCreateTime {

    Double maxValue;

    DateTime maxTime;
}