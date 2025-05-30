package cn.iocoder.yudao.module.bus.util;

import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineHourDo;
import cn.iocoder.yudao.framework.common.enums.DataTypeEnums;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusHdaLineAvgResVO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataProcessingUtils {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat hourOnlyFormat = new SimpleDateFormat("HH:mm");


    /**
     * 收集相电压电流数据
     *
     * @param houResVO    数据对象
     * @param resultMap   结果映射
     * @param isSameDay   是否同一天
     * @param dataType    数据类型
     */
    public static void collectPhaseData(BusHdaLineAvgResVO houResVO, Map<String, Object> resultMap, boolean isSameDay, DataTypeEnums dataType) {
        int lineId = houResVO.getLineId();
        String lineKey = "dayList" + lineId;


        Map<String, Object> lineData = (Map<String, Object>) resultMap.computeIfAbsent(lineKey, k -> new HashMap<>());
        ((List<BusHdaLineAvgResVO>) lineData.computeIfAbsent("data", k -> new ArrayList<>())).add(houResVO);
        ((List<Float>) lineData.computeIfAbsent("curDataList", k -> new ArrayList<>())).add(getCurValue(houResVO, dataType));
        ((List<Float>) lineData.computeIfAbsent("volDataList", k -> new ArrayList<>())).add(getVolValue(houResVO, dataType));
        ((List<String>) lineData.computeIfAbsent("curHappenTime", k -> new ArrayList<>())).add(formatCurTime(houResVO, dataType));
        ((List<String>) lineData.computeIfAbsent("volHappenTime", k -> new ArrayList<>())).add(formatVolTime(houResVO, dataType));


        List<String> dateTimes = (List<String>) resultMap.computeIfAbsent("dateTimes", k -> new ArrayList<>());
        dateTimes.add(isSameDay ? hourOnlyFormat.format(houResVO.getCreateTime()) : dateOnlyFormat.format(houResVO.getCreateTime()));

    }

    /**
     * 处理相电流值
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return 相电流值
     */
    public static Float getCurValue(BusHdaLineAvgResVO houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getCurMaxValue().floatValue();
            case AVG:
                return houResVO.getCurAvgValue().floatValue();
            case MIN:
                return houResVO.getCurMinValue().floatValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理相电压值
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return 相电压值
     */
    public static Float getVolValue(BusHdaLineAvgResVO houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getVolMaxValue().floatValue();
            case AVG:
                return houResVO.getVolAvgValue().floatValue();
            case MIN:
                return houResVO.getVolMinValue().floatValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理相电流发生时间
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return 发生时间
     */
    public static String formatCurTime(BusHdaLineAvgResVO houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return sdf.format(houResVO.getCurMaxTime());
            case AVG:
                return "无";
            case MIN:
                return sdf.format(houResVO.getCurMinTime());
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理相电压发生时间
     *
     * @param houResVO    数据对象
     * @param dataType    数据类型
     * @return 发生时间
     */
    public static String formatVolTime(BusHdaLineAvgResVO houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return sdf.format(houResVO.getVolMaxTime());
            case AVG:
                return "无";
            case MIN:
                return sdf.format(houResVO.getVolMinTime());
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    public static void collectLoadRateData(BusLineHourDo houResVO, Map<String, Object> resultMap, boolean isSameDay, DataTypeEnums dataType) {
        int lineId = houResVO.getLineId();
        String lineKey = "dayList" + lineId;

        Map<String, Object> lineData = (Map<String, Object>) resultMap.computeIfAbsent(lineKey, k -> new HashMap<>());
        ((List<BusLineHourDo>) lineData.computeIfAbsent("data", k -> new ArrayList<>())).add(houResVO);
        ((List<Float>) lineData.computeIfAbsent("loadRateDataList", k -> new ArrayList<>())).add(getCurValue(houResVO, dataType));
        ((List<String>) lineData.computeIfAbsent("loadRateHappenTime", k -> new ArrayList<>())).add(formatCurTime(houResVO, dataType));
        List<String> dateTimes = (List<String>) resultMap.computeIfAbsent("dateTimes", k -> new ArrayList<>());
        dateTimes.add(isSameDay ? hourOnlyFormat.format(houResVO.getCreateTime()) : dateOnlyFormat.format(houResVO.getCreateTime()));

    }

    /**
     * 获取相负载率
     * @param houResVO
     * @param dataType
     * @return
     */
    public static Float getCurValue(BusLineHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getLoadRateMaxValue();
            case AVG:
                return houResVO.getLoadRateAvgValue();
            case MIN:
                return houResVO.getLoadRateMinValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 获取相负载率时间
     * @param houResVO
     * @param dataType
     * @return
     */
    public static String formatCurTime(BusLineHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return sdf.format(houResVO.getLoadRateMaxTime());
            case AVG:
                return "无";
            case MIN:
                return sdf.format(houResVO.getLoadRateMinTime());
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }
}
