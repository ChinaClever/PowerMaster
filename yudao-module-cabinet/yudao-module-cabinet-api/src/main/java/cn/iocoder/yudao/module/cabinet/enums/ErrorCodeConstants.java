package cn.iocoder.yudao.module.cabinet.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode INDEX_NOT_EXISTS = new ErrorCode(11111245, "机柜索引不存在");

    ErrorCode STATIS_CONFIG_NOT_EXISTS = new ErrorCode(11111246, "机柜计算服务配置不存在");

    ErrorCode TEM_COLOR_NOT_EXISTS = new ErrorCode(11111247, "机柜温度颜色不存在");

}
