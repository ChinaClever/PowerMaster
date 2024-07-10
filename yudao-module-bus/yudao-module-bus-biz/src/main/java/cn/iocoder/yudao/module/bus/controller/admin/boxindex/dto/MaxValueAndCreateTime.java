package cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto;

import cn.hutool.core.date.DateTime;
import lombok.Data;

@Data
public class MaxValueAndCreateTime {

    Double maxValue;

    DateTime maxTime;
}
