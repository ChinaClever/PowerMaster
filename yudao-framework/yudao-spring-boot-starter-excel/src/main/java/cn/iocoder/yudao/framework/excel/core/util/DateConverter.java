package cn.iocoder.yudao.framework.excel.core.util;

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
public class DateConverter  implements Converter<Date> {


    private static  final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";


    @Override
    public Class<Date> supportJavaTypeKey() {
        return Date.class;
    }


    @Override
    public WriteCellData<String> convertToExcelData(WriteConverterContext<Date> context) throws Exception {
        Date date = context.getValue();
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD);
        return new WriteCellData<>(sdf.format(date));
    }
}
