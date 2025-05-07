package cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Schema(description = "管理后台 - 机柜环境记录 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class CabinetRealtimeEnvExcelVO {

    @ExcelProperty("位置")
    private String address;

    @ExcelProperty("时间")
    private String createTime;

    @ExcelProperty(value = {"前门上","温度(°C)"})
    private Double temValuefront1;

    @ExcelProperty(value = {"前门上","湿度(%RH)"})
    private Double humValuefront1;

    @ExcelProperty(value = {"前门中","温度(°C)"})
    private Double temValuefront2;

    @ExcelProperty(value = {"前门中","湿度(%RH)"})
    private Double humValuefront2;

    @ExcelProperty(value = {"前门下","温度(°C)"})
    private Double temValuefront3;

    @ExcelProperty(value = {"前门下","湿度(%RH)"})
    private Double humValuefront3;

    @ExcelProperty(value = {"后门上","温度(°C)"})
    private Double temValueblack1;

    @ExcelProperty(value = {"后门上","湿度(%RH)"})
    private Double humValueblack1;

    @ExcelProperty(value = {"后门中","温度(°C)"})
    private Double temValueblack2;

    @ExcelProperty(value = {"后门中","湿度(%RH)"})
    private Double humValueblack2;

    @ExcelProperty(value = {"后门下","温度(°C)"})
    private Double temValueblack3;

    @ExcelProperty(value = {"后门下","湿度(%RH)"})
    private Double humValueblack3;

}
