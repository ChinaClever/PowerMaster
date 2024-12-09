package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import cn.hutool.core.date.DateTime;
import lombok.Data;

@Data
public class MaxValueAndCreateTime {

    Double maxValue;

    DateTime maxTime;
}