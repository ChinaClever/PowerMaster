package cn.iocoder.yudao.module.pdu.utils;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PduHdaLineHouResVO;
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

    public static Map<String, Object> analyzePduData(List<PduHdaLineHouResVO> dayList1, Integer dataType) {
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

            for (PduHdaLineHouResVO item : dayList1) {
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
            // 初始化电压结果
            voltageResult.maxVolValue = dayList1.get(0).getVolValue();
            voltageResult.minVolValue = dayList1.get(0).getVolValue();

            // 初始化电流结果
            currentResult.maxCurValue = dayList1.get(0).getCurValue();
            currentResult.minCurValue = dayList1.get(0).getCurValue();
            for (PduHdaLineHouResVO item : dayList1) {
                // 电压分析
                if (item.getVolValue() != null && item.getVolValue().compareTo(voltageResult.maxVolValue) > 0) {
                    voltageResult.maxVolValue = item.getVolValue();

                }
                if (item.getVolValue() != null && item.getVolValue().compareTo(voltageResult.minVolValue) < 0) {
                    voltageResult.minVolValue = item.getVolValue();

                }

                // 电流分析
                if (item.getCurValue() != null && item.getCurValue().compareTo(currentResult.maxCurValue) > 0) {
                    currentResult.maxCurValue = item.getCurValue();
                }
                if (item.getCurValue() != null && item.getCurValue().compareTo(currentResult.minCurValue) < 0) {
                    currentResult.minCurValue = item.getCurValue();
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

            for (PduHdaLineHouResVO item : dayList1) {
                // 电压分析
                if (item.getVolMinValue() != null && item.getVolMinValue().compareTo(voltageResult.maxVolValue) > 0) {
                    voltageResult.maxVolValue = item.getCurMinValue();
                    voltageResult.maxVolTime = item.getVolMinTime();
                }
                if (item.getVolMinValue() != null && item.getVolMinValue().compareTo(voltageResult.minVolValue) < 0) {
                    voltageResult.minVolValue = item.getVolMinValue();
                    voltageResult.minVolTime = item.getVolMinTime();
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