package cn.iocoder.yudao.module.cabinet.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.cabinet.service.historydata.CabinetHistoryDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 机柜历史数据")
@RestController
@RequestMapping("/cabinet/history-data")
@Validated
public class CabinetHistoryDataController {

    @Resource
    private CabinetHistoryDataService cabinetHistoryDataService;

    @GetMapping("/page")
    @Operation(summary = "获得机柜历史数据分页")
    public CommonResult<PageResult<Object>> getHistoryDataPage(CabinetHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = cabinetHistoryDataService.getHistoryDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得机柜历史数据详情")
    public CommonResult<PageResult<Object>> getHistoryDataDetails(CabinetHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = cabinetHistoryDataService.getHistoryDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/new-data/{granularity}")
    @Operation(summary = "获得机柜电力分析导航显示的插入的数据量")
    public CommonResult<Map<String, Object>> getNavNewData(@PathVariable("granularity") String granularity) throws IOException {
        Map<String, Object> map = cabinetHistoryDataService.getNavNewData(granularity);
        return success(map);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机柜电力历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportHistoryDataExcel(CabinetHistoryDataPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = cabinetHistoryDataService.getHistoryDataPage(pageReqVO).getList();
        cabinetHistoryDataService.getNewHistoryList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", RealtimePageRespVO.class,
                    BeanUtils.toBean(list, RealtimePageRespVO.class));
        } else {
            ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", HourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, HourAndDayPageRespVO.class));
        }
    }

    @GetMapping("/details-export-excel")
    @Operation(summary = "导出机柜电力分析历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportDetailsHistoryDataExcel(CabinetHistoryDataDetailsReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        PageResult<Object> historyDataDetails = cabinetHistoryDataService.getHistoryDataDetails(pageReqVO);
        List<Object> list=null;
        if (historyDataDetails != null) {
            list = historyDataDetails.getList();
        }else{
            list = new ArrayList<>();
        }
        cabinetHistoryDataService.getNewDetailHistoryList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            if(Objects.equals(pageReqVO.getAbtotal(), "a")){
                List<DetailHistoryDataExcelExportA> bean = BeanUtils.toBean(list, DetailHistoryDataExcelExportA.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", DetailHistoryDataExcelExportA.class,
                        bean);
            } else if (Objects.equals(pageReqVO.getAbtotal(), "b")) {
                List<DetailHistoryDataExcelExportB> bean = BeanUtils.toBean(list, DetailHistoryDataExcelExportB.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", DetailHistoryDataExcelExportB.class,
                        bean);
            }else{
                List<DetailHistoryDataExcelExport> bean = BeanUtils.toBean(list, DetailHistoryDataExcelExport.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", DetailHistoryDataExcelExport.class,
                        bean );
            }

        } else {
            if(Objects.equals(pageReqVO.getAbtotal(), "total")){
                List<HourAndDayDetailHistoryDataExcelExport> bean = BeanUtils.toBean(list, HourAndDayDetailHistoryDataExcelExport.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", HourAndDayDetailHistoryDataExcelExport.class,
                        bean );
            } else if (Objects.equals(pageReqVO.getAbtotal(), "a")) {
                List<HourAndDayDetailHistoryDataExcelExportA> bean = BeanUtils.toBean(list, HourAndDayDetailHistoryDataExcelExportA.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", HourAndDayDetailHistoryDataExcelExportA.class,
                        bean );
            }
            else{
                List<HourAndDayDetailHistoryDataExcelExportB> bean = BeanUtils.toBean(list, HourAndDayDetailHistoryDataExcelExportB.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", HourAndDayDetailHistoryDataExcelExportB.class,
                        bean );
            }

        }
    }

    @PostMapping("/pageEnv")
    @Operation(summary = "获得机柜历史数据分页")
    public CommonResult<PageResult<CabinetEnvResVO>> getHistoryDataPageEnv(@RequestBody CabinetHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<CabinetEnvResVO> pageResult = cabinetHistoryDataService.getHistoryDataPageEnv(pageReqVO,  true);
        return success(pageResult);
    }

