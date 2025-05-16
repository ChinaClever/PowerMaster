package cn.iocoder.yudao.framework.common.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode
public class AisleCabinetPduEditExcelVO {
    @ExcelProperty(index = 0)
    private Integer roomId;
    @ExcelProperty(index = 1)
    private Integer aisleId;

    /**
     * 机柜id
     */
    @ExcelProperty(index = 2)
    private Integer cabinetId;

    /**
     * 机房名称
     */
    @ExcelProperty(index = 3)
    private String roomName;

    /**
     * 柜列名称
     */
    @ExcelProperty(index = 4)
    private String aisleName;

    /**
     * 机柜名称
     */
    @ExcelProperty(index = 5)
    private String cabinetName;

    /**
     * 机柜类型
     */
    @ExcelProperty(index = 6)
    private String cabinetType;

    /**
     * A路母线
     */
    @ExcelProperty(index = 7)
    private String pduKeya;

    /**
     * B路母线
     */
    @ExcelProperty(index = 8)
    private String pduKeyb;

    /**
     * 前-上
     */
    @ExcelProperty(index = 9)
    private String frontUpper;

    /**
     * 前-中
     */
    @ExcelProperty(index = 10)
    private String frontMiddle;

    /**
     * 前-下
     */
    @ExcelProperty(index = 11)
    private String frontLower;

    /**
     * 后-上
     */
    @ExcelProperty(index = 12)
    private String backUpper;

    /**
     * 后-中
     */
    @ExcelProperty(index = 13)
    private String backMiddle;

    /**
     * 后-下
     */
    @ExcelProperty(index = 14)
    private String backLower;

}
