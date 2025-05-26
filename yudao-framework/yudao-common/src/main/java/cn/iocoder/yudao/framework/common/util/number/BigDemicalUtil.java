package cn.iocoder.yudao.framework.common.util.number;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * BigDecimal/double 工具类
 *
 * @Title:
 * @Description:
 * @Company:
 * @Author:yctong
 * @Created Date:2020年4月23日
 */
public class BigDemicalUtil {

    public static BigDecimal ZERO = new BigDecimal("0.00");
    public static BigDecimal TWO = new BigDecimal("2.00");

    private BigDemicalUtil() {
    }

    public static BigDecimal valueOf(Object v1) {
        if (v1 == null) {
            return ZERO;
        }
        if (v1 instanceof String) {
            if ("".equals(v1)) {
                return ZERO;
            }
            return new BigDecimal(String.valueOf(v1));
        }
        if (v1 instanceof BigDecimal) {
            return (BigDecimal) v1;
        }
        return ZERO;
    }

    public static BigDecimal mul(BigDecimal bd, Long v1) {
        BigDecimal bd1 = new BigDecimal(v1);
        return bd.multiply(bd1);
    }

    public static BigDecimal mul(BigDecimal bd, Integer v1) {
        BigDecimal bd1 = new BigDecimal(v1);
        return bd.multiply(bd1);
    }

    public static BigDecimal mul(BigDecimal bd, Double v1) {
        BigDecimal bd1 = new BigDecimal(v1);
        return bd.multiply(bd1);
    }

    public static BigDecimal add(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null) {
            bd1 = ZERO;
        }

