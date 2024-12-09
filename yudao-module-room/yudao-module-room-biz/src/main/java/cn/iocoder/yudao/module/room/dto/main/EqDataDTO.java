package cn.iocoder.yudao.module.room.dto.main;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 主页面用能数据
 * @date 2024/6/21 15:49
 */
@Data
public class EqDataDTO {


    /**
     * 机房数据
     */
    List<RoomEq> roomEqList;


    /**
     * 上周电量
     */
    private Double lastWeekEqTotal;

    /**
     * 上月电量
     */
    private Double lastMonthEqTotal;

    /**
     * 今日电量
     */
    private Double todayEqTotal;

    /**
     * 昨日用能
     */
    private Double yesterdayEqTotal;


    @Data
    public  class  RoomEq{
        /**
         * 上周电量
         */
        @Schema(description = "上周电量", example = "1")
        private Double lastWeekEq;

        /**
         * 上月电量
         */
        @Schema(description = "上月电量", example = "1")
        private Double lastMonthEq;

        /**
         * 今日电量
         */
        @Schema(description = "今日电量", example = "1")
        private Double todayEq;

        /**
         * 昨日用能
         */
        @Schema(description = "昨日用能", example = "1")
        private Double yesterdayEq;

        /**
         * 机房iD
         */
        private Integer id;

        private String name;
    }
}


