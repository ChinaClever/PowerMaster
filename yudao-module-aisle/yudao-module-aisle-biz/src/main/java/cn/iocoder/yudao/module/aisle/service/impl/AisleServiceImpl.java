package cn.iocoder.yudao.module.aisle.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleBarDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleBoxDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleDetailDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.oulet.BoxEqOutletDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBox;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.enums.DisableEnums;
import cn.iocoder.yudao.framework.common.enums.PduBoxEnums;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.aisle.dto.*;
import cn.iocoder.yudao.module.aisle.service.AisleService;
import cn.iocoder.yudao.module.aisle.vo.AisleBusSaveVo;
import cn.iocoder.yudao.module.cabinet.api.CabinetApi;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列操作
 * @date 2024/6/21 14:19
 */
@Slf4j
@Service
public class AisleServiceImpl implements AisleService {

    @Resource
    AisleIndexMapper aisleIndexMapper;

    @Resource
    AisleBarMapper aisleBarMapper;

    @Resource
    AisleBoxMapper aisleBoxMapper;

    @Resource
    RedisTemplate redisTemplate;
    @Resource
    RoomIndexMapper roomIndexMapper;
    @Resource
    CabinetIndexMapper cabinetIndexMapper;
    @Resource
    BusIndexDoMapper busIndexDoMapper;
    @Resource
    BoxIndexMapper boxIndexMapper;
    @Resource
    CabinetCfgDoMapper cfgDoMapper;

    @Autowired
    RackIndexDoMapper rackIndexDoMapper;

    @Value("${aisle-refresh-url}")
    public String adder;

    @Resource
    CabinetApi cabinetApi;

    @Autowired
    private RestHighLevelClient client;

    public static final String HOUR_FORMAT = "yyyy-MM-dd";