        if (bd2 == null) {
            bd2 = ZERO;
        }
        return bd1.add(bd2);
    }

    public static BigDecimal sub(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null) {
            bd1 = ZERO;
        }

        if (bd2 == null) {
            bd2 = ZERO;
        }
        return bd1.subtract(bd2);
    }

    public static BigDecimal mul(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null) {
            bd1 = ZERO;
        }

        if (bd2 == null) {
            bd2 = ZERO;
        }
        return bd1.multiply(bd2);
    }

    public static BigDecimal div(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null) {
            bd1 = ZERO;
        }

        if (bd2 == null) {
            bd2 = ZERO;
        }
        return bd1.divide(bd2, 4, BigDecimal.ROUND_DOWN);
    }

    public static int compareTo(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null) {
            bd1 = ZERO;
        }

        if (bd2 == null) {
            bd2 = ZERO;
        }
        return bd1.compareTo(bd2);
    }

    public static BigDecimal abs(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null) {
            bd1 = ZERO;
        }

        if (bd2 == null) {
            bd2 = ZERO;
        }
        return bd1.subtract(bd2).abs();
    }

    public static BigDecimal formatComma2BigDecimal(Object obj) {
        String val = String.valueOf(obj);
        if (val == null) {
            return new BigDecimal("0.00");
        }
        val = val.replaceAll(",", "");
        if (!isNumber(val)) {
            return new BigDecimal("0.00");
        }
        BigDecimal decimal = new BigDecimal(val);
        decimal = decimal.setScale(2, BigDecimal.ROUND_HALF_DOWN);

        return decimal;

    }

    @SuppressWarnings("unused")
    private String formatCommaAnd2Point(Object obj) {
        BigDecimal decimal = formatComma2BigDecimal(obj);

        DecimalFormat df = new DecimalFormat("#,###.00");
        String decimalStr = df.format(decimal).equals(".00") ? "0.00" : df.format(decimal);
        if (decimalStr.startsWith(".")) {
            decimalStr = "0" + decimalStr;
        }
        return decimalStr;
    }

    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            if (value.contains(".")) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNumber(String value) {
        return isInteger(value) || isDouble(value);
    }

    /**
     * BigDecimal的加法运算封装
     *
     * @param b1
     * @param bn
     * @return
     */
    public static BigDecimal safeAdd(BigDecimal b1, BigDecimal... bn) {
        if (null == b1) {
            b1 = BigDecimal.ZERO;
        }

        if (null != bn) {
            for (BigDecimal b : bn) {
                b1 = b1.add(null == b ? BigDecimal.ZERO : b);
            }
        }

        return b1;
    }

    /**
     * BigDecimal的加法运算封装
     *
     * @param b1
     * @param bn
     * @return
     */
    public static BigDecimal safeAdd(Integer scale, BigDecimal b1, BigDecimal... bn) {
        if (null == b1) {
            b1 = BigDecimal.ZERO;
        }

        if (null != bn) {
            for (BigDecimal b : bn) {
                b1 = b1.add(null == b ? BigDecimal.ZERO : b);
            }
        }

        return b1.setScale(scale, BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * 计算金额方法
     *
     * @param b1 BigDecimal的安全减法运算
     * @param bn
     * @return
     */
    public static BigDecimal safeSubtract(BigDecimal b1, BigDecimal... bn) {
        return safeSubtract(true, b1, bn);
    }


    /**
     * 计算金额方法
     *
     * @param b1 BigDecimal的安全减法运算
     * @param bn
     * @return
     */
    public static BigDecimal safeSubtract(Integer scale, BigDecimal b1, BigDecimal... bn) {
        return safeSubtract(true, b1, bn).setScale(scale, BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * BigDecimal的除法运算封装，如果除数或者被除数为0，返回默认值
     *
     * @param b1
     * @param b2
     * @param defaultValue
     * @return
     */
    public static <T extends Number> BigDecimal safeDivide(T b1, T b2, BigDecimal defaultValue) {
        if (null == b1 || null == b2) {
            return defaultValue;
        }
        try {
            return BigDecimal.valueOf(b1.doubleValue()).divide(BigDecimal.valueOf(b2.doubleValue()), 4,
                    BigDecimal.ROUND_HALF_DOWN);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static <T extends Number> BigDecimal setScale(T b1,Integer scale) {
        if (null == b1) {
         return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(b1.doubleValue()).setScale(scale, BigDecimal.ROUND_HALF_UP);

    }

    public static <T extends Number> BigDecimal setScale1(T b1,Integer scale) {
        if (null == b1) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(b1.doubleValue()).setScale(scale, RoundingMode.HALF_UP);
        //RoundingMode.HALF_UP
    }
    /**
     * BigDecimal的乘法运算封装
     *
     * @param b1
     * @param b2
     * @return
     */
    public static <T extends Number> BigDecimal safeMultiply(T b1, T b2) {
        if (null == b1 || null == b2) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(b1.doubleValue()).multiply(BigDecimal.valueOf(b2.doubleValue()))
                .setScale(2, BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * BigDecimal的乘法运算封装
     *
     * @param b1
     * @param b2
     * @return
     */
    public static <T extends Number> BigDecimal safeMultiply(Integer scale, T b1, T b2) {
        if (null == b1 || null == b2) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(b1.doubleValue()).multiply(BigDecimal.valueOf(b2.doubleValue()))
                .setScale(scale, BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * 金额除法计算，返回2位小数（具体的返回多少位大家自己看着改吧）
     *
     * @param b1
     * @param b2
     * @return
     */
    public static <T extends Number> BigDecimal safeDivide(T b1, T b2) {
        return safeDivide(b1, b2, BigDecimal.ZERO);
    }

    /**
     * 金额除法计算，返回自定义位小数（具体的返回多少位大家自己看着改吧）
     *
     * @param b1
     * @param b2
     * @return
     */
    public static <T extends Number> BigDecimal safeDivideNum(Integer scale, T b1, T b2) {

        return safeDivideByNum(b1, b2, BigDecimal.ZERO, scale);
    }

    public static <T extends Number> BigDecimal safeDivideByNum(T b1, T b2, BigDecimal defaultValue, Integer scale) {
        if (null == b1 || null == b2) {
            return defaultValue;
        }
        try {
            return BigDecimal.valueOf(b1.doubleValue()).divide(BigDecimal.valueOf(b2.doubleValue()), scale,
                    BigDecimal.ROUND_DOWN);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * BigDecimal的安全减法运算
     *
     * @param isZero
     * @param b1
     * @param bn
     * @return
     */
    public static BigDecimal safeSubtract(Boolean isZero, BigDecimal b1, BigDecimal... bn) {
        if (null == b1) {
            b1 = BigDecimal.ZERO;
        }
        BigDecimal r = b1;
        if (null != bn) {
            for (BigDecimal b : bn) {
                r = r.subtract((null == b ? BigDecimal.ZERO : b));
            }
        }
        return isZero ? (r.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : r) : r;
    }

    /**
     * 提供精确的加法运算。
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算。
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    public static Integer sub(Integer v1, Integer v2) {
        if (null == v1) {
            return BigDecimal.ZERO.intValue();
        }
        if (null == v2) {
            return v1;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        if (b1.subtract(b2).intValue() > 0) {
            return b1.subtract(b2).intValue();
        } else {
            return BigDecimal.ZERO.intValue();
        }

    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    /**
     * 提供精确的类型转换(Float)
     *
     * @param v 需要被转换的数字
     * @return 返回转换结果
     */
    public static float convertsToFloat(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.floatValue();
    }

    /**
     * 提供精确的类型转换(Int)不进行四舍五入
     *
     * @param v 需要被转换的数字
     * @return 返回转换结果
     */
    public static int convertsToInt(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.intValue();
    }

    /**
     * 提供精确的类型转换(Long)
     *
     * @param v 需要被转换的数字
     * @return 返回转换结果
     */
    public static long convertsToLong(double v) {
        BigDecimal b = new BigDecimal(v);
        return b.longValue();
    }

    /**
     * 返回两个数中大的一个值
     *
     * @param v1 需要被对比的第一个数
     * @param v2 需要被对比的第二个数
     * @return 返回两个数中大的一个值
     */
    public static double returnMax(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.max(b2).doubleValue();
    }

    /**
     * 返回两个数中小的一个值
     *
     * @param v1 需要被对比的第一个数
     * @param v2 需要被对比的第二个数
     * @return 返回两个数中小的一个值
     */
    public static double returnMin(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.min(b2).doubleValue();
    }

    /**
     * 精确对比两个数字
     *
     * @param v1 需要被对比的第一个数
     * @param v2 需要被对比的第二个数
     * @return 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
     */
    public static int compareTo(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.compareTo(b2);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入。
     *
     * @param value1 被除数
     * @param value2 除数
     * @param scale  表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double value1, double value2, int scale) throws IllegalAccessException {
        if (scale < 0) {
            // 如果精确范围小于0，抛出异常信息。
            throw new IllegalArgumentException("精确度不能小于0");
        } else if (value2 == 0) {
            // 如果除数为0，抛出异常信息。
            throw new IllegalArgumentException("除数不能为0");
        }
        BigDecimal b1 = new BigDecimal(Double.valueOf(value1));
        BigDecimal b2 = new BigDecimal(Double.valueOf(value2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    /**
     * 提供精确加法计算的add方法，确认精确度
     *
     * @param value1 被加数
     * @param value2 加数
     * @param scale  小数点后保留几位
     * @return 两个参数求和之后，按精度四舍五入的结果
     */
    public static double add(double value1, double value2, int scale) {
        return round(add(value1, value2), scale);
    }

    /**
     * 提供精确减法运算的sub方法，确认精确度
     *
     * @param value1 被减数
     * @param value2 减数
     * @param scale  小数点后保留几位
     * @return 两个参数的求差之后，按精度四舍五入的结果
     */
    public static double sub(double value1, double value2, int scale) {
        return round(sub(value1, value2), scale);
    }

    /**
     * 提供精确乘法运算的mul方法，确认精确度
     *
     * @param value1 被乘数
     * @param value2 乘数
     * @param scale  小数点后保留几位
     * @return 两个参数的乘积之后，按精度四舍五入的结果
     */
    public static double mul(double value1, double value2, int scale) {
        return round(mul(value1, value2), scale);
    }

}