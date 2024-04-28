package cn.iocoder.yudao.module.cabinet.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {
    ErrorCode HISTORY_DATA_NOT_EXISTS = new ErrorCode(11111234, "pdu历史数据不存在");
    ErrorCode PDU_DEVICE_NOT_EXISTS = new ErrorCode(11111235, "PDU设备不存在");
}
