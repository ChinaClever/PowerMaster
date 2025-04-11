package cn.iocoder.yudao.framework.excel.core.util;

import cn.hutool.core.date.DateTime;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * jiangjinchi
 * 2024/11/07 14:07
 * 时间格式设置
 */
public class DateTimeConverter implements Converter<DateTime> {


    private static  final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd HH:mm:ss";


    @Override
    public Class<String> supportJavaTypeKey() {
        return String.class;
    }


    @Override
    public WriteCellData<String> convertToExcelData(WriteConverterContext<DateTime> context) throws Exception {
        DateTime str = context.getValue();
        if (str == null) {
            return null;
        }
//        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD);
//        Date date = sdf.parse(str);
        return new WriteCellData<>(str.toString(PATTERN_YYYY_MM_DD));
    }
}