    @PostMapping("/env-export")
    @Operation(summary = "获得机柜历史环境数据")
    public void HistoryEnvDataExport(CabinetHistoryDataPageReqVO pageReqVO,HttpServletResponse response) throws IOException {
        PageResult<CabinetEnvResVO> pageResult = cabinetHistoryDataService.getHistoryDataPageEnv(pageReqVO,false);
        if(pageResult!=null){
            if("realtime".equals(pageReqVO.getGranularity())){
                ExcelUtils.write(response, "机柜环境历史数据.xlsx", "数据", CabinetRealtimeEnvExcelVO.class,
                        pageResult.getList().stream().map((map)->{
                            CabinetRealtimeEnvExcelVO ans = new CabinetRealtimeEnvExcelVO();
                            ans.setCreateTime(map.getCreateTime());
                            ans.setAddress(map.getAddress());
                            map.getFront().forEach((iter)->{
                                try {
                                    String methodName1 = "setTemValuefront" + ((Map)iter).get("sensorId");
                                    Method temValue1 = ans.getClass().getDeclaredMethod(methodName1, Double.class);
                                    temValue1.invoke(ans, ((Map)iter).get("temValue"));

                                    String methodName2 = "setHumValuefront" + ((Map)iter).get("sensorId");
                                    Method temValue2 = ans.getClass().getDeclaredMethod(methodName2, Double.class);
                                    temValue2.invoke(ans, ((Map)iter).get("humValue"));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            map.getBlack().forEach((iter)->{
                                try {
                                    String methodName1 = "setTemValueblack" + ((Map)iter).get("sensorId");
                                    Method temValue1 = ans.getClass().getDeclaredMethod(methodName1, Double.class);
                                    temValue1.invoke(ans, ((Map)iter).get("temValue"));

                                    String methodName2 = "setHumValueblack" + ((Map)iter).get("sensorId");
                                    Method temValue2 = ans.getClass().getDeclaredMethod(methodName2, Double.class);
                                    temValue2.invoke(ans, ((Map)iter).get("humValue"));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            return ans;
                        }).collect(Collectors.toList()));
            }else{
                ExcelUtils.write(response,"机柜环境历史数据.xlsx", "数据", CabinetNotRealtimeEnvExcelVO.class,pageResult.getList().stream().map((map)->{
                    CabinetNotRealtimeEnvExcelVO ans = new CabinetNotRealtimeEnvExcelVO();
                    ans.setCreateTime(map.getCreateTime());
                    ans.setAddress(map.getAddress());
                    List front = map.getFront();
                    if(front!=null){
                        front.forEach((iter)->{
                            change((Map)iter,"_front_",ans);
                        });
                    }
                    List black = map.getBlack();
                    if(black!=null){
                        black.forEach((iter)->{
                            change((Map)iter,"_black_",ans);
                        });
                    }
                    return ans;
                }).collect(Collectors.toList()));
            }
        }
    }

    @GetMapping("/new-env-data")
    @Operation(summary = "获得机柜环境数据插入数据量")
    public CommonResult<Map<String, Object>> getEnvNewData() throws IOException {
        Map<String, Object> map = cabinetHistoryDataService.getEnvNewData();
        return success(map);
    }

    @GetMapping("/env-details")
    @Operation(summary = "获得机柜环境数据详情")
    public CommonResult<PageResult<CabinetEnvResVO>> getHistoryEnvDataDetails(CabinetHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<CabinetEnvResVO> pageResult = cabinetHistoryDataService.getHistoryEnvDataDetails(reqVO.getCabinetId(),reqVO.getGranularity(),reqVO.getTimeRange(),reqVO.getNowAddress());
        return success(pageResult);
    }

    @PostMapping("/env-detail-export")
    @Operation(summary = "获得机柜历史环境数据")
    public void HistoryEnvDetailDataExport(CabinetHistoryDataDetailsReqVO reqVO,HttpServletResponse response) throws IOException {
        PageResult<CabinetEnvResVO> pageResult = cabinetHistoryDataService.getHistoryEnvDataDetails(reqVO.getCabinetId(),reqVO.getGranularity(),reqVO.getTimeRange(),reqVO.getNowAddress());
        if(pageResult!=null){
            if("realtime".equals(reqVO.getGranularity())){
                List<CabinetRealtimeEnvDetailExcelVO> list = pageResult.getList().stream().map((map) -> {
                    CabinetRealtimeEnvDetailExcelVO ans = new CabinetRealtimeEnvDetailExcelVO();
                    ans.setCreateTime(map.getCreateTime());
                    ans.setAddress(map.getAddress());
                    if ("front".equals(reqVO.getSensor()[0])) {
                        map.getFront().forEach((iter) -> {
                            if (((Map) iter).get("sensorId").toString().equals(reqVO.getSensor()[1])) {
                                ans.setHumValue((Double) ((Map) iter).get("humValue"));
                                ans.setTemValue((Double) ((Map) iter).get("temValue"));
                            }
                        });
                    } else if ("black".equals(reqVO.getSensor()[0])) {
                        map.getBlack().forEach((iter) -> {
                            if (((Map) iter).get("sensorId").toString().equals(reqVO.getSensor()[1])) {
                                ans.setHumValue((Double) ((Map) iter).get("humValue"));
                                ans.setTemValue((Double) ((Map) iter).get("temValue"));
                            }
                        });
                    }
                    return ans;
                }).collect(Collectors.toList());
                boolean hasData=false;
                for (int i=0;i<list.size();i++){
                    if(list.get(i).getHumValue()!=null||list.get(i).getTemValue()!=null){
                        hasData=true;
                        break;
                    }
                }
                if(!hasData){
                    list.clear();
                }
                ExcelUtils.write(response, "机柜环境详情历史数据.xlsx", "数据", CabinetRealtimeEnvDetailExcelVO.class, list);
            }else{
                List<CabinetNotRealtimeEnvDetailExcelVO> list = pageResult.getList().stream().map((map) -> {
                    CabinetNotRealtimeEnvDetailExcelVO ans = new CabinetNotRealtimeEnvDetailExcelVO();
                    ans.setCreateTime(map.getCreateTime());
                    ans.setAddress(map.getAddress());
                    if ("front".equals(reqVO.getSensor()[0])) {
                        List front = map.getFront();
                        if (front != null) {
                            front.forEach((iter) -> {
                                if (((Map) iter).get("sensorId").toString().equals(reqVO.getSensor()[1])) {
                                    ans.setHum_avg_value((Double) ((Map) iter).get("hum_avg_value"));
                                    ans.setTem_avg_value((Double) ((Map) iter).get("tem_avg_value"));
                                    ans.setHum_max_value((Double) ((Map) iter).get("hum_max_value"));
                                    ans.setTem_max_value((Double) ((Map) iter).get("tem_max_value"));
                                    ans.setHum_max_time((String) ((Map) iter).get("max_time"));
                                    ans.setTem_max_time((String) ((Map) iter).get("max_time"));
                                    ans.setTem_min_value((Double) ((Map) iter).get("tem_min_value"));
                                    ans.setHum_min_value((Double) ((Map) iter).get("hum_min_value"));
                                    ans.setHum_min_time((String) ((Map) iter).get("min_time"));
                                    ans.setTem_min_time((String) ((Map) iter).get("min_time"));
                                }
                            });
                        }
                    } else if ("black".equals(reqVO.getSensor()[0])) {
                        List black = map.getBlack();
                        if (black != null) {
                            black.forEach((iter) -> {
                                if (((Map) iter).get("sensorId").toString().equals(reqVO.getSensor()[1])) {
                                    ans.setHum_avg_value((Double) ((Map) iter).get("hum_avg_value"));
                                    ans.setTem_avg_value((Double) ((Map) iter).get("tem_avg_value"));
                                    ans.setHum_max_value((Double) ((Map) iter).get("hum_max_value"));
                                    ans.setTem_max_value((Double) ((Map) iter).get("tem_max_value"));
                                    ans.setHum_max_time((String) ((Map) iter).get("max_time"));
                                    ans.setTem_max_time((String) ((Map) iter).get("max_time"));
                                    ans.setTem_min_value((Double) ((Map) iter).get("tem_min_value"));
                                    ans.setHum_min_value((Double) ((Map) iter).get("hum_min_value"));
                                    ans.setHum_min_time((String) ((Map) iter).get("min_time"));
                                    ans.setTem_min_time((String) ((Map) iter).get("min_time"));
                                }
                            });
                        }
                    }
                    return ans;
                }).collect(Collectors.toList());
                boolean hasData=false;
                for (int i=0;i<list.size();i++){
                    if(list.get(i).getHum_avg_value()!=null||list.get(i).getTem_avg_value()!=null||list.get(i).getHum_max_value()!=null||list.get(i).getTem_max_value()!=null||list.get(i).getHum_min_value()!=null||list.get(i).getTem_min_value()!=null){
                        hasData=true;
                        break;
                    }
                }
                if(!hasData){
                    list.clear();
                }
                ExcelUtils.write(response,"机柜环境历史数据.xlsx", "数据", CabinetNotRealtimeEnvDetailExcelVO.class,list);
            }
        }
    }
    private void change(Map map, String position, Object object){
        map.keySet().forEach((key)->{
            if("sensorId".equals(key.toString())) return;
            String methodName="set"+((String) key).substring(0, 1).toUpperCase() + ((String) key).substring(1)+position+map.get("sensorId");
            try {
                Method method = object.getClass().getDeclaredMethod(methodName, Double.class);
                method.invoke(object, map.get(key));
            } catch (Exception e) {
                try {
                    methodName="setTem_"+key.toString()+position+map.get("sensorId");
                    Method method1=object.getClass().getDeclaredMethod(methodName, String.class);
                    method1.invoke(object, map.get(key));

                    methodName="setHum_"+key.toString()+position+map.get("sensorId");
                    Method method2=object.getClass().getDeclaredMethod(methodName, String.class);
                    method2.invoke(object, map.get(key));
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }

        });
    }

}