package cn.iocoder.yudao.framework.excel.core.util;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;


public class DataSourceConverter implements Converter<Integer>{


    /**
     * excel转化后的类型
     *
     * @return
     */
    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    /**
     * excel中的数据类型,统一设置字符串
     *
     * @return
     */
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * 导入转换
     *
     * @param cellData            当前单元格对象
     * @param contentProperty     当前单元格属性
     * @param globalConfiguration
     * @return
     * @throws Exception
     */
    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        return null;
    }

    /**
     * 导出转化
     *
     * @param value               当前值
     * @param contentProperty     当前单元格属性
     * @param globalConfiguration
     * @return
     * @throws Exception
     */
    @Override
    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        String result="";
        if(0==value){
            result="定时记录";
        }else if(1==value){
            result="波动数据";
        }else if(2==value){
            result="突变数据";
        }else if(3==value){
            result="告警数据";
        }
        return new WriteCellData<>(result);
    }

}