    /**
     * 柜列保存
     * @param aisleSaveVo 保存参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer aisleSave(AisleSaveVo aisleSaveVo) {

        try {
            //柜列信息
            AisleIndex index = new AisleIndex();
            index.setName(aisleSaveVo.getAisleName());
            index.setLength(aisleSaveVo.getLength());
            index.setRoomId(aisleSaveVo.getRoomId());
            index.setType(aisleSaveVo.getType());
            index.setPduBar(aisleSaveVo.getPduBar());
            index.setEleAlarmDay(aisleSaveVo.getEleAlarmDay());
            index.setEleAlarmMonth(aisleSaveVo.getEleAlarmMonth());
            index.setEleLimitDay(aisleSaveVo.getEleLimitDay());
            index.setEleLimitMonth(aisleSaveVo.getEleLimitMonth());
            index.setDirection(aisleSaveVo.getDirection());
            index.setXCoordinate(aisleSaveVo.getXCoordinate());
            index.setYCoordinate(aisleSaveVo.getYCoordinate());

            Integer roomId = aisleSaveVo.getRoomId();
            RoomIndex roomIndex = roomIndexMapper.selectById(roomId);
            if (Objects.nonNull(roomIndex)){
                if (StringUtils.isNotEmpty(aisleSaveVo.getDirection())
                        && "x".equals(aisleSaveVo.getDirection())){
                    //横向
                    if (aisleSaveVo.getXCoordinate() + aisleSaveVo.getLength()> roomIndex.getXLength() +1){
                        throw new RuntimeException("柜列长度超出");
                    }
                }
                if ( StringUtils.isNotEmpty(aisleSaveVo.getDirection())
                        && "y".equals(aisleSaveVo.getDirection())){
                    //纵向
                    if (aisleSaveVo.getYCoordinate() + aisleSaveVo.getLength()>roomIndex.getYLength() + 1){
                        throw new RuntimeException("柜列长度超出");
                    }
                }
            }

            if (Objects.nonNull(aisleSaveVo.getId())){
                //编辑
                AisleIndex aisleIndex = aisleIndexMapper.selectOne(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getId,aisleSaveVo.getId()));
                if (Objects.nonNull(aisleIndex)){
                    index.setId(aisleSaveVo.getId());
                    aisleIndexMapper.updateById(index);
                }

            }else {
                //新增
                aisleIndexMapper.insert(index);
            }


            String busIpA;

            String busNameA;

            String busIpB;

            String busNameB;

            Integer barIdA;
            Integer barIdB;



            //母线信息
            List<AisleBar> aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                    .eq(AisleBar::getAisleId,index.getId()));
            List<AisleBarDTO> barVos = new ArrayList<>();
            if (Objects.nonNull(aisleSaveVo.getBarA())){
                barVos.add(aisleSaveVo.getBarA());
                busIpA = aisleSaveVo.getBarA().getDevIp();
                busNameA = aisleSaveVo.getBarA().getBusName();
                barIdA = aisleSaveVo.getBarA().getBarId();
            } else {
                busNameA = "";
                busIpA = "";
                barIdA = null;
            }
            if (Objects.nonNull(aisleSaveVo.getBarB())){
                barVos.add(aisleSaveVo.getBarB());
                busIpB = aisleSaveVo.getBarB().getDevIp();
                busNameB = aisleSaveVo.getBarB().getBusName();
                barIdB = aisleSaveVo.getBarB().getBarId();
            } else {
                busNameB = "";
                busIpB = "";
                barIdB = null;
            }

            if (!CollectionUtils.isEmpty(barVos)){
                if (!CollectionUtils.isEmpty(aisleBars)){
                    List<Integer> ids = aisleBars.stream().map(AisleBar::getId).collect(Collectors.toList());
                    ids.forEach(this::deleteBus);

                }
                AisleBusSaveVo busSaveVo = new AisleBusSaveVo();
                busSaveVo.setAisleId(index.getId());
                busSaveVo.setBarVos(barVos);
                aisleBusSave(busSaveVo);
            }else {
                //删除绑定关系
                if (!CollectionUtils.isEmpty(aisleBars)){
                    List<Integer> ids = aisleBars.stream().map(AisleBar::getId).collect(Collectors.toList());
                    ids.forEach(this::deleteBus);
                }
            }

            //已有机柜
            List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                    .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                    .eq(CabinetIndex::getRoomId,aisleSaveVo.getRoomId())
                    .eq(CabinetIndex::getAisleId,index.getId()));
            //机柜信息
            if (!CollectionUtils.isEmpty(aisleSaveVo.getCabinetList())){
                //更改后的机柜列表
                List<Integer> ids =  aisleSaveVo.getCabinetList().stream().map(CabinetVo::getId).filter(id -> id >0).collect(Collectors.toList());



                if (!CollectionUtils.isEmpty(ids)){
                    //需要删除的机柜
                    List<CabinetIndex> indices = cabinetIndexMapper.selectList(new LambdaUpdateWrapper<CabinetIndex>()
                            .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                            .eq(CabinetIndex::getRoomId,aisleSaveVo.getRoomId())
                            .eq(CabinetIndex::getAisleId,index.getId()).notIn(CabinetIndex::getId,ids));

                    //删除不在更改后机柜中的机架
                    if (!CollectionUtils.isEmpty(indices)){
                        rackIndexDoMapper.delete(new LambdaQueryWrapper<RackIndex>()
                                .in(RackIndex::getCabinetId,indices.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                    }

                    //需要删除的机柜
                    if (!CollectionUtils.isEmpty(indices)){
                        indices.forEach(cabinetIndex -> {
                            try {
                                cabinetApi.delCabinet(cabinetIndex.getId());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

                    }


                }else {

                    //清空该柜列机柜及机架
                    if (!CollectionUtils.isEmpty(cabinetIndexList)){
                        rackIndexDoMapper.delete(new LambdaQueryWrapper<RackIndex>()
                                .in(RackIndex::getCabinetId,cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                    }
                    //需要删除的机柜
                    if (!CollectionUtils.isEmpty(cabinetIndexList)){
                        cabinetIndexList.forEach(cabinetIndex -> {
                            try {
                                cabinetApi.delCabinet(cabinetIndex.getId());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

                    }

                }

                //新增/保存
                aisleSaveVo.getCabinetList().forEach(cabinetVo -> {
                    cabinetVo.setRoomId(aisleSaveVo.getRoomId());
                    cabinetVo.setAisleId(index.getId());
                    if (aisleSaveVo.getPduBar().equals(PduBoxEnums.BUS.getValue())){
                        cabinetVo.setBusIpA(busIpA);
                        cabinetVo.setBarIdA(barIdA);
                        cabinetVo.setBusIpB(busIpB);
                        cabinetVo.setBarIdB(barIdB);

                        if (Objects.isNull(cabinetVo.getAddrA()) && Objects.nonNull(cabinetVo.getBoxIndexA())){
                            AisleBar bar = aisleBarMapper.selectOne(new LambdaQueryWrapper<AisleBar>()
                                    .eq(AisleBar::getAisleId,index.getId())
                                    .eq(AisleBar::getBarId,barIdA)
                                    .eq(AisleBar::getDevIp,busIpA));
                            if (Objects.nonNull(bar)){
                                AisleBox box = aisleBoxMapper.selectOne(new LambdaQueryWrapper<AisleBox>()
                                        .eq(AisleBox::getAisleBarId,bar.getId())
                                        .eq(AisleBox::getType,0)
                                        .eq(AisleBox::getBoxIndex,cabinetVo.getBoxIndexA()));
                                cabinetVo.setAddrA(Objects.nonNull(box)?box.getCasAddr():null);

                            }

                        }
                        if (Objects.isNull(cabinetVo.getAddrB()) && Objects.nonNull(cabinetVo.getBoxIndexB())){
                            AisleBar bar = aisleBarMapper.selectOne(new LambdaQueryWrapper<AisleBar>()
                                    .eq(AisleBar::getAisleId,index.getId())
                                    .eq(AisleBar::getBarId,barIdB)
                                    .eq(AisleBar::getDevIp,busIpB));
                            if (Objects.nonNull(bar)){
                                AisleBox box = aisleBoxMapper.selectOne(new LambdaQueryWrapper<AisleBox>()
                                        .eq(AisleBox::getAisleBarId,bar.getId())
                                        .eq(AisleBox::getType,0)
                                        .eq(AisleBox::getBoxIndex,cabinetVo.getBoxIndexB()));
                                cabinetVo.setAddrB(Objects.nonNull(box)?box.getCasAddr():null);

                            }

                        }
                    }
                    cabinetVo.setPduBox(aisleSaveVo.getPduBar());

                    if (Objects.nonNull(cabinetVo.getIndex())
                            && StringUtils.isNotEmpty(aisleSaveVo.getDirection())
                            && "x".equals(aisleSaveVo.getDirection())){
                        //横向
                        cabinetVo.setXCoordinate(aisleSaveVo.getXCoordinate() + cabinetVo.getIndex() - 1);
                        cabinetVo.setYCoordinate(aisleSaveVo.getYCoordinate());
                    }
                    if (Objects.nonNull(cabinetVo.getIndex())
                            && StringUtils.isNotEmpty(aisleSaveVo.getDirection())
                            && "y".equals(aisleSaveVo.getDirection())){
                        //纵向
                        cabinetVo.setYCoordinate(aisleSaveVo.getYCoordinate() + cabinetVo.getIndex() - 1);
                        cabinetVo.setXCoordinate(aisleSaveVo.getXCoordinate());
                    }
                    try {
                        cabinetApi.saveCabinet(cabinetVo);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }else {
                //清空该柜列机柜及机架
                if (!CollectionUtils.isEmpty(cabinetIndexList)){
                    rackIndexDoMapper.delete(new LambdaQueryWrapper<RackIndex>()
                            .in(RackIndex::getCabinetId,cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                }
                //需要删除的机柜
                if (!CollectionUtils.isEmpty(cabinetIndexList)){
                    cabinetIndexList.forEach(cabinetIndex -> {
                        try {
                            cabinetApi.delCabinet(cabinetIndex.getId());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                }
            }

            return index.getId();
        }finally {
            //刷新柜列计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }

    }

    @Override
    public void aisleBusSave(AisleBusSaveVo busSaveVo) {

        try {
            Integer aisleId = busSaveVo.getAisleId();

            if (!CollectionUtils.isEmpty(busSaveVo.getBarVos())){
                //绑定始端箱
                List<AisleBarDTO> barVos = busSaveVo.getBarVos();
                //先删除
                aisleBarMapper.delete(new LambdaQueryWrapper<AisleBar>()
                        .eq(AisleBar::getAisleId,aisleId));
                aisleBoxMapper.delete(new LambdaQueryWrapper<AisleBox>()
                        .eq(AisleBox::getAisleId,aisleId));
                barVos.forEach(barVo -> {
                    AisleBar  bar = BeanUtils.toBean(barVo,AisleBar.class);
                    bar.setAisleId(aisleId);
                    bar.setBarKey(barVo.getDevIp() + SPLIT_KEY + barVo.getBarId()+ SPLIT_KEY + barVo.getCasAddr());

                    aisleBarMapper.insert(bar);

                    List<AisleBoxDTO> boxList = barVo.getBoxList();
                    if (!CollectionUtils.isEmpty(boxList)){
                        boxList.forEach(boxDTO ->{
                            AisleBox box = BeanUtils.toBean(boxDTO,AisleBox.class);
                            box.setAisleId(aisleId);
                            box.setAisleBarId(bar.getId());
                            box.setBarId(barVo.getBarId());
                            box.setBarKey(barVo.getDevIp() + SPLIT_KEY + barVo.getBarId()+ SPLIT_KEY + boxDTO.getCasAddr());
                            aisleBoxMapper.insert(box);
                        });
                    }

                });

            }
        }finally {
            //刷新柜列计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDeleteBox(List<Integer> boxIds) {
        try {
            if (!CollectionUtils.isEmpty(boxIds)){
                aisleBoxMapper.deleteBatchIds(boxIds);
            }
        }finally {
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }


    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBus(Integer barId) {
        try {
            //删除母线需要先删除插接箱
            List<AisleBox>  boxList = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
                    .eq(AisleBox::getAisleBarId,barId));
            if (!CollectionUtils.isEmpty(boxList)){
                aisleBoxMapper.delete(new LambdaQueryWrapper<AisleBox>()
                        .eq(AisleBox::getAisleBarId,barId));
            }
            aisleBarMapper.deleteById(barId);
        }finally {
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAisle(Integer aisleId) {
        try {
            //删除柜列
            AisleIndex aisleIndex = aisleIndexMapper.selectById(aisleId);
            if (Objects.nonNull(aisleIndex)){
                List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                        .eq(CabinetIndex::getAisleId,aisleIndex.getId()));
                if (!CollectionUtils.isEmpty(cabinetIndexList)){
                    throw new RuntimeException("存在未删除机柜，不可删除");
                }
                //逻辑删除
                if (aisleIndex.getIsDelete().equals(DelEnums.NO_DEL.getStatus())){
                    aisleIndexMapper.update(new LambdaUpdateWrapper<AisleIndex>()
                            .eq(AisleIndex::getId, aisleId)
                            .set(AisleIndex::getIsDelete, DelEnums.DELETE.getStatus()));

                    //1.删除绑定关系
                    aisleBoxMapper.delete(new LambdaQueryWrapper<AisleBox>()
                            .eq(AisleBox::getAisleId,aisleId));
                    aisleBarMapper.delete(new LambdaQueryWrapper<AisleBar>()
                            .eq(AisleBar::getAisleId,aisleId));

                }else {
                    //物理删除
                    //1.删除绑定关系
                    aisleBoxMapper.delete(new LambdaQueryWrapper<AisleBox>()
                            .eq(AisleBox::getAisleId,aisleId));
                    aisleBarMapper.delete(new LambdaQueryWrapper<AisleBar>()
                            .eq(AisleBar::getAisleId,aisleId));
                    //2.删除柜列
                    aisleIndexMapper.deleteById(aisleId);
                }
            }
            //删除key
            String key = REDIS_KEY_AISLE + aisleId;

            boolean flag = redisTemplate.delete(key);
            log.info("key: " + key + " flag : " + flag);
        }finally {
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }

    }

    @Override
    public AisleDetailDTO getAisleDetail(Integer aisleId) throws IOException {
        AisleDetailDTO detailDTO = new AisleDetailDTO();

        AisleIndex aisleIndex = aisleIndexMapper.selectById(aisleId);

        if (Objects.nonNull(aisleIndex)){
            detailDTO.setAisleName(aisleIndex.getName());
            detailDTO.setId(aisleId);
            detailDTO.setLength(aisleIndex.getLength());
            detailDTO.setType(aisleIndex.getType());
            detailDTO.setPduBar(aisleIndex.getPduBar());
            detailDTO.setEleAlarmDay(aisleIndex.getEleAlarmDay());
            detailDTO.setEleAlarmMonth(aisleIndex.getEleAlarmMonth());
            detailDTO.setEleLimitDay(aisleIndex.getEleLimitDay());
            detailDTO.setEleLimitMonth(aisleIndex.getEleLimitMonth());
            detailDTO.setDirection(aisleIndex.getDirection());
            detailDTO.setXCoordinate(aisleIndex.getXCoordinate());
            detailDTO.setYCoordinate(aisleIndex.getYCoordinate());

            Integer roomId = aisleIndex.getRoomId();
            RoomIndex roomIndex = roomIndexMapper.selectById(roomId);
            if (Objects.nonNull(roomIndex)){
                detailDTO.setRoomName(roomIndex.getName());
                detailDTO.setRoomId(roomId);
            }

        }

        //母线
        List<AisleBar>  aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                .eq(AisleBar::getAisleId,aisleId));


        if (!CollectionUtils.isEmpty(aisleBars)){
            //key
            List<String> keys = aisleBars.stream().map(AisleBar::getBarKey).collect(Collectors.toList());
            List<BusIndex>  busIndexList = busIndexDoMapper.selectList(new LambdaQueryWrapper<BusIndex>()
                    .in(BusIndex::getDevKey,keys));
            Map<String,Integer>  idMap;
            Map<Integer,Double> yesterdayMap = new HashMap<>();

            //获取昨日统计用电
            if (!CollectionUtils.isEmpty(busIndexList)){
                List<Integer> ids = busIndexList.stream().map(BusIndex::getId).distinct().collect(Collectors.toList());
                idMap = busIndexList.stream().collect(Collectors.toMap(BusIndex::getDevKey,BusIndex::getId));

                String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
                String endTime =DateUtil.formatDateTime(DateTime.now());
                List<String>  yesterdayList = getData(startTime,endTime, ids,BUS_EQ_TOTAL_DAY,BUS_ID);

                if (!CollectionUtils.isEmpty(yesterdayList)){
                    yesterdayList.forEach(str -> {
                        BusEqTotalDayDo dayDo = JsonUtils.parseObject(str, BusEqTotalDayDo.class);
                        yesterdayMap.put(dayDo.getBusId(),dayDo.getEq());
                    });
                }
            } else {
                idMap = new HashMap<>();
            }


            Map<String,Integer>  boxIdMap;
            Map<Integer,Map<Integer,Double>> boxYesterdayMap = new HashMap<>();

            List<AisleBox> aisleBoxList = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
                    .eq(AisleBox::getAisleId,aisleId));
            if (!CollectionUtils.isEmpty(aisleBoxList)){
                //获取id
                List<String> boxKeys = aisleBoxList.stream().map(AisleBox::getBarKey).collect(Collectors.toList());
                List<BoxIndex>  boxIndexList = boxIndexMapper.selectList(new LambdaQueryWrapper<BoxIndex>()
                        .in(BoxIndex::getDevKey,boxKeys));
                //获取昨日统计用电
                if (!CollectionUtils.isEmpty(boxIndexList)){
                    List<Integer> ids = boxIndexList.stream().map(BoxIndex::getId).distinct().collect(Collectors.toList());
                    boxIdMap = boxIndexList.stream().collect(Collectors.toMap(BoxIndex::getDevKey,BoxIndex::getId));

                    String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
                    String endTime =DateUtil.formatDateTime(DateTime.now());
                    List<String>  yesterdayList = null;
                    try {
                        yesterdayList = getData(startTime,endTime, ids,BOX_EQ_OUTLET_DAY,BOX_ID);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (!CollectionUtils.isEmpty(yesterdayList)){

                        yesterdayList.forEach(str -> {
                            BoxEqOutletDayDo dayDo = JsonUtils.parseObject(str, BoxEqOutletDayDo.class);

                            Map<Integer,Double> outletMap = boxYesterdayMap.get(dayDo.getBoxId());
                            if (Objects.nonNull(outletMap)){
                                outletMap.put(dayDo.getOutletId(),dayDo.getEq());
                            }else {
                                outletMap = new HashMap<>();
                                outletMap.put(dayDo.getOutletId(),dayDo.getEq());
                            }
                            boxYesterdayMap.put(dayDo.getBoxId(),outletMap);
                        });
                    }
                } else {
                    boxIdMap = new HashMap<>();
                }
            } else {
                boxIdMap = new HashMap<>();
            }


            aisleBars.forEach(aisleBar -> {
                AisleBarDTO barVo = BeanUtils.toBean(aisleBar, AisleBarDTO.class);
                List<AisleBox> boxList = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
                        .eq(AisleBox::getAisleBarId,aisleBar.getId()));
                List<AisleBoxDTO> boxDTOList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(boxList)){

                    boxList.forEach(box -> {
                        AisleBoxDTO boxDTO = BeanUtils.toBean(box, AisleBoxDTO.class);
                        //获取输出位用电量
                        int boxId = boxIdMap.getOrDefault(box.getBarKey(),0);
                        Map<Integer,Double> outletEq = boxYesterdayMap.getOrDefault(boxId,new HashMap<>());

                        if (!CollectionUtils.isEmpty(outletEq.values())){
                            Double[] eqList = new Double[outletEq.values().size()];
                            outletEq.keySet().forEach(id -> eqList[id-1] = outletEq.get(id));
                            boxDTO.setYesterdayEq(eqList);
                        }
                        boxDTOList.add(boxDTO);
                    });
                }
                //获取昨日用电量
                String key = aisleBar.getBarKey();
                int busId = idMap.getOrDefault(key,0);
                barVo.setYesterdayEq(yesterdayMap.getOrDefault(busId, 0.0));

                barVo.setBoxList(boxDTOList.stream().sorted(Comparator.comparing(AisleBoxDTO::getBoxIndex)).collect(Collectors.toList()));
                if ("A".equals(aisleBar.getPath())){
                    detailDTO.setBarA(barVo);
                }
                if ("B".equals(aisleBar.getPath())){
                    detailDTO.setBarB(barVo);
                }


            });
        }

        //机柜
        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                .eq(CabinetIndex::getAisleId,aisleId)
                .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus()));
        List<CabinetDTO> aisleCabinetDTOList = new ArrayList<>();
       if (!CollectionUtils.isEmpty(cabinetIndexList)){

           List<Integer> ids = cabinetIndexList.stream().map(CabinetIndex::getId).distinct().collect(Collectors.toList());

           String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
           String endTime =DateUtil.formatDateTime(DateTime.now());
           List<String>  yesterdayList = getData(startTime,endTime, ids,CABINET_EQ_TOTAL_DAY,CABINET_ID);
           Map<Integer,Double> yesterdayMap = new HashMap<>();
           if (!CollectionUtils.isEmpty(yesterdayList)){
               yesterdayList.forEach(str -> {
                   CabinetEqTotalDayDo dayDo = JsonUtils.parseObject(str, CabinetEqTotalDayDo.class);
                   yesterdayMap.put(dayDo.getCabinetId(),dayDo.getEqValue());
               });
           }
           cabinetIndexList.forEach(cabinetIndex ->{
               CabinetDTO cabinetDTO = cabinetApi.getDetail(cabinetIndex.getId());
               if ("x".equals(aisleIndex.getDirection())){
                   //横向
                   cabinetDTO.setIndex(cabinetDTO.getXCoordinate() - aisleIndex.getXCoordinate() + 1);
               }
               if ("y".equals(aisleIndex.getDirection())){
                   //纵向
                   cabinetDTO.setIndex(cabinetDTO.getYCoordinate() - aisleIndex.getYCoordinate() + 1);
               }
               cabinetDTO.setYesterdayEq(yesterdayMap.getOrDefault(cabinetIndex.getId(), 0.0));
               aisleCabinetDTOList.add(cabinetDTO);
           });
           detailDTO.setCabinetList(aisleCabinetDTOList.stream().sorted(Comparator.comparing(CabinetDTO::getIndex)).collect(Collectors.toList()));
       }

       return detailDTO;
    }

    @Override
    public List<AisleListDTO> getAisleList() {
        List<AisleListDTO> aisleListDTOList = new ArrayList<>();

        List<RoomIndex> roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                .eq(RoomIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));

        List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
        Map<Integer,List<AisleIndex>>  map = new HashMap<>();
        if (!CollectionUtils.isEmpty(aisleIndexList)){
            map.putAll(aisleIndexList.stream().collect(Collectors.groupingBy(AisleIndex::getRoomId)));
        }
        if (!CollectionUtils.isEmpty(roomIndexList)){
            roomIndexList.forEach(roomIndex -> {
                if (Objects.nonNull(map.get(roomIndex.getId()))){
                    AisleListDTO aisleListDTO = new AisleListDTO();
                    aisleListDTO.setRoomId(roomIndex.getId());
                    aisleListDTO.setRoomName(roomIndex.getName());
                    aisleListDTO.setYLength(roomIndex.getYLength());
                    aisleListDTO.setXLength(roomIndex.getXLength());
                    aisleListDTO.setPowerCapacity(roomIndex.getPowerCapacity());
                    aisleListDTO.setAisleList(map.get(roomIndex.getId()));
                    aisleListDTOList.add(aisleListDTO);
                }
            });

        }
        return aisleListDTOList;
    }

    @Override
    public AisleDataDTO getAisleDataDetail(int aisleId) {
        AisleDataDTO detailDTO = new AisleDataDTO();
        ValueOperations ops = redisTemplate.opsForValue();

        detailDTO.setId(aisleId);

        //母线
        List<AisleBar>  aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                .eq(AisleBar::getAisleId,aisleId));
        List<AisleBox> aisleBoxList = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
                .eq(AisleBox::getAisleId,aisleId));
        //始端箱
        if (!CollectionUtils.isEmpty(aisleBars)){
            aisleBars.forEach(aisleBar -> {
                BusDetailDataDTO busDTO = new BusDetailDataDTO();

                String key = aisleBar.getBarKey();
                String busKey =  REDIS_KEY_BUS + key;
                Object busObject = ops.get(busKey);
                if (Objects.nonNull(busObject)) {
                    JSONObject data = JSON.parseObject(JSON.toJSONString(busObject));
                    JSONObject busData = data.containsKey(BUS_DATA) ? data.getJSONObject(BUS_DATA) : new JSONObject();
                    JSONObject envData = data.containsKey(ENV_ITEM_LIST)?data.getJSONObject(ENV_ITEM_LIST):new JSONObject();
                    //温度数据
                    if (envData.containsKey(TEM_VALUE)){
                        busDTO.setTemData(envData.getObject(TEM_VALUE,float[].class));
                    }
                    //相数据
                    if (busData.containsKey(LINE_ITEM_LIST)){
                        JSONObject lineData = busData.getJSONObject(LINE_ITEM_LIST);
                        //负载率
                        if (lineData.containsKey(LOAD_RATE)){
                            busDTO.setLineLoadRate(lineData.getObject(LOAD_RATE,float[].class));
                        }
                        //电流
                        if (lineData.containsKey(CUR_VALUE)){
                            busDTO.setLineCur(lineData.getObject(CUR_VALUE,float[].class));
                        }
                        //电压
                        if (lineData.containsKey(VOL_VALUE)){
                            busDTO.setLineVol(lineData.getObject(VOL_VALUE,float[].class));
                        }
                        //有功功率
                        if (lineData.containsKey(POW_ACTIVE)){
                            busDTO.setPowActive(lineData.getObject(POW_ACTIVE,float[].class));
                        }
                        //视在功率
                        if (lineData.containsKey(POW_APPARENT)){
                            busDTO.setPowApparent(lineData.getObject(POW_APPARENT,float[].class));
                        }
                        //无功功率
                        if (lineData.containsKey(POW_REACTIVE)){
                            busDTO.setPowReactive(lineData.getObject(POW_REACTIVE,float[].class));
                        }
                        //功率因素
                        if (lineData.containsKey(POWER_FACTOR)){
                            busDTO.setPowerFactor(lineData.getObject(POWER_FACTOR,float[].class));
                        }

                    }

                }
                busDTO.setDevKey(key);

                //插接箱
                List<BoxDetailDataDTO> boxList =  new ArrayList<>();

                if (!CollectionUtils.isEmpty(aisleBoxList)){
                    
                    aisleBoxList.forEach(box -> {
                        if (box.getAisleBarId().equals(aisleBar.getId())){
                            BoxDetailDataDTO boxDto = new BoxDetailDataDTO();

                            boxDto.setId(box.getId());
                            String devKey = box.getBarKey();
                            String boxKey =  REDIS_KEY_BOX + devKey;
                            Object boxObject = ops.get(boxKey);
                            boxDto.setDevKey(devKey);
                            boxDto.setBoxIndex(box.getBoxIndex());
                            if (Objects.nonNull(boxObject)) {
                                JSONObject data = JSON.parseObject(JSON.toJSONString(boxObject));
                                JSONObject boxData = data.containsKey(BOX_DATA) ? data.getJSONObject(BOX_DATA) : new JSONObject();
                                JSONObject envData = data.containsKey(ENV_ITEM_LIST)?data.getJSONObject(ENV_ITEM_LIST):new JSONObject();
                                //温度数据
                                if (envData.containsKey(TEM_VALUE)){
                                    boxDto.setTemData(envData.getObject(TEM_VALUE,float[].class));
                                }
                                //相数据
                                if (boxData.containsKey(LINE_ITEM_LIST)){
                                    JSONObject lineData = boxData.getJSONObject(LINE_ITEM_LIST);
                                    //负载率
                                    if (lineData.containsKey(LOAD_RATE)){
                                        boxDto.setLineLoadRate(lineData.getObject(LOAD_RATE,float[].class));
                                    }
                                    //电流
                                    if (lineData.containsKey(CUR_VALUE)){
                                        boxDto.setLineCur(lineData.getObject(CUR_VALUE,float[].class));
                                    }
                                    //电压
                                    if (lineData.containsKey(VOL_VALUE)){
                                        boxDto.setLineVol(lineData.getObject(VOL_VALUE,float[].class));
                                    }

                                }
                                //输出位数据
                                if (boxData.containsKey(OUTLET_ITEM_LIST)){
                                    JSONObject outletData = boxData.getJSONObject(OUTLET_ITEM_LIST);
                                    //有功功率
                                    if (outletData.containsKey(POW_ACTIVE)){
                                        boxDto.setPowActive(outletData.getObject(POW_ACTIVE,float[].class));
                                    }
                                    //视在功率
                                    if (outletData.containsKey(POW_APPARENT)){
                                        boxDto.setPowApparent(outletData.getObject(POW_APPARENT,float[].class));
                                    }
                                    //无功功率
                                    if (outletData.containsKey(POW_REACTIVE)){
                                        boxDto.setPowReactive(outletData.getObject(POW_REACTIVE,float[].class));
                                    }
                                    //功率因素
                                    if (outletData.containsKey(POWER_FACTOR)){
                                        boxDto.setPowerFactor(outletData.getObject(POWER_FACTOR,float[].class));
                                    }

                                }
                            }
                            boxList.add(boxDto);
                        }
                    });
                }
                busDTO.setBoxList(boxList.stream().sorted(Comparator.comparing(BoxDetailDataDTO::getBoxIndex)).collect(Collectors.toList()));
                if ("A".equals(aisleBar.getPath())){
                    detailDTO.setBarA(busDTO);
                }
                if ("B".equals(aisleBar.getPath())){
                    detailDTO.setBarB(busDTO);
                }
            });
        }

        //机柜
        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                .eq(CabinetIndex::getAisleId,aisleId)
                .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus()));

        List<CabinetDetailDataDTO> cabList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cabinetIndexList)){
            cabinetIndexList.forEach(cabinetIndex ->{
                CabinetDetailDataDTO cabDto = new CabinetDetailDataDTO();
                cabDto.setId(cabinetIndex.getId());
                String cabKey =  REDIS_KEY_CABINET + cabinetIndex.getRoomId() + SPLIT_KEY + cabinetIndex.getId();
                Object cabObject = ops.get(cabKey);
                if (Objects.nonNull(cabObject)) {
                    JSONObject data = JSON.parseObject(JSON.toJSONString(cabObject));
                    JSONObject cabData = data.containsKey(CABINET_POWER) ? data.getJSONObject(CABINET_POWER) : new JSONObject();
                    JSONObject envData = data.containsKey(CABINET_ENV)?data.getJSONObject(CABINET_ENV):new JSONObject();
                    //负载率
                    if (data.containsKey(LOAD_FACTOR)){
                        cabDto.setLoadRate(data.getFloatValue(LOAD_FACTOR));
                    }
                    //A数据
                    if (cabData.containsKey(PATH_A)){
                        JSONObject aData = cabData.getJSONObject(PATH_A);
                        //电流
                        if (aData.containsKey(CUR_VALUE)){
                            cabDto.setLineCurA(aData.getObject(CUR_VALUE,float[].class));
                        }
                        //电压
                        if (aData.containsKey(VOL_VALUE)){
                            cabDto.setLineVolA(aData.getObject(VOL_VALUE,float[].class));
                        }
                        //有功功率
                        if (aData.containsKey(POW_ACTIVE)){
                            cabDto.setPowActiveA(aData.getFloatValue(POW_ACTIVE));
                        }
                        //视在功率
                        if (aData.containsKey(POW_APPARENT)){
                            cabDto.setPowApparentA(aData.getFloatValue(POW_APPARENT));
                        }
                        //无功功率
                        if (aData.containsKey(POW_REACTIVE)){
                            cabDto.setPowReactiveA(aData.getFloatValue(POW_REACTIVE));
                        }
                        //功率因素
                        if (aData.containsKey(POWER_FACTOR)){
                            cabDto.setPowerFactorA(aData.getFloatValue(POWER_FACTOR));
                        }
                    }

                    //b数据
                    if (cabData.containsKey(PATH_B)){
                        JSONObject bData = cabData.getJSONObject(PATH_B);
                        //电流
                        if (bData.containsKey(CUR_VALUE)){
                            cabDto.setLineCurB(bData.getObject(CUR_VALUE,float[].class));
                        }
                        //电压
                        if (bData.containsKey(VOL_VALUE)){
                            cabDto.setLineVolB(bData.getObject(VOL_VALUE,float[].class));
                        }
                        //有功功率
                        if (bData.containsKey(POW_ACTIVE)){
                            cabDto.setPowActiveB(bData.getFloatValue(POW_ACTIVE));
                        }
                        //视在功率
                        if (bData.containsKey(POW_APPARENT)){
                            cabDto.setPowApparentB(bData.getFloatValue(POW_APPARENT));
                        }
                        //无功功率
                        if (bData.containsKey(POW_REACTIVE)){
                            cabDto.setPowReactiveB(bData.getFloatValue(POW_REACTIVE));
                        }
                        //功率因素
                        if (bData.containsKey(POWER_FACTOR)){
                            cabDto.setPowerFactorB(bData.getFloatValue(POWER_FACTOR));
                        }
                    }
                    //总数据
                    if (cabData.containsKey(TOTAL_DATA)){
                        JSONObject totalData = cabData.getJSONObject(TOTAL_DATA);
                        //有功功率
                        if (totalData.containsKey(POW_ACTIVE)){
                            cabDto.setPowActive(totalData.getFloatValue(POW_ACTIVE));
                        }
                        //视在功率
                        if (totalData.containsKey(POW_APPARENT)){
                            cabDto.setPowApparent(totalData.getFloatValue(POW_APPARENT));
                        }
                        //无功功率
                        if (totalData.containsKey(POW_REACTIVE)){
                            cabDto.setPowReactive(totalData.getFloatValue(POW_REACTIVE));
                        }
                        //功率因素
                        if (totalData.containsKey(POWER_FACTOR)){
                            cabDto.setPowerFactor(totalData.getFloatValue(POWER_FACTOR));
                        }
                    }

                    //温度
                    if (envData.containsKey(TEM_VALUE)){
                        JSONObject temData = envData.getJSONObject(TEM_VALUE);

                        double[] front = temData.getObject("front",double[].class);
                        double[] black = temData.getObject("black",double[].class);
                        double maxF = Arrays.stream(front).max().getAsDouble();
                        double maxB = Arrays.stream(black).max().getAsDouble();
                        cabDto.setTemData(Math.max(maxB, maxF));
                    }
                }
               cabList.add(cabDto);
            });
            detailDTO.setCabinetList(cabList);
        }


        return detailDTO;
    }

    @Override
    public AisleMainDataDTO getMainData(int aisleId) {
        AisleMainDataDTO dataDTO = new AisleMainDataDTO();
        ValueOperations ops = redisTemplate.opsForValue();

        dataDTO.setId(aisleId);

        String aisleKey =  REDIS_KEY_AISLE + aisleId;
        Object aisleObject = ops.get(aisleKey);
        if (Objects.nonNull(aisleObject)) {
            JSONObject data = JSON.parseObject(JSON.toJSONString(aisleObject));
            JSONObject aisleData = data.containsKey(AISLE_POWER) ? data.getJSONObject(AISLE_POWER) : new JSONObject();

            if (aisleData.containsKey(TOTAL_DATA)){
                JSONObject totalData = aisleData.getJSONObject(TOTAL_DATA);
                //有功功率
                if (totalData.containsKey(POW_ACTIVE)){
                    dataDTO.setPowActive(totalData.getFloatValue(POW_ACTIVE));
                }
                //视在功率
                if (totalData.containsKey(POW_APPARENT)){
                    dataDTO.setPowApparent(totalData.getFloatValue(POW_APPARENT));
                }
                //无功功率
                if (totalData.containsKey(POW_REACTIVE)){
                    dataDTO.setPowReactive(totalData.getFloatValue(POW_REACTIVE));
                }
                //功率因素
                if (totalData.containsKey(POWER_FACTOR)){
                    dataDTO.setPowerFactor(totalData.getFloatValue(POWER_FACTOR));
                }
            }
        }


         //母线
        List<AisleBar>  aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                .eq(AisleBar::getAisleId,aisleId));
        //始端箱
        if (!CollectionUtils.isEmpty(aisleBars)){
            aisleBars.forEach(aisleBar -> {
                String key = aisleBar.getBarKey();
                String busKey =  REDIS_KEY_BUS + key;
                Object busObject = ops.get(busKey);
                if (Objects.nonNull(busObject)) {
                    JSONObject data = JSON.parseObject(JSON.toJSONString(busObject));
                    JSONObject busData = data.containsKey(BUS_DATA) ? data.getJSONObject(BUS_DATA) : new JSONObject();

                    //相数据
                    if (busData.containsKey(LINE_ITEM_LIST)){
                        JSONObject lineData = busData.getJSONObject(LINE_ITEM_LIST);

                        //电流
                        if (lineData.containsKey(CUR_VALUE)){
                            if ("A".equals(aisleBar.getPath())){
                                dataDTO.setBarLineCurA(lineData.getObject(CUR_VALUE,float[].class));
                            }
                            if ("B".equals(aisleBar.getPath())){
                                dataDTO.setBarLineCurB(lineData.getObject(CUR_VALUE,float[].class));
                            }
                        }
                        //电压
                        if (lineData.containsKey(VOL_VALUE)){
                            if ("A".equals(aisleBar.getPath())){
                                dataDTO.setBarLineVolA(lineData.getObject(VOL_VALUE,float[].class));
                            }
                            if ("B".equals(aisleBar.getPath())){
                                dataDTO.setBarLineVolB(lineData.getObject(VOL_VALUE,float[].class));
                            }
                        }

                    }

                }
            });
        }

        //机柜
        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                .eq(CabinetIndex::getAisleId,aisleId)
                .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus()));

        List<CabinetMainDataDTO> cabList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cabinetIndexList)){
            cabinetIndexList.forEach(cabinetIndex ->{
                CabinetMainDataDTO cabDto = new CabinetMainDataDTO();
                cabDto.setId(cabinetIndex.getId());
                cabDto.setCabinetName(cabinetIndex.getName());

                String cabKey =  REDIS_KEY_CABINET + cabinetIndex.getRoomId() + SPLIT_KEY + cabinetIndex.getId();
                Object cabObject = ops.get(cabKey);
                if (Objects.nonNull(cabObject)) {
                    JSONObject data = JSON.parseObject(JSON.toJSONString(cabObject));
                    JSONObject cabData = data.containsKey(CABINET_POWER) ? data.getJSONObject(CABINET_POWER) : new JSONObject();
                    //A数据
                    if (cabData.containsKey(PATH_A)){
                        JSONObject aData = cabData.getJSONObject(PATH_A);

                        //有功功率
                        if (aData.containsKey(POW_ACTIVE)){
                            cabDto.setPowActiveA(aData.getFloatValue(POW_ACTIVE));
                        }
                        //视在功率
                        if (aData.containsKey(POW_APPARENT)){
                            cabDto.setPowApparentA(aData.getFloatValue(POW_APPARENT));
                        }
                        //无功功率
                        if (aData.containsKey(POW_REACTIVE)){
                            cabDto.setPowReactiveA(aData.getFloatValue(POW_REACTIVE));
                        }
                        //功率因素
                        if (aData.containsKey(POWER_FACTOR)){
                            cabDto.setPowerFactorA(aData.getFloatValue(POWER_FACTOR));
                        }
                    }

                    //b数据
                    if (cabData.containsKey(PATH_B)){
                        JSONObject bData = cabData.getJSONObject(PATH_B);

                        //有功功率
                        if (bData.containsKey(POW_ACTIVE)){
                            cabDto.setPowActiveB(bData.getFloatValue(POW_ACTIVE));
                        }
                        //视在功率
                        if (bData.containsKey(POW_APPARENT)){
                            cabDto.setPowApparentB(bData.getFloatValue(POW_APPARENT));
                        }
                        //无功功率
                        if (bData.containsKey(POW_REACTIVE)){
                            cabDto.setPowReactiveB(bData.getFloatValue(POW_REACTIVE));
                        }
                        //功率因素
                        if (bData.containsKey(POWER_FACTOR)){
                            cabDto.setPowerFactorB(bData.getFloatValue(POWER_FACTOR));
                        }
                    }
                }
                cabList.add(cabDto);
            });
            dataDTO.setCabinetList(cabList);
        }


        return dataDTO;
    }

    @Override
    public AisleEqDataDTO getMainEq(int aisleId) {

        AisleEqDataDTO eqDataDTO = new AisleEqDataDTO();
        eqDataDTO.setId(aisleId);
        try {


            getDayChain(aisleId, eqDataDTO);


            getWeekChain(aisleId, eqDataDTO);


            getMonthChain(aisleId, eqDataDTO);


        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return eqDataDTO;
    }


    /**
     * 获取数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ids 机柜id列表
     * @param index 索引表
     */
    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index,String idStr) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery(idStr, ids))));
        builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(10000);

        List<String> list = new ArrayList<>();
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                list.add(str);
            }
        }
        return list;

    }

    /**
     * 获取日环比
     *
     * @param id
     * @param eqDataDTO
     * @return
     */
    private void getDayChain(int id, AisleEqDataDTO eqDataDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //日环比
        //今日
        startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double todayEq = getDayEq(startTime, endTime, id);
        eqDataDTO.setTodayEq(todayEq);
        //昨日
        List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_DAY);
        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalDayDo dayDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(dayDo.getCreateTime());
            eqMap.put(dateTime, dayDo.getEqValue());

        });

        //昨天
        String thisTime = DateUtil.formatDate(DateUtil.beginOfDay(DateTime.now()));
        double lastEq = eqMap.containsKey(thisTime) ? eqMap.get(thisTime) : 0;
        eqDataDTO.setYesterdayEq(lastEq);

    }

    /**
     * 获取周环比
     *
     * @param id
     * @param eqDataDTO
     */
    private void getWeekChain(int id, AisleEqDataDTO eqDataDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //周环比
        //本周
        startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double thisWeekEq = getDayEq(startTime, endTime, id);
        eqDataDTO.setThisWeekEq(thisWeekEq);

       List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_WEEK);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalWeekDo weekDo = JsonUtils.parseObject(str, AisleEqTotalWeekDo.class);
            String dateTime = DateUtil.formatDate(weekDo.getCreateTime());
            eqMap.put(dateTime, weekDo.getEqValue());

        });
        //上周
        String thisTime = DateUtil.formatDate(DateUtil.beginOfWeek(DateTime.now()));
        double lastEq = eqMap.containsKey(thisTime) ? eqMap.get(thisTime) : 0;

        eqDataDTO.setLastWeekEq(lastEq);
    }

    /**
     * 获取月环比
     *
     * @param id
     * @param eqDataDTO
     */
    private void getMonthChain(int id, AisleEqDataDTO eqDataDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //月环比
        //本月
        startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double thisMonthEq = getDayEq(startTime, endTime, id);
        eqDataDTO.setThisMonthEq(thisMonthEq);
        //上月
        List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_MONTH);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalMonthDo monthDo = JsonUtils.parseObject(str, AisleEqTotalMonthDo.class);
            String dateTime = DateUtil.formatDate(monthDo.getCreateTime());
            eqMap.put(dateTime, monthDo.getEqValue());

        });

        //上月
        String thisMonthTime = DateUtil.formatDate(DateUtil.beginOfMonth(DateTime.now()));
        double lastMonthEq = eqMap.containsKey(thisMonthTime) ? eqMap.get(thisMonthTime) : 0;
        eqDataDTO.setLastMonthEq(lastMonthEq);
    }

    /**
     * 获取日用电量
     *
     * @param startTime
     * @param endTime
     * @param id
     * @return
     */
    private double getDayEq(String startTime, String endTime, int id) {
        log.info("startTime : " + startTime + "endTime：" + endTime);
        //获取时间段内第一条和最后一条数据
        AisleEleTotalRealtimeDo endRealtimeDo = getEleData(startTime, endTime,
                SortOrder.DESC,
                AISLE_ELE_TOTAL_REALTIME, id);
        AisleEleTotalRealtimeDo startRealtimeDo = getEleData(startTime, endTime,
                SortOrder.ASC,
                AISLE_ELE_TOTAL_REALTIME, id);
        //结束时间电量
        double endEle = endRealtimeDo.getEleTotal();

        //开始时间电量
        double startEle = startRealtimeDo.getEleTotal();

        //判断使用电量  开始电量大于结束电量，记为0
        double eq;
        if (startEle > endEle) {
            eq = 0;
        } else {
            eq = endEle - startEle;
        }
        return eq;
    }
    /**
     * @param startTime
     * @param endTime
     * @param sortOrder
     * @param index
     * @param id
     * @return
     */
    private AisleEleTotalRealtimeDo getEleData(String startTime, String endTime, SortOrder sortOrder, String index, int id) {
        AisleEleTotalRealtimeDo realtimeDo = new AisleEleTotalRealtimeDo();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD)
                            .gte(startTime)
                            .lt(endTime))
                    .must(QueryBuilders.termQuery(AISLE_ID, id))));

            // 嵌套聚合
            // 设置聚合查询
            String top = "top";
            AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                    .size(1).sort(CREATE_TIME + KEYWORD, sortOrder);

            builder.aggregation(topAgg);

            // 设置搜索条件
            searchRequest.source(builder);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();

            TopHits tophits = aggregations.get(top);
            SearchHits sophistsHits = tophits.getHits();
            if (null != sophistsHits.getHits() && sophistsHits.getHits().length>0){
                SearchHit hit = sophistsHits.getHits()[0];
                realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), AisleEleTotalRealtimeDo.class);
            }
            return realtimeDo;
        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return realtimeDo;
    }

    /**
     * 获取es数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param index     索引表
     * @return
     * @throws IOException
     */
    private List<String> getData(String startTime, String endTime, int id, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termQuery(AISLE_ID, id))));
        builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(10000);

        List<String> list = new ArrayList<>();
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                list.add(str);
            }
        }
        return list;

    }

}
