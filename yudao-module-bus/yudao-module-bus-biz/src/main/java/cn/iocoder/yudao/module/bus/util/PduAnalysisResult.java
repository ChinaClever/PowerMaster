package cn.iocoder.yudao.module.bus.util;

import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusHdaLineAvgResVO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.*;

public class PduAnalysisResult {
    // 电压分析结果
    public static class VoltageResult {
        public BigDecimal maxVolValue;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date maxVolTime;
        public BigDecimal minVolValue;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date minVolTime;
        public BigDecimal avgVolValue;
//        public Date avgVolTime;
    }

    // 电流分析结果
    public static class CurrentResult {
        public BigDecimal maxCurValue;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date maxCurTime;
        public BigDecimal minCurValue;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date minCurTime;
        public BigDecimal avgCurValue;
//        public Date avgCurTime;
    }

    public static Map<String, Object> analyzePduData(List<BusHdaLineAvgResVO> dayList1, Integer dataType) {
        if (dayList1 == null || dayList1.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>();
        VoltageResult voltageResult = new VoltageResult();
        CurrentResult currentResult = new CurrentResult();

        if (dataType == 1) {
            // 初始化电压结果
            voltageResult.maxVolValue = dayList1.get(0).getVolMaxValue();
            voltageResult.maxVolTime = dayList1.get(0).getVolMaxTime();
            voltageResult.minVolValue = dayList1.get(0).getVolMaxValue();
            voltageResult.minVolTime = dayList1.get(0).getVolMaxTime();

            // 初始化电流结果
            currentResult.maxCurValue = dayList1.get(0).getCurMaxValue();
            currentResult.maxCurTime = dayList1.get(0).getCurMaxTime();
            currentResult.minCurValue = dayList1.get(0).getCurMaxValue();
            currentResult.minCurTime = dayList1.get(0).getCurMaxTime();

            for (BusHdaLineAvgResVO item : dayList1) {
                // 电压分析
                if (item.getVolMaxValue() != null && item.getVolMaxValue().compareTo(voltageResult.maxVolValue) > 0) {
                    voltageResult.maxVolValue = item.getVolMaxValue();
                    voltageResult.maxVolTime = item.getVolMaxTime();
                }
                if (item.getVolMaxValue() != null && item.getVolMaxValue().compareTo(voltageResult.minVolValue) < 0) {
                    voltageResult.minVolValue = item.getVolMaxValue();
                    voltageResult.minVolTime = item.getVolMaxTime();
                }

                // 电流分析
                if (item.getCurMaxValue() != null && item.getCurMaxValue().compareTo(currentResult.maxCurValue) > 0) {
                    currentResult.maxCurValue = item.getCurMaxValue();
                    currentResult.maxCurTime = item.getCurMaxTime();
                }
                if (item.getCurMaxValue() != null && item.getCurMaxValue().compareTo(currentResult.minCurValue) < 0) {
                    currentResult.minCurValue = item.getCurMaxValue();
                    currentResult.minCurTime = item.getCurMaxTime();
                }
            }

        } else if (dataType == 0) {
            for (BusHdaLineAvgResVO item : dayList1) {
                // 初始化电压结果
                voltageResult.maxVolValue = dayList1.get(0).getVolAvgValue();
                voltageResult.minVolValue = dayList1.get(0).getVolAvgValue();

                // 初始化电流结果
                currentResult.maxCurValue = dayList1.get(0).getCurAvgValue();
                currentResult.minCurValue = dayList1.get(0).getCurAvgValue();

                // 电压分析
                if (item.getVolAvgValue() != null && item.getVolAvgValue().compareTo(voltageResult.maxVolValue) > 0) {
                    voltageResult.maxVolValue = item.getVolMaxValue();

                }
                if (item.getVolAvgValue() != null && item.getVolAvgValue().compareTo(voltageResult.minVolValue) < 0) {
                    voltageResult.minVolValue = item.getVolAvgValue();

                }

                // 电流分析
                if (item.getCurAvgValue() != null && item.getCurAvgValue().compareTo(currentResult.maxCurValue) > 0) {
                    currentResult.maxCurValue = item.getCurAvgValue();
                }
                if (item.getCurAvgValue() != null && item.getCurAvgValue().compareTo(currentResult.minCurValue) < 0) {
                    currentResult.minCurValue = item.getCurAvgValue();
                }
            }
        } else if (dataType == -1) {
            // 初始化电压结果
            voltageResult.maxVolValue = dayList1.get(0).getVolMinValue();
            voltageResult.maxVolTime = dayList1.get(0).getVolMinTime();
            voltageResult.minVolValue = dayList1.get(0).getVolMinValue();
            voltageResult.minVolTime = dayList1.get(0).getVolMinTime();

            // 初始化电流结果
            currentResult.maxCurValue = dayList1.get(0).getCurMinValue();
            currentResult.maxCurTime = dayList1.get(0).getCurMinTime();
            currentResult.minCurValue = dayList1.get(0).getCurMinValue();
            currentResult.minCurTime = dayList1.get(0).getCurMinTime();

            for (BusHdaLineAvgResVO item : dayList1) {
                // 电压分析
                if (item.getCurMinValue() != null && item.getCurMinValue().compareTo(voltageResult.maxVolValue) > 0) {
                    voltageResult.maxVolValue = item.getCurMinValue();
                    voltageResult.maxVolTime = item.getVolMinTime();
                }
                if (item.getVolMaxValue() != null && item.getVolMaxValue().compareTo(voltageResult.minVolValue) < 0) {
                    voltageResult.minVolValue = item.getVolMaxValue();
                    voltageResult.minVolTime = item.getVolMaxTime();
                }

                // 电流分析
                if (item.getCurMinValue() != null && item.getCurMinValue().compareTo(currentResult.maxCurValue) > 0) {
                    currentResult.maxCurValue = item.getCurMinValue();
                    currentResult.maxCurTime = item.getCurMinTime();
                }
                if (item.getCurMinValue() != null && item.getCurMinValue().compareTo(currentResult.minCurValue) < 0) {
                    currentResult.minCurValue = item.getCurMinValue();
                    currentResult.minCurTime = item.getCurMinTime();
                }
            }

        }
        result.put("voltage", voltageResult);
        result.put("current", currentResult);
        return result;
    }
}
