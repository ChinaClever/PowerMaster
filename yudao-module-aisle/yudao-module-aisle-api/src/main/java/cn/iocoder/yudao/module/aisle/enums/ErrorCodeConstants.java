package cn.iocoder.yudao.module.aisle.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode INDEX_NOT_EXISTS = new ErrorCode(11111246, "通道列不存在");

    ErrorCode CURBALANCE_COLOR_NOT_EXISTS = new ErrorCode(11111247, "母线不平衡度颜色不存在");

    ErrorCode BOX_CURBALANCE_COLOR_NOT_EXISTS = new ErrorCode(11111248, "插接箱不平衡度颜色不存在");



}
