package cn.iocoder.yudao.framework.common.util.string;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 *
 * @author 芋道源码
 */
public class StrUtils {

    public static String maxLength(CharSequence str, int maxLength) {
        return StrUtil.maxLength(str, maxLength - 3); // -3 的原因，是该方法会补充 ... 恰好
    }

    /**
     * 给定字符串是否以任何一个字符串开始
     * 给定字符串和数组为空都返回 false
     *
     * @param str      给定字符串
     * @param prefixes 需要检测的开始字符串
     * @since 3.0.6
     */
    public static boolean startWithAny(String str, Collection<String> prefixes) {
        if (StrUtil.isEmpty(str) || ArrayUtil.isEmpty(prefixes)) {
            return false;
        }

        for (CharSequence suffix : prefixes) {
            if (StrUtil.startWith(str, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    public static List<Long> splitToLong(String value, CharSequence separator) {
        long[] longs = StrUtil.splitToLong(value, separator);
        return Arrays.stream(longs).boxed().collect(Collectors.toList());
    }

    public static List<Integer> splitToInteger(String value, CharSequence separator) {
        int[] integers = StrUtil.splitToInt(value, separator);
        return Arrays.stream(integers).boxed().collect(Collectors.toList());
    }

    /**
     * 移除字符串中，包含指定字符串的行
     *
     * @param content 字符串
     * @param sequence 包含的字符串
     * @return 移除后的字符串
     */
    public static String removeLineContains(String content, String sequence) {
        if (StrUtil.isEmpty(content) || StrUtil.isEmpty(sequence)) {
            return content;
        }
        return Arrays.stream(content.split("\n"))
                .filter(line -> !line.contains(sequence))
                .collect(Collectors.joining("\n"));
    }


    public static <T> String redisKeyByLoginId(String loginId, String methodName, T vo) {
        StringBuilder str = new StringBuilder();
        try {
            if (ObjectUtils.isNotEmpty(methodName)) {
                str.append(methodName + "-");
            }
            if (ObjectUtils.isNotEmpty(loginId)) {
                str.append("user:" + loginId + "-");
            }

            if (ObjectUtils.isEmpty(vo)) {
                return str.toString();
            }
            Class cls = vo.getClass();
            Field[] fields = cls.getDeclaredFields();

            if (!ObjectUtils.isEmpty(vo)) {
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    f.setAccessible(true);
                    if (!ObjectUtils.isEmpty(f.get(vo))) {
                        if (Objects.equals(String[].class, f.getType())) {
                            String str4 = StringUtils.join((String[]) f.get(vo), ",");
                            str.append("-" + f.getName() + ":" + str4);
                            continue;
                        }
                        if (Objects.equals(List.class, f.getType())) {
                            String str4 = StringUtils.join((List) f.get(vo), ",");
                            str.append("-" + f.getName() + ":" + str4);
                            continue;
                        }
                        str.append("-" + f.getName() + ":" + f.get(vo).toString());
                    }
                }
            }
            // 判断父类是不是PageDTO
//            Class superclass = cls.getSuperclass();
//            if(!ObjectUtils.isEmpty(superclass) && superclass.getSimpleName().equals("PageDTO")){
//                Field[] declaredFields = superclass.getDeclaredFields();
//                for(int i=0;i<declaredFields.length;i++) {
//                    // 设置可以访问私有变量
//                    declaredFields[i].setAccessible(true);
//                    // 获取属性的名字
//                    String name = declaredFields[i].getName();
//                    if (name.equals("pageSize")) {
//
//                        // 将属性名字的首字母大写
//                        name = name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
//                        // 组装Get方法
//                        Method m = superclass.getDeclaredMethod("get" + name);
//                        // 调用Get方法
//                        Object o = m.invoke(vo);
//                        str.append("-" + name+ ":" + o.toString());
//                    }
//                    if (name.equals("pageNum")) {
//
//                        // 将属性名字的首字母大写
//                        name = name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
//                        // 组装Get方法
//                        Method m = superclass.getDeclaredMethod("get" + name);
//                        // 调用Get方法
//                        Object o = m.invoke(vo);
//                        str.append("-" + name+ ":" + o.toString());
//                    }
//                }
//            }
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str.toString();
    }

}
