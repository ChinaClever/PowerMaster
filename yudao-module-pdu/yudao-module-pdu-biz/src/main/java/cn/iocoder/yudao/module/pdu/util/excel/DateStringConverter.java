package cn.iocoder.yudao.module.pdu.util.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateStringConverter implements Converter<String> {


    private static  final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";


    @Override
    public Class<String> supportJavaTypeKey() {
        return String.class;
    }


    @Override
    public WriteCellData<String> convertToExcelData(WriteConverterContext<String> context) throws Exception {
        String str = context.getValue();
        if (str == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD);
        Date date = sdf.parse(str);
        return new WriteCellData<>(sdf.format(date));
    }
}
