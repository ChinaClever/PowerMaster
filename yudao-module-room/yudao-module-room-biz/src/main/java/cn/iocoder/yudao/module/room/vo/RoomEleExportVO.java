package cn.iocoder.yudao.module.room.vo;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.excel.core.util.DateTimeConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class RoomEleExportVO {

    @JsonProperty("room_id")
    private int roomId;

    @ExcelProperty("机房名称")
    private String roomName;

    /**
     * 总电能
     */
    @NumberFormat("0.0")
    @ExcelProperty("总电能")
    @JsonProperty("ele_total")
    private double eleTotal;
    /**
     * a路电能
     */
    @NumberFormat("0.0")
    @ExcelProperty("a路电能")
    @JsonProperty("ele_a")
    private double eleA;

    /**
     * b路电能
     */
    @NumberFormat("0.0")
    @ExcelProperty("b路电能")
    @JsonProperty("ele_b")
    private double eleB;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", converter = DateTimeConverter.class)
    @JsonProperty("create_time")
    private DateTime createTime;
}
