package cn.iocoder.yudao.module.cabinet.utils;

import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowHourDo;
import cn.iocoder.yudao.framework.common.enums.DataTypeEnums;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.text.SimpleDateFormat;
import java.util.*;

public class DataProcessingUtils {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void collectPhaseData(CabinetPowHourDo houResVO, Map<String, Object> resultMap, boolean isSameDay, DataTypeEnums dataType) {

        String lineKey = "dayList";


        Map<String, Object> lineData = (Map<String, Object>) resultMap.computeIfAbsent(lineKey, k -> new HashMap<>());
        ((List<CabinetPowHourDo>) lineData.computeIfAbsent("data", k -> new ArrayList<>())).add(houResVO);
        ((List<Float>) lineData.computeIfAbsent("factorTotalList", k -> new ArrayList<>())).add(getFactorTotal(houResVO, dataType));
        ((List<Float>) lineData.computeIfAbsent("factorAList", k -> new ArrayList<>())).add(getFactorA(houResVO, dataType));
        ((List<Float>) lineData.computeIfAbsent("factorBList", k -> new ArrayList<>())).add(getFactorB(houResVO, dataType));

        ((List<String>) lineData.computeIfAbsent("totalHappenTime", k -> new ArrayList<>())).add(formatFactorTotalTime(houResVO, dataType));
        ((List<String>) lineData.computeIfAbsent("AHappenTime", k -> new ArrayList<>())).add(formatFactorATime(houResVO, dataType));
        ((List<String>) lineData.computeIfAbsent("BHappenTime", k -> new ArrayList<>())).add(formatFactorBTime(houResVO, dataType));

        List<String> dateTimes = (List<String>) resultMap.computeIfAbsent("dateTimes", k -> new ArrayList<>());
        if (isSameDay){
            dateTimes.add((houResVO.getCreateTime().split(" ")[1]));
        }else{
            dateTimes.add((houResVO.getCreateTime().split(" ")[0]));
        }



    }


    /**
     * 处理总功率因素
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return 总功率因素
     */
    public static Float getFactorTotal(CabinetPowHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getFactorTotalMaxValue();
            case AVG:
                return houResVO.getFactorTotalAvgValue();
            case MIN:
                return houResVO.getFactorTotalMinValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理A路功率因素
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return A路功率因素
     */
    public static Float getFactorB(CabinetPowHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getFactorAMaxValue();
            case AVG:
                return houResVO.getFactorAAvgValue();
            case MIN:
                return houResVO.getFactorAMinValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理B路功率因素
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return B路功率因素
     */
    public static Float getFactorA(CabinetPowHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getFactorBMaxValue();
            case AVG:
                return houResVO.getFactorBAvgValue();
            case MIN:
                return houResVO.getFactorBMinValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理总功率因素发生时间
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return 发生时间
     */
    public static String formatFactorTotalTime(CabinetPowHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getFactorTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss");
            case AVG:
                return "无";
            case MIN:
                return houResVO.getFactorTotalMinTime().toString("yyyy-MM-dd HH:mm:ss");
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理A路功率因素发生时间
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return 发生时间
     */
    public static String formatFactorATime(CabinetPowHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getFactorAMaxTime().toString("yyyy-MM-dd HH:mm:ss");
            case AVG:
                return "无";
            case MIN:
                return houResVO.getFactorAMinTime().toString("yyyy-MM-dd HH:mm:ss");
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理B路功率因素发生时间
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return 发生时间
     */
    public static String formatFactorBTime(CabinetPowHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getFactorBMaxTime().toString("yyyy-MM-dd HH:mm:ss");
            case AVG:
                return "无";
            case MIN:
                return houResVO.getFactorBMinTime().toString("yyyy-MM-dd HH:mm:ss");
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
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
