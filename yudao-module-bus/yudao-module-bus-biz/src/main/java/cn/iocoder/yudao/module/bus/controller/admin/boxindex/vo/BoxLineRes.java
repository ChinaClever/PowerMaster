package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BoxLineRes extends BoxResBase{

    @Schema(description = "插接箱id")
    private Integer boxId;

    @Schema(description = "设备识别码")
    private String devKey;

    @Schema(description = "位置")
    private String Location;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "1的最大值")
    private Float L1MaxValue;
    @Schema(description = "1的最大时间")
    private String L1MaxValueTime;

    @Schema(description = "2的最大值")
    private Float L2MaxValue;
    @Schema(description = "2的最大时间")
    private String L2MaxValueTime;

    @Schema(description = "3的最大值")
    private Float L3MaxValue;
    @Schema(description = "3的最大时间")
    private String L3MaxValueTime;


    @Schema(description = "1的最大值")
    private Float L1MaxValueb;
    @Schema(description = "1的最大时间")
    private String L1MaxValueTimeb;

    @Schema(description = "2的最大值")
    private Float L2MaxValueb;
    @Schema(description = "2的最大时间")
    private String L2MaxValueTimeb;

    @Schema(description = "3的最大值")
    private Float L3MaxValueb;
    @Schema(description = "3的最大时间")
    private String L3MaxValueTimeb;



//    private Float L1MaxCur;
//
//    private String L1MaxCurTime;
//
//    private Float L1MaxVol;
//
//    private String L1MaxVolTime;
//
//    private Float L2MaxCur;
//
//    private String L2MaxCurTime;
//
//    private Float L2MaxVol;
//
//    private String L2MaxVolTime;
//
//    private Float L3MaxCur;
//
//    private String L3MaxCurTime;
//
//    private Float L3MaxVol;
//
//    private String L3MaxVolTime;
//
//    private Float L1MaxPow;
//
//    private String L1MaxPowTime;
//
//    private Float L2MaxPow;
//
//    private String L2MaxPowTime;
//
//    private Float L3MaxPow;
//
//    private String L3MaxPowTime;
}
