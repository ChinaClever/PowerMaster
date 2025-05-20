package cn.iocoder.yudao.framework.common.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class AisleCabinetBoxEditExcelVO {
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
    private String busKeya;

    /**
     * a路插接箱地址
     */
    @ExcelProperty(index = 8)
    private String boxAddra;

    /**
     * A路插接箱输出位编号
     */
    @ExcelProperty(index = 9)
    private Integer outletIda;

    /**
     * B路母线
     */
    @ExcelProperty(index = 10)
    private String busKeyb;

    /**
     * b路插接箱地址
     */
    @ExcelProperty(index = 11)
    private String boxAddrb;

    /**
     * B路插接箱输出位编号
     */
    @ExcelProperty(index = 12)
    private Integer outletIdb;


    /**
     * a路插接箱地址
     */
//    @ExcelProperty(index =)
//    private Integer boxIndexa;

    /**
     * b路插接箱地址
     */
//    @ExcelProperty(index =)
//    private Integer boxIndexb;


}
