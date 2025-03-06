package cn.iocoder.yudao.framework.common.exception;

import lombok.Getter;
import lombok.Setter;


/**
 * @Description //Api断言类
 * jiangjinchi
 * 2021/3/26 14:03
 **/
@Getter
@Setter
public class BusinessAssert {

    public static void error(String message) {
        throw new BusinessException(message);
    }

    public static void error(Integer code, String message) {
        throw new BusinessException(code, message);
    }

    public static void error(ErrorCode apiEnum) {
        throw new BusinessException(apiEnum.getCode(), apiEnum.getMsg());
    }

    public static void error(boolean expression, ErrorCode apiEnum) {
        if (expression) {
            throw new BusinessException(apiEnum);
        }
    }
}
