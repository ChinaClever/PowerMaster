package cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - 机柜环境记录 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class CabinetNotRealtimeEnvExcelVO {

    @ExcelProperty("位置")
    private String address;

    @ExcelProperty("时间")
    private String createTime;

    @NumberFormat("0.0")
    @ExcelProperty({"前门上","平均温度(°C)"})
    private Double tem_avg_value_front_1;

    @NumberFormat("0")
    @ExcelProperty({"前门上","平均湿度(%RH)"})
    private Double hum_avg_value_front_1;

    @NumberFormat("0.0")
    @ExcelProperty({"前门上","最大温度(°C)"})
    private Double tem_max_value_front_1;

    @ExcelProperty({"前门上","发生时间"})
    private String tem_max_time_front_1;

    @NumberFormat("0")
    @ExcelProperty({"前门上","最大湿度(%RH)"})
    private Double hum_max_value_front_1;

    @ExcelProperty({"前门上","发生时间"})
    private String hum_max_time_front_1;

    @NumberFormat("0.0")
    @ExcelProperty({"前门上","最小温度(°C)"})
    private Double tem_min_value_front_1;

    @ExcelProperty({"前门上","发生时间"})
    private String tem_min_time_front_1;

    @NumberFormat("0")
    @ExcelProperty({"前门上","最小湿度(%RH)"})
    private Double hum_min_value_front_1;

    @ExcelProperty({"前门上","发生时间"})
    private String hum_min_time_front_1;


    @NumberFormat("0.0")
    @ExcelProperty({"前门中","平均温度(°C)"})
    private Double tem_avg_value_front_2;

    @NumberFormat("0")
    @ExcelProperty({"前门中","平均湿度(%RH)"})
    private Double hum_avg_value_front_2;

    @NumberFormat("0.0")
    @ExcelProperty({"前门中","最大温度(°C)"})
    private Double tem_max_value_front_2;

    @ExcelProperty({"前门中","发生时间"})
    private String tem_max_time_front_2;

    @NumberFormat("0")
    @ExcelProperty({"前门中","最大湿度(%RH)"})
    private Double hum_max_value_front_2;

    @ExcelProperty({"前门中","发生时间"})
    private String hum_max_time_front_2;

    @NumberFormat("0.0")
    @ExcelProperty({"前门中","最小温度(°C)"})
    private Double tem_min_value_front_2;

    @ExcelProperty({"前门中","发生时间"})
    private String tem_min_time_front_2;

    @NumberFormat("0")
    @ExcelProperty({"前门中","最小湿度(%RH)"})
    private Double hum_min_value_front_2;

    @ExcelProperty({"前门中","发生时间"})
    private String hum_min_time_front_2;

    @NumberFormat("0.0")
    @ExcelProperty({"前门下","平均温度(°C)"})
    private Double tem_avg_value_front_3;

    @NumberFormat("0")
    @ExcelProperty({"前门下","平均湿度(%RH)"})
    private Double hum_avg_value_front_3;

    @NumberFormat("0.0")
    @ExcelProperty({"前门下","最大温度(°C)"})
    private Double tem_max_value_front_3;

    @ExcelProperty({"前门下","发生时间"})
    private String tem_max_time_front_3;

    @NumberFormat("0")
    @ExcelProperty({"前门下","最大湿度(%RH)"})
    private Double hum_max_value_front_3;

    @ExcelProperty({"前门下","发生时间"})
    private String hum_max_time_front_3;

    @NumberFormat("0.0")
    @ExcelProperty({"前门下","最小温度(°C)"})
    private Double tem_min_value_front_3;

    @ExcelProperty({"前门下","发生时间"})
    private String tem_min_time_front_3;

    @NumberFormat("0")
    @ExcelProperty({"前门下","最小湿度(%RH)"})
    private Double hum_min_value_front_3;

    @ExcelProperty({"前门下","发生时间"})
    private String hum_min_time_front_3;


    @NumberFormat("0.0")
    @ExcelProperty({"后门上","平均温度(°C)"})
    private Double tem_avg_value_black_1;

    @NumberFormat("0")
    @ExcelProperty({"后门上","平均湿度(%RH)"})
    private Double hum_avg_value_black_1;

    @NumberFormat("0.0")
    @ExcelProperty({"后门上","最大温度(°C)"})
    private Double tem_max_value_black_1;

    @ExcelProperty({"后门上","发生时间"})
    private String tem_max_time_black_1;

    @NumberFormat("0")
    @ExcelProperty({"后门上","最大湿度(%RH)"})
    private Double hum_max_value_black_1;

    @ExcelProperty({"后门上","发生时间"})
    private String hum_max_time_black_1;

    @NumberFormat("0.0")
    @ExcelProperty({"后门上","最小温度(°C)"})
    private Double tem_min_value_black_1;

    @ExcelProperty({"后门上","发生时间"})
    private String tem_min_time_black_1;

    @NumberFormat("0")
    @ExcelProperty({"后门上","最小湿度(%RH)"})
    private Double hum_min_value_black_1;

    @ExcelProperty({"后门上","发生时间"})
    private String hum_min_time_black_1;


    @NumberFormat("0.0")
    @ExcelProperty({"后门中","平均温度(°C)"})
    private Double tem_avg_value_black_2;

    @NumberFormat("0")
    @ExcelProperty({"后门中","平均湿度(%RH)"})
    private Double hum_avg_value_black_2;

    @NumberFormat("0.0")
    @ExcelProperty({"后门中","最大温度(°C)"})
    private Double tem_max_value_black_2;

    @ExcelProperty({"后门中","发生时间"})
    private String tem_max_time_black_2;

    @NumberFormat("0")
    @ExcelProperty({"后门中","最大湿度(%RH)"})
    private Double hum_max_value_black_2;

    @ExcelProperty({"后门中","发生时间"})
    private String hum_max_time_black_2;

    @NumberFormat("0.0")
    @ExcelProperty({"后门中","最小温度(°C)"})
    private Double tem_min_value_black_2;

    @ExcelProperty({"后门中","发生时间"})
    private String tem_min_time_black_2;

    @NumberFormat("0")
    @ExcelProperty({"后门中","最小湿度(%RH)"})
    private Double hum_min_value_black_2;

    @ExcelProperty({"后门中","发生时间"})
    private String hum_min_time_black_2;

    @NumberFormat("0.0")
    @ExcelProperty({"后门下","平均温度(°C)"})
    private Double tem_avg_value_black_3;

    @NumberFormat("0")
    @ExcelProperty({"后门下","平均湿度(%RH)"})
    private Double hum_avg_value_black_3;

    @NumberFormat("0.0")
    @ExcelProperty({"后门下","最大温度(°C)"})
    private Double tem_max_value_black_3;

    @ExcelProperty({"后门下","发生时间"})
    private String tem_max_time_black_3;

    @NumberFormat("0")
    @ExcelProperty({"后门下","最大湿度(%RH)"})
    private Double hum_max_value_black_3;

    @ExcelProperty({"后门下","发生时间"})
    private String hum_max_time_black_3;

    @NumberFormat("0.0")
    @ExcelProperty({"后门下","最小温度(°C)"})
    private Double tem_min_value_black_3;

    @ExcelProperty({"后门下","发生时间"})
    private String tem_min_time_black_3;

    @NumberFormat("0")
    @ExcelProperty({"后门下","最小湿度(%RH)"})
    private Double hum_min_value_black_3;

    @ExcelProperty({"后门下","发生时间"})
    private String hum_min_time_black_3;
}
