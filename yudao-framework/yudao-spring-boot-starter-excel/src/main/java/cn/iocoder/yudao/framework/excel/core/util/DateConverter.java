package cn.iocoder.yudao.framework.excel.core.util;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * jiangjinchi
 * 2024/11/07 14:07
 * 时间格式设置
 */
public class DateConverter implements Converter<Object> {


    private static String PATTERN_YYYY_MM_DD = null;


    @Override
    public Class<?> supportJavaTypeKey() {
        return Date.class;
    }

    /**
     * 导入转换
     *
     * @param context
     * @return
     * @throws Exception
     */
    @Override
    public Object convertToJavaData(ReadConverterContext<?> context) throws Exception {
        return Converter.super.convertToJavaData(context);
    }

    /**
     * @param obj
     * @return
     * @throws Exception
     */
    @Override
    public WriteCellData<?> convertToExcelData(Object obj, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) throws Exception {
        Field field = contentProperty.getField();

        JsonFormat jsonFormat = field.getAnnotation(JsonFormat.class);
        if (jsonFormat == null) {
            return new WriteCellData();
        }
        PATTERN_YYYY_MM_DD = jsonFormat.pattern();

        if (field.getType().isInstance(String.class)) {
            String str = String.valueOf(obj);
            if (str == null) {
                return null;
            }
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD);
            Date date = sdf.parse(str);
            return new WriteCellData<>(sdf.format(date));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD);
            return new WriteCellData<>(sdf.format(obj));
        }
    }
}
