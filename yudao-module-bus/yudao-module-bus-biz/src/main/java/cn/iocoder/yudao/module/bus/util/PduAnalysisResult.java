package cn.iocoder.yudao.module.bus.util;

import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineHourDo;
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

    }

    // 负载率分析结果
    public static class LoadRateResult {
        public Float loadRateMax;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date loadRateMaxTime;

        public Float loadRateMin;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date loadRateMinTime;

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
            // 初始化电压结果
            voltageResult.maxVolValue = dayList1.get(0).getVolAvgValue();
            voltageResult.minVolValue = dayList1.get(0).getVolAvgValue();

            // 初始化电流结果
            currentResult.maxCurValue = dayList1.get(0).getCurAvgValue();
            currentResult.minCurValue = dayList1.get(0).getCurAvgValue();
            for (BusHdaLineAvgResVO item : dayList1) {


                // 电压分析
                if (item.getVolAvgValue() != null && item.getVolAvgValue().compareTo(voltageResult.maxVolValue) > 0) {
                    voltageResult.maxVolValue = item.getVolAvgValue();

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


    public static Map<String, Object> analyzeLoadRateData(List<BusLineHourDo> dayList1, Integer dataType) {
        if (dayList1 == null || dayList1.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>();
        LoadRateResult loadRateResult = new LoadRateResult();

        if (dataType == 1) {
            // 初始化
            loadRateResult.loadRateMax = dayList1.get(0).getLoadRateMaxValue();
            loadRateResult.loadRateMaxTime = dayList1.get(0).getLoadRateMaxTime();
            loadRateResult.loadRateMin = dayList1.get(0).getLoadRateMaxValue();
            loadRateResult.loadRateMinTime = dayList1.get(0).getLoadRateMaxTime();

            for (BusLineHourDo item : dayList1) {

                // 电流分析
                if (item.getLoadRateMaxValue() > loadRateResult.loadRateMax) {
                    loadRateResult.loadRateMax = item.getLoadRateMaxValue();
                    loadRateResult.loadRateMaxTime = item.getLoadRateMaxTime();
                }
                if (item.getLoadRateMaxValue() < loadRateResult.loadRateMax) {
                    loadRateResult.loadRateMin = item.getLoadRateMaxValue();
                    loadRateResult.loadRateMinTime = item.getLoadRateMaxTime();
                }
            }

        } else if (dataType == 0) {
            for (BusLineHourDo item : dayList1) {
                // 初始化
                loadRateResult.loadRateMax = dayList1.get(0).getLoadRateAvgValue();
                loadRateResult.loadRateMin = dayList1.get(0).getLoadRateAvgValue();

                if (item.getLoadRateAvgValue() > loadRateResult.loadRateMax) {
                    loadRateResult.loadRateMax = item.getLoadRateAvgValue();

                }
                if (item.getLoadRateAvgValue() < loadRateResult.loadRateMax) {
                    loadRateResult.loadRateMin = item.getLoadRateAvgValue();
                }
            }

        } else if (dataType == -1) {
            // 初始化
            loadRateResult.loadRateMax = dayList1.get(0).getLoadRateMinValue();
            loadRateResult.loadRateMaxTime = dayList1.get(0).getLoadRateMinTime();
            loadRateResult.loadRateMin = dayList1.get(0).getLoadRateMinValue();
            loadRateResult.loadRateMinTime = dayList1.get(0).getLoadRateMinTime();

            for (BusLineHourDo item : dayList1) {
                // 电压分析
                if (item.getLoadRateMinValue() > loadRateResult.loadRateMax) {
                    loadRateResult.loadRateMax = item.getLoadRateMinValue();
                    loadRateResult.loadRateMaxTime = item.getLoadRateMinTime();
                }
                if (item.getLoadRateMinValue() < loadRateResult.loadRateMax) {
                    loadRateResult.loadRateMin = item.getLoadRateMinValue();
                    loadRateResult.loadRateMinTime = item.getLoadRateMinTime();
                }
            }
        }
        result.put("loadRateTage", loadRateResult);

        return result;
    }



    /**
     * 更新数值
     *
     * @param powerData
     * @param maxValue
     * @param maxTime
     * @param minValue
     * @param minTime
     * @param dataType
     */
    private static void updatePowerData(PowerData powerData, Float maxValue, String maxTime, Float avgValue, Float minValue, String minTime, Integer dataType) {
        if (dataType == 1) {
            updateExtremes(powerData, maxValue, maxTime, maxValue, maxTime);
        } else if (dataType == 0) {
            updateExtremes(powerData, avgValue, "无", avgValue, "无");
        } else if (dataType == -1) {
            updateExtremes(powerData, minValue, minTime, minValue, minTime);
        }
    }

    private static void updateExtremes(PowerData powerData, Float maxValue, String maxTime, Float minValue, String minTime) {
        if (powerData.getMaxValue() < maxValue) {
            powerData.setMaxValue(maxValue);
            powerData.setMaxTime(maxTime);
        }
        if (powerData.getMinValue() > minValue) {
            powerData.setMinValue(minValue);
            powerData.setMinTime(minTime);
        }
    }

    /**
     * 数值辅助类
     */
    private static class PowerData {
        private Float maxValue = 0f;
        private Float minValue = Float.MAX_VALUE;
        private String maxTime = "";
        private String minTime = "";

        public Float getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(Float maxValue) {
            this.maxValue = maxValue;
        }

        public String getMaxTime() {
            return maxTime;
        }

        public void setMaxTime(String maxTime) {
            this.maxTime = maxTime;
        }

        public Float getMinValue() {
            return minValue;
        }

        public void setMinValue(Float minValue) {
            this.minValue = minValue;
        }

        public String getMinTime() {
            return minTime;
        }

        public void setMinTime(String minTime) {
            this.minTime = minTime;
        }
    }

}
