package cn.iocoder.yudao.module.bus.util;

import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowHourDo;
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

    // 总功率因素分析结果
    public static class FactorTotalResult {
        public Float totalFactorMax;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date totalFactorMaxTime;

        public Float totalFactorMin;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date totalFactorMinTime;

    }

    // 总功率因素分析结果
    public static class FactorAResult {
        public Float aFactorMax;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date aFactorMaxTime;

        public Float aFactorMin;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date aFactorMinTime;

    }

    // 总功率因素分析结果
    public static class FactorBResult {
        public Float bFactorMax;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date bFactorMaxTime;

        public Float bFactorMin;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        public Date bFactorMinTime;

    }

    public static Map<String, Object> analyzeFactorData(List<CabinetPowHourDo> dayList1, Integer dataType) {
        if (dayList1 == null || dayList1.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>();
        FactorTotalResult factorTotalResult = new FactorTotalResult();
        FactorAResult factorAResult = new FactorAResult();
        FactorBResult factorBResult = new FactorBResult();

        if (dataType == 1) {

            factorTotalResult.totalFactorMax = dayList1.get(0).getFactorTotalMaxValue();
            factorTotalResult.totalFactorMaxTime = dayList1.get(0).getFactorTotalMaxTime();
            factorTotalResult.totalFactorMin = dayList1.get(0).getFactorTotalMaxValue();
            factorTotalResult.totalFactorMinTime = dayList1.get(0).getFactorTotalMaxTime();

            factorAResult.aFactorMax = dayList1.get(0).getFactorAMaxValue();
            factorAResult.aFactorMaxTime = dayList1.get(0).getFactorAMaxTime();
            factorAResult.aFactorMin = dayList1.get(0).getFactorAMaxValue();
            factorAResult.aFactorMinTime = dayList1.get(0).getFactorAMaxTime();

            factorBResult.bFactorMax = dayList1.get(0).getFactorBMaxValue();
            factorBResult.bFactorMaxTime = dayList1.get(0).getFactorBMaxTime();
            factorBResult.bFactorMin = dayList1.get(0).getFactorBMaxValue();
            factorBResult.bFactorMinTime = dayList1.get(0).getFactorBMaxTime();


            for (CabinetPowHourDo item : dayList1) {
                // 总功率因素分析
                if (item.getFactorTotalMaxValue() > factorTotalResult.totalFactorMax) {
                    factorTotalResult.totalFactorMax = item.getFactorTotalMaxValue();
                    factorTotalResult.totalFactorMaxTime = item.getFactorTotalMaxTime();
                }
                if (item.getFactorTotalMaxValue() < factorTotalResult.totalFactorMin) {
                    factorTotalResult.totalFactorMin = item.getFactorTotalMaxValue();
                    factorTotalResult.totalFactorMinTime = item.getFactorTotalMaxTime();
                }

                // A路功率因素分析
                if (item.getFactorAMaxValue() > factorAResult.aFactorMax) {
                    factorAResult.aFactorMax = item.getFactorAMaxValue();
                    factorAResult.aFactorMaxTime = item.getFactorAMaxTime();
                }
                if (item.getFactorAMaxValue() < factorAResult.aFactorMin) {
                    factorAResult.aFactorMin = item.getFactorAMaxValue();
                    factorAResult.aFactorMinTime = item.getFactorAMaxTime();
                }

                // B路功率因素分析
                if (item.getFactorBMaxValue() > factorBResult.bFactorMax) {
                    factorBResult.bFactorMax = item.getFactorBMaxValue();
                    factorBResult.bFactorMaxTime = item.getFactorBMaxTime();
                }
                if (item.getFactorBMaxValue() < factorBResult.bFactorMin) {
                    factorBResult.bFactorMin = item.getFactorBMaxValue();
                    factorBResult.bFactorMinTime = item.getFactorBMaxTime();
                }

            }

        } else if (dataType == 0) {

            factorTotalResult.totalFactorMax = dayList1.get(0).getFactorTotalAvgValue();

            factorTotalResult.totalFactorMin = dayList1.get(0).getFactorTotalAvgValue();


            factorAResult.aFactorMax = dayList1.get(0).getFactorAAvgValue();

            factorAResult.aFactorMin = dayList1.get(0).getFactorAAvgValue();


            factorBResult.bFactorMax = dayList1.get(0).getFactorBAvgValue();

            factorBResult.bFactorMin = dayList1.get(0).getFactorBAvgValue();

            for (CabinetPowHourDo item : dayList1) {
                // 总功率因素分析
                if (item.getFactorTotalAvgValue() > factorTotalResult.totalFactorMax) {
                    factorTotalResult.totalFactorMax = item.getFactorTotalAvgValue();

                }
                if (item.getFactorTotalAvgValue() < factorTotalResult.totalFactorMin) {
                    factorTotalResult.totalFactorMin = item.getFactorTotalAvgValue();

                }

                // A路功率因素分析
                if (item.getFactorAAvgValue() > factorAResult.aFactorMax) {
                    factorAResult.aFactorMax = item.getFactorAMaxValue();

                }
                if (item.getFactorAMaxValue() < factorAResult.aFactorMin) {
                    factorAResult.aFactorMin = item.getFactorAMaxValue();

                }

                // B路功率因素分析
                if (item.getFactorBMaxValue() > factorBResult.bFactorMax) {
                    factorBResult.bFactorMax = item.getFactorBMaxValue();

                }
                if (item.getFactorBMaxValue() < factorBResult.bFactorMin) {
                    factorBResult.bFactorMin = item.getFactorBMaxValue();

                }
            }

        }
        else if (dataType == -1) {
            factorTotalResult.totalFactorMax = dayList1.get(0).getFactorTotalMinValue();
            factorTotalResult.totalFactorMaxTime = dayList1.get(0).getFactorTotalMinTime();
            factorTotalResult.totalFactorMin = dayList1.get(0).getFactorTotalMinValue();
            factorTotalResult.totalFactorMinTime = dayList1.get(0).getFactorTotalMinTime();

            factorAResult.aFactorMax = dayList1.get(0).getFactorAMinValue();
            factorAResult.aFactorMaxTime = dayList1.get(0).getFactorAMinTime();
            factorAResult.aFactorMin = dayList1.get(0).getFactorAMinValue();
            factorAResult.aFactorMinTime = dayList1.get(0).getFactorAMinTime();

            factorBResult.bFactorMax = dayList1.get(0).getFactorBMinValue();
            factorBResult.bFactorMaxTime = dayList1.get(0).getFactorBMinTime();
            factorBResult.bFactorMin = dayList1.get(0).getFactorBMinValue();
            factorBResult.bFactorMinTime = dayList1.get(0).getFactorBMinTime();

            for (CabinetPowHourDo item : dayList1) {
                if (item.getFactorTotalMinValue() > factorTotalResult.totalFactorMax) {
                    factorTotalResult.totalFactorMax = item.getFactorTotalMinValue();
                    factorTotalResult.totalFactorMaxTime = item.getFactorTotalMinTime();
                }
                if (item.getFactorTotalMinValue() < factorTotalResult.totalFactorMin) {
                    factorTotalResult.totalFactorMin = item.getFactorTotalMinValue();
                    factorTotalResult.totalFactorMinTime = item.getFactorTotalMinTime();
                }

                // A路功率因素分析
                if (item.getFactorAMinValue() > factorAResult.aFactorMax) {
                    factorAResult.aFactorMax = item.getFactorAMinValue();
                    factorAResult.aFactorMaxTime = item.getFactorAMinTime();
                }
                if (item.getFactorAMinValue() < factorAResult.aFactorMin) {
                    factorAResult.aFactorMin = item.getFactorAMinValue();
                    factorAResult.aFactorMinTime = item.getFactorAMinTime();
                }

                // B路功率因素分析
                if (item.getFactorBMinValue() > factorBResult.bFactorMax) {
                    factorBResult.bFactorMax = item.getFactorBMinValue();
                    factorBResult.bFactorMaxTime = item.getFactorBMinTime();
                }
                if (item.getFactorBMinValue() < factorBResult.bFactorMin) {
                    factorBResult.bFactorMin = item.getFactorBMinValue();
                    factorBResult.bFactorMinTime = item.getFactorBMinTime();
                }
            }

        }
        result.put("totalFactor", factorTotalResult);
        result.put("aFactor", factorAResult);
        result.put("bFactor", factorBResult);
        return result;
    }
}
