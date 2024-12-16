package cn.iocoder.yudao.module.room.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleCabinetDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleDetailDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.room.pow.RoomPowDayDo;
import cn.iocoder.yudao.framework.common.entity.es.room.pow.RoomPowRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.*;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.*;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduIndexDo;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomSavesVo;
import cn.iocoder.yudao.framework.common.enums.*;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.aisle.api.AisleApi;
import cn.iocoder.yudao.module.cabinet.api.CabinetApi;
import cn.iocoder.yudao.module.room.dto.*;
import cn.iocoder.yudao.module.room.service.RoomService;
import cn.iocoder.yudao.module.room.vo.RoomSaveVo;
import cn.iocoder.yudao.module.system.api.alarm.AlarmRecordApi;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mzt.logapi.service.IFunctionService;
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
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.TEM_VALUE;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房操作
 * @date 2024/6/21 14:19
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Resource
    RoomIndexMapper roomIndexMapper;

    @Autowired
    RoomCfgMapper roomCfgMapper;

    @Resource
    AisleIndexMapper aisleIndexMapper;

    @Autowired
    AisleCfgMapper aisleCfgMapper;

    @Resource
    RedisTemplate redisTemplate;
    @Resource
    CabinetIndexMapper cabinetIndexMapper;
    @Resource
    CabinetCfgDoMapper cfgDoMapper;
    @Value("${room-refresh-url}")
    public String adder;

    @Resource
    RackIndexDoMapper rackIndexDoMapper;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private CabinetPduMapper cabinetPduMapper;
    @Autowired
    private CabinetBusMapper cabinetBusMapper;
    @Autowired
    private AisleBarMapper aisleBarMapper;
    @Autowired
    private AisleBoxMapper aisleBoxMapper;
    @Resource
    AlarmRecordApi alarmRecordApi;
    @Resource
    PduIndexDoMapper pduIndexDoMapper;

    @Autowired
    CabinetCfgMapper cabinetCfgMapper;

    @Autowired
    CabinetEnvSensorMapper envSensorMapper;

    @Autowired
    CabinetApi cabinetApi;
    @Autowired
    AisleApi aisleApi;
    /**
     * 机房保存
     * @param roomSaveVo 保存参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer roomSave(RoomSaveVo roomSaveVo) {

        try {
            RoomIndex index = new RoomIndex();
            index.setRoomName(roomSaveVo.getRoomName());
            index.setPowerCapacity(roomSaveVo.getPowerCapacity());
//            index.setEleAlarmDay(roomSaveVo.getEleAlarmDay());
//            index.setEleLimitDay(roomSaveVo.getEleLimitDay());
//            index.setEleAlarmMonth(roomSaveVo.getEleAlarmMonth());
//            index.setEleLimitMonth(roomSaveVo.getEleLimitMonth());
            index.setYLength(roomSaveVo.getYLength());
            index.setXLength(roomSaveVo.getXLength());
            if (Objects.nonNull(roomSaveVo.getId())){
                //编辑
                RoomIndex roomIndex = roomIndexMapper.selectOne(new LambdaQueryWrapper<RoomIndex>()
                        .eq(RoomIndex::getId,roomSaveVo.getId()));
                if (Objects.nonNull(roomIndex)){
                    index.setId(roomSaveVo.getId());
                    roomIndexMapper.updateById(index);
                }

            }else {
                //新增
                roomIndexMapper.insert(index);
            }

            roomSaveVo.setId(index.getId());

            //柜列
            if (!CollectionUtils.isEmpty(roomSaveVo.getAisleList())){
                //更改后的柜列
                List<Integer> ids =  roomSaveVo.getAisleList().stream().map(AisleSaveVo::getId).filter(Objects::nonNull).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(ids)){
                    //需要删除的机柜
                    List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaUpdateWrapper<CabinetIndex>()
                                    .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                                    .eq(CabinetIndex::getRoomId,roomSaveVo.getId())
                                    .gt(CabinetIndex::getAisleId,0)
                                    .notIn(CabinetIndex::getAisleId,ids));
                    //需要删除的机架
                    if (!CollectionUtils.isEmpty(cabinetIndexList)){
                        rackIndexDoMapper.delete(new LambdaQueryWrapper<RackIndex>()
                                .in(RackIndex::getCabinetId,cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                    }

                    //删除柜列下机柜
                    if (!CollectionUtils.isEmpty(cabinetIndexList)){
                       cabinetIndexList.forEach(cabinetIndex -> {
                           try {
                               cabinetApi.delCabinet(cabinetIndex.getId());
                           } catch (Exception e) {
                               throw new RuntimeException(e);
                           }
                       });
                    }
                    //删除柜列
                    List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                            .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                            .eq(AisleIndex::getRoomId,roomSaveVo.getId())
                            .notIn(AisleIndex::getId,ids));
                    if (!CollectionUtils.isEmpty(aisleIndexList)){
                        aisleIndexList.forEach(aisleIndex -> aisleApi.deleteAisle(aisleIndex.getId()));
                    }


                }else {


                    //需要删除的机柜
                    List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaUpdateWrapper<CabinetIndex>()
                            .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                            .eq(CabinetIndex::getRoomId,roomSaveVo.getId())
                            .gt(CabinetIndex::getAisleId,0));

                    //需要删除的机架
                    if (!CollectionUtils.isEmpty(cabinetIndexList)){
                        rackIndexDoMapper.delete(new LambdaQueryWrapper<RackIndex>()
                                .in(RackIndex::getCabinetId,cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                    }
                    //删除柜列下机柜
                    if (!CollectionUtils.isEmpty(cabinetIndexList)){
                        cabinetIndexList.forEach(cabinetIndex -> {
                            try {
                                cabinetApi.delCabinet(cabinetIndex.getId());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    //删除柜列
                    List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                            .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                            .eq(AisleIndex::getRoomId,roomSaveVo.getId()));
                    if (!CollectionUtils.isEmpty(aisleIndexList)){
                        aisleIndexList.forEach(aisleIndex -> aisleApi.deleteAisle(aisleIndex.getId()));
                    }

                }

                roomSaveVo.getAisleList().forEach(aisleSaveVo -> {
                    aisleSaveVo.setRoomId(index.getId());
                    aisleSave(aisleSaveVo);
                });
            }else {

                //需要删除的机柜
                List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaUpdateWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                        .eq(CabinetIndex::getRoomId,roomSaveVo.getId())
                        .gt(CabinetIndex::getAisleId,0));
                //需要删除的机架
                if (!CollectionUtils.isEmpty(cabinetIndexList)){
                    rackIndexDoMapper.delete(new LambdaQueryWrapper<RackIndex>()
                            .in(RackIndex::getCabinetId,cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                }

                //删除柜列下机柜
                if (!CollectionUtils.isEmpty(cabinetIndexList)){
                    cabinetIndexList.forEach(cabinetIndex -> {
                        try {
                            cabinetApi.delCabinet(cabinetIndex.getId());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                //删除柜列
                List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                        .eq(AisleIndex::getRoomId,roomSaveVo.getId()));
              if (!CollectionUtils.isEmpty(aisleIndexList)){
                  aisleIndexList.forEach(aisleIndex -> aisleApi.deleteAisle(aisleIndex.getId()));
              }
            }

            //机柜
            if (!CollectionUtils.isEmpty(roomSaveVo.getCabinetList())){
                //修改后的机柜列表
                List<Integer> ids =  roomSaveVo.getCabinetList().stream().map(CabinetVo::getId).filter(id -> id >0).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(ids)){

                    //需要删除的机柜
                    List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaUpdateWrapper<CabinetIndex>()
                            .eq(CabinetIndex::getRoomId,roomSaveVo.getId())
                            .eq(CabinetIndex::getAisleId,0)
                            .notIn(CabinetIndex::getId,ids));
                    //需要删除的机架
                    if (!CollectionUtils.isEmpty(cabinetIndexList)){
                        rackIndexDoMapper.delete(new LambdaQueryWrapper<RackIndex>()
                                .in(RackIndex::getCabinetId,cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                    }

                    if (!CollectionUtils.isEmpty(cabinetIndexList)){
                        cabinetIndexList.forEach(cabinetIndex -> {
                            try {
                                cabinetApi.delCabinet(cabinetIndex.getId());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }


                }else {
                    //需要删除的机柜
                    List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaUpdateWrapper<CabinetIndex>()
                            .eq(CabinetIndex::getRoomId,roomSaveVo.getId())
                            .eq(CabinetIndex::getAisleId,0));
                    //需要删除的机架
                    if (!CollectionUtils.isEmpty(cabinetIndexList)){
                        rackIndexDoMapper.delete(new LambdaQueryWrapper<RackIndex>()
                                .in(RackIndex::getCabinetId,cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                    }

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
                roomSaveVo.getCabinetList().forEach(cabinetVo -> {
                    cabinetVo.setRoomId(index.getId());
                    saveCabinet(cabinetVo);
                });

            }else {

                //需要删除的机柜
                List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaUpdateWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                        .eq(CabinetIndex::getRoomId,roomSaveVo.getId())
                        .eq(CabinetIndex::getAisleId,0));
                //需要删除的机架
                if (!CollectionUtils.isEmpty(cabinetIndexList)){
                    rackIndexDoMapper.delete(new LambdaQueryWrapper<RackIndex>()
                            .in(RackIndex::getCabinetId,cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                }

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
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Integer roomId) {
        try {
            //删除机房
            RoomIndex roomIndex = roomIndexMapper.selectById(roomId);
            if (Objects.nonNull(roomIndex)){
                List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                        .eq(CabinetIndex::getRoomId,roomIndex.getId()));
                if (!CollectionUtils.isEmpty(cabinetIndexList)){
                    throw new RuntimeException("存在未删除机柜，不可删除");
                }

                List<AisleIndex> aisleIndices = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                        .eq(AisleIndex::getRoomId,roomIndex.getId()));
                if (!CollectionUtils.isEmpty(aisleIndices)){
                    throw new RuntimeException("存在未删除柜列，不可删除");
                }

                //逻辑删除
                if (roomIndex.getIsDelete() == (DelEnums.NO_DEL.getStatus())){
                    roomIndexMapper.update(new LambdaUpdateWrapper<RoomIndex>()
                            .eq(RoomIndex::getId, roomId)
                            .set(RoomIndex::getIsDelete, DelEnums.DELETE.getStatus()));
                    //删除柜列
                    List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                            .eq(AisleIndex::getRoomId,roomId));

                    aisleIndexMapper.update(new LambdaUpdateWrapper<AisleIndex>()
                            .eq(AisleIndex::getRoomId, roomId)
                            .set(AisleIndex::getIsDelete, DelEnums.DELETE.getStatus()));

                    if (!CollectionUtils.isEmpty(aisleIndexList)){
                        aisleIndexList.forEach(aisleIndex -> {
                            //删除key
                            String key = REDIS_KEY_AISLE + aisleIndex.getId();
                            redisTemplate.delete(key);
                        });
                    }

                    //删除机柜
                    cabinetIndexMapper.update(new LambdaUpdateWrapper<CabinetIndex>()
                            .eq(CabinetIndex::getRoomId, roomId)
                            .set(CabinetIndex::getIsDeleted, DelEnums.DELETE.getStatus()));
                }else {
                    //物理删除
                    //删除机房
                    roomIndexMapper.deleteById(roomId);

                    //物理删除
                    //1.删除绑定关系
                    List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                            .eq(AisleIndex::getRoomId,roomId));
                    aisleBoxMapper.delete(new LambdaQueryWrapper<AisleBox>()
                            .in(!CollectionUtils.isEmpty(aisleIndexList),
                                    AisleBox::getAisleId,
                                    aisleIndexList.stream().map(AisleIndex::getId).collect(Collectors.toList())));
                    aisleBarMapper.delete(new LambdaQueryWrapper<AisleBar>()
                            .in(!CollectionUtils.isEmpty(aisleIndexList),
                                    AisleBar::getAisleId,
                                    aisleIndexList.stream().map(AisleIndex::getId).collect(Collectors.toList())));
                    //2.删除柜列
                    aisleIndexMapper.delete(new LambdaQueryWrapper<AisleIndex>()
                            .eq(AisleIndex::getRoomId,roomId));
                    if (!CollectionUtils.isEmpty(aisleIndexList)){
                        aisleIndexList.forEach(aisleIndex -> {
                            //删除key
                            String key = REDIS_KEY_AISLE + aisleIndex.getId();
                            redisTemplate.delete(key);
                        });
                    }

                }
                //删除机柜
                List<CabinetIndex> indices = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getRoomId,roomId));
                if (!CollectionUtils.isEmpty(indices)){
                    indices.forEach(cabinetIndex -> {
                        delCabinet(cabinetIndex.getId());
                    });
                }
            }
            //删除key
            String key = REDIS_KEY_ROOM + roomId;

            boolean flag = redisTemplate.delete(key);
            log.info("key: " + key + " flag : " + flag);
        }finally {
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }



    }

    /**
     * 删除机柜
     * @param id
     * @return
     */
    private int delCabinet(int id) {
        try {
            CabinetIndex index = cabinetIndexMapper.selectById(id);
            if (Objects.isNull(index)) {
                return -1;
            }
            if (index.getIsDeleted() == DelFlagEnums.DELETE.getStatus()) {
                //已经删除则物理删除
                cabinetIndexMapper.deleteById(id);
                //删除pdu关联关系
                cabinetPduMapper.delete(new LambdaQueryWrapper<CabinetPdu>()
                        .eq(CabinetPdu::getCabinetId, id));
                //删除配置信息
                cabinetCfgMapper.delete(new LambdaQueryWrapper<CabinetCfg>()
                        .eq(CabinetCfg::getCabinetId, id));
                //删除环境信息
                envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                        .eq(CabinetEnvSensor::getCabinetId,id));
            } else {
                //逻辑删除
                cabinetIndexMapper.update(new LambdaUpdateWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getId, id)
                        .set(CabinetIndex::getIsDeleted, DelEnums.DELETE.getStatus()));
            }

            //删除key
            String key = REDIS_KEY_CABINET + index.getRoomId() + SPLIT_KEY + index.getId();

            boolean flag = redisTemplate.delete(key);
            log.info("key: " + key + " flag : " + flag);

            return id;
        } finally {
            log.info("刷新计算服务缓存 --- " + adder);
            //刷新机柜计算服务缓存
            HttpUtil.get(adder);
        }
    }

    @Override
    public RoomDetailDTO getDetail(Integer roomId) {
        RoomDetailDTO roomDetailDTO = new RoomDetailDTO();
        RoomIndex roomIndex = roomIndexMapper.selectById(roomId);
        RoomCfg roomCfg = roomCfgMapper.selectRoomCfgByRoomId(roomId);
        roomDetailDTO.setRoomName(roomIndex.getRoomName());
        roomDetailDTO.setId(roomId);
        roomDetailDTO.setPowerCapacity(roomIndex.getPowerCapacity());
        roomDetailDTO.setAirPower(roomIndex.getAirPower());
        roomDetailDTO.setEleAlarmDay(roomCfg.getEleAlarmDay());
        roomDetailDTO.setEleAlarmMonth(roomCfg.getEleAlarmMonth());
        roomDetailDTO.setEleLimitDay(roomCfg.getEleLimitDay());
        roomDetailDTO.setEleLimitMonth(roomCfg.getEleLimitMonth());
        roomDetailDTO.setXLength(roomIndex.getXLength());
        roomDetailDTO.setYLength(roomIndex.getYLength());

        AtomicInteger totalSpace = new AtomicInteger();
        AtomicInteger totalUsedSpace = new AtomicInteger();
        AtomicInteger deviceNum = new AtomicInteger();

        //获取机柜
        //无柜列机柜
        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                .eq(CabinetIndex::getRoomId,roomId)
                .eq(CabinetIndex::getAisleId,0)
                .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus()));
        List<AisleCabinetDTO> aisleCabinetDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cabinetIndexList)){
            List<Integer> cabinetIds = cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList());
            List<CabinetCfg> cabinetCfgList = cfgDoMapper.selectList(new LambdaQueryWrapper<CabinetCfg>()
                    .in(CabinetCfg::getCabinetId,cabinetIds));
            Map<Integer,CabinetCfg> cfgMap;
            if (!CollectionUtils.isEmpty(cabinetCfgList)){
                cfgMap = cabinetCfgList.stream().collect(Collectors.toMap(CabinetCfg::getCabinetId, Function.identity()));
            } else {
                cfgMap = new HashMap<>();
            }

            cabinetIndexList.forEach(cabinetIndex ->{
                AisleCabinetDTO cabinetDTO = BeanUtils.toBean(cabinetIndex,AisleCabinetDTO.class);
                CabinetCfg cfg = cfgMap.get(cabinetIndex.getId());
                if (Objects.nonNull(cfg)){
                    //cabinetDTO.setCabinetName(cfg.getCabinetName());
                    //cabinetDTO.setCabinetHeight(cfg.getCabinetHeight());
                    cabinetDTO.setCompany(cfg.getCompany());
                    cabinetDTO.setXCoordinate(cfg.getXCoordinate());
                    cabinetDTO.setYCoordinate(cfg.getYCoordinate());
                    cabinetDTO.setEleAlarmDay(cfg.getEleAlarmDay());
                    cabinetDTO.setEleAlarmMonth(cfg.getEleAlarmMonth());
                    cabinetDTO.setEleLimitDay(cfg.getEleLimitDay());
                    cabinetDTO.setEleLimitMonth(cfg.getEleLimitMonth());
                    cabinetDTO.setPowerCapacity(cabinetIndex.getPowerCapacity());
                    //cabinetDTO.setType(cfg.getType());
                    //totalSpace.addAndGet(cfg.getCabinetHeight());
                }
                //获取机柜使用空间
                List<RackIndex> rackIndexList = rackIndexDoMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                        .eq(RackIndex::getCabinetId,cabinetIndex.getId())
                        .eq(RackIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
                if (!CollectionUtils.isEmpty(rackIndexList)){
                    int usedSpace = rackIndexList.stream().map(RackIndex::getUHeight).reduce(0,Integer::sum);
                    int freeSpace = cabinetDTO.getCabinetHeight() - usedSpace;
                    cabinetDTO.setUsedSpace(usedSpace);
                    cabinetDTO.setFreeSpace(freeSpace);
                    totalUsedSpace.addAndGet(usedSpace);
                }else {
                    cabinetDTO.setFreeSpace(cabinetDTO.getCabinetHeight());
                }
                aisleCabinetDTOList.add(cabinetDTO);
            });

            //获取pdu数量
            List<Integer> ids = cabinetIndexList.stream()
                    .filter(t->t.getPduBox() == PduBoxFlagEnums.PDU.getValue())
                    .map(CabinetIndex::getId).collect(Collectors.toList());

            List<CabinetPdu> cabinetPdus = cabinetPduMapper.selectList(new LambdaQueryWrapper<CabinetPdu>()
                    .in(!CollectionUtils.isEmpty(ids),CabinetPdu::getCabinetId,ids));
            if (!CollectionUtils.isEmpty(cabinetPdus)) {
                cabinetPdus.forEach(cabinetPdu -> {
                    if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyA())){
                        deviceNum.getAndIncrement();
                    }
                    if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyB())){
                        deviceNum.getAndIncrement();
                    }
                });
            }

        }

        //获取母线始端箱 插接箱数量
        List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                .eq(AisleIndex::getRoomId,roomId)
                .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
        if (!CollectionUtils.isEmpty(aisleIndexList)){
            List<Integer> ids = aisleIndexList.stream().map(AisleIndex::getId).collect(Collectors.toList());

            List<AisleBar> bars = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                    .in(AisleBar::getAisleId,ids));

            List<AisleBox> boxs = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
                    .in(AisleBox::getAisleId,ids));
            deviceNum.addAndGet( CollectionUtils.isEmpty(bars) ? 0 : bars.size());

            deviceNum.addAndGet( CollectionUtils.isEmpty(boxs) ? 0 : boxs.size());
        }
        roomDetailDTO.setCabinetList(aisleCabinetDTOList);
        //柜列
        roomDetailDTO.setAisleList(getAisleDetail(roomId,roomIndex.getRoomName(),totalSpace,totalUsedSpace));
        roomDetailDTO.setTotalSpace(totalSpace.get());
        roomDetailDTO.setUsedSpace(totalUsedSpace.get());
        roomDetailDTO.setFreeSpace(totalSpace.get()-totalUsedSpace.get());
        List<CabinetIndex> cabinetIndices = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                .eq(CabinetIndex::getRoomId,roomId)
                .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus()));
        roomDetailDTO.setCabNum(CollectionUtils.isEmpty(cabinetIndices)? 0 : cabinetIndices.size());
        roomDetailDTO.setDeviceNum(deviceNum.get());
        return roomDetailDTO;
    }

    @Override
    public  RoomDataDTO getDataDetail(int id) {
        RoomDataDTO roomDataDTO = new RoomDataDTO();
        roomDataDTO.setId(id);
        ValueOperations ops = redisTemplate.opsForValue();

        //获取机柜 无柜列
        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                .eq(CabinetIndex::getRoomId,id)
                .eq(CabinetIndex::getAisleId,0)
                .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus()));
        if (!CollectionUtils.isEmpty(cabinetIndexList)){
            List<RoomCabinetDTO> cabinetDTOList = new ArrayList<>();

            List<Integer> cabinetIds = cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList());
            List<CabinetCfg> cabinetCfgList = cfgDoMapper.selectList(new LambdaQueryWrapper<CabinetCfg>()
                    .in(CabinetCfg::getCabinetId,cabinetIds));
            Map<Integer,CabinetCfg> cfgMap;
            if (!CollectionUtils.isEmpty(cabinetCfgList)){
                cfgMap = cabinetCfgList.stream().collect(Collectors.toMap(CabinetCfg::getCabinetId, Function.identity()));
            } else {
                cfgMap = new HashMap<>();
            }

            List<String> keys = new ArrayList<>();
            Map<String,RoomCabinetDTO> map = new HashMap<>();

            cabinetIndexList.forEach(cabinetIndex ->{
                RoomCabinetDTO cabinetDTO = BeanUtils.toBean(cabinetIndex,RoomCabinetDTO.class);
                CabinetCfg cfg = cfgMap.get(cabinetIndex.getId());
                if (Objects.nonNull(cfg)){
                    //cabinetDTO.setCabinetName(cfg.getCabinetName());
                    //cabinetDTO.setCabinetHeight(cfg.getCabinetHeight());
                    cabinetDTO.setXCoordinate(cfg.getXCoordinate());
                    cabinetDTO.setYCoordinate(cfg.getYCoordinate());
                }
                String cabKey =  REDIS_KEY_CABINET + cabinetIndex.getRoomId() + SPLIT_KEY + cabinetIndex.getId();
                keys.add(cabKey);

                map.put(cabinetIndex.getRoomId() + SPLIT_KEY + cabinetIndex.getId(),cabinetDTO);
            });


            List<Object> cabObjects = ops.multiGet(keys);
            if (!CollectionUtils.isEmpty(cabObjects)){
                cabObjects.forEach(cabObject -> {
                    if (Objects.nonNull(cabObject)) {
                        JSONObject data = JSON.parseObject(JSON.toJSONString(cabObject));
                        JSONObject cabData = data.containsKey(CABINET_POWER) ? data.getJSONObject(CABINET_POWER) : new JSONObject();
                        JSONObject envData = data.containsKey(CABINET_ENV)?data.getJSONObject(CABINET_ENV):new JSONObject();

                        RoomCabinetDTO cabinetDTO = map.get(data.getString(CABINET_KEY));

                         //负载率
                        if (data.containsKey(LOAD_FACTOR)){
                            cabinetDTO.setLoadRate(data.getFloatValue(LOAD_FACTOR));
                        }
                        //总数据
                        if (cabData.containsKey(TOTAL_DATA)){
                            JSONObject totalData = cabData.getJSONObject(TOTAL_DATA);
                            //有功功率
                            if (totalData.containsKey(POW_ACTIVE)){
                                cabinetDTO.setPowActive(totalData.getFloatValue(POW_ACTIVE));
                            }
                            //视在功率
                            if (totalData.containsKey(POW_APPARENT)){
                                cabinetDTO.setPowApparent(totalData.getFloatValue(POW_APPARENT));
                            }
                            //无功功率
                            if (totalData.containsKey(POW_REACTIVE)){
                                cabinetDTO.setPowReactive(totalData.getFloatValue(POW_REACTIVE));
                            }
                            //功率因素
                            if (totalData.containsKey(POWER_FACTOR)){
                                cabinetDTO.setPowerFactor(totalData.getFloatValue(POWER_FACTOR));
                            }
                        }

                        //温度
                        if (envData.containsKey(TEM_VALUE)){
                            JSONObject temData = envData.getJSONObject(TEM_VALUE);

                            double[] front = temData.getObject("front",double[].class);
                            double[] black = temData.getObject("black",double[].class);
                            double maxF = Arrays.stream(front).max().getAsDouble();
                            double maxB = Arrays.stream(black).max().getAsDouble();
                            cabinetDTO.setTem(Math.max(maxB, maxF));
                        }
                        cabinetDTOList.add(cabinetDTO);
                    }
                });

            }
            roomDataDTO.setCabinetList(cabinetDTOList);
        }

        //柜列
        List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                .eq(AisleIndex::getRoomId,id)
                .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
        if (!CollectionUtils.isEmpty(aisleIndexList)){
            List<AisleDataDTO> aisleDataDTOS = new ArrayList<>();
            List<Integer> ids = aisleIndexList.stream().map(AisleIndex::getId).collect(Collectors.toList());

            List<CabinetIndex> cabinetIndices = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                    .in(CabinetIndex::getAisleId,ids)
                    .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                    .eq(CabinetIndex::getIsDisabled,DisableEnums.ENABLE.getStatus()));
            if (!CollectionUtils.isEmpty(cabinetIndices)){
                List<CabinetCfg> cabinetCfgList = cfgDoMapper.selectList(new LambdaQueryWrapper<CabinetCfg>()
                        .in(CabinetCfg::getCabinetId,cabinetIndices.stream().map(CabinetIndex::getId).collect(Collectors.toList())));
                Map<Integer,CabinetCfg> cfgMap;
                if (!CollectionUtils.isEmpty(cabinetCfgList)){
                    cfgMap = cabinetCfgList.stream().collect(Collectors.toMap(CabinetCfg::getCabinetId, Function.identity()));
                } else {
                    cfgMap = new HashMap<>();
                }
                Map<Integer,List<CabinetIndex>> cabMap = cabinetIndices.stream().collect(Collectors.groupingBy(CabinetIndex::getAisleId));
                aisleIndexList.forEach(aisleIndex -> {
                    List<CabinetIndex> indices = Objects.nonNull(cabMap)?cabMap.get(aisleIndex.getId()):new ArrayList<>();

                    AisleDataDTO aisleDataDTO = BeanUtils.toBean(aisleIndex,AisleDataDTO.class);
                    List<RoomCabinetDTO> cabinetDTOList = new ArrayList<>();

                    if (!CollectionUtils.isEmpty(indices)) {
                        List<String> keys = new ArrayList<>();
                        Map<String, RoomCabinetDTO> map = new HashMap<>();

                        indices.forEach(cabinetIndex -> {
                            RoomCabinetDTO cabinetDTO = BeanUtils.toBean(cabinetIndex, RoomCabinetDTO.class);
                            CabinetCfg cfg = cfgMap.get(cabinetIndex.getId());
                            if (Objects.nonNull(cfg)) {
                                //cabinetDTO.setCabinetName(cfg.getCabinetName());
                                //cabinetDTO.setCabinetHeight(cfg.getCabinetHeight());
                                cabinetDTO.setXCoordinate(cfg.getXCoordinate());
                                cabinetDTO.setYCoordinate(cfg.getYCoordinate());
                            }
                            String cabKey = REDIS_KEY_CABINET + cabinetIndex.getRoomId() + SPLIT_KEY + cabinetIndex.getId();
                            keys.add(cabKey);
                            map.put(cabinetIndex.getRoomId() + SPLIT_KEY + cabinetIndex.getId(), cabinetDTO);
                        });

                        List<Object> cabObjects = ops.multiGet(keys);
                        if (!CollectionUtils.isEmpty(cabObjects)) {
                            cabObjects.forEach(cabObject -> {
                                if (Objects.nonNull(cabObject)) {
                                    JSONObject data = JSON.parseObject(JSON.toJSONString(cabObject));
                                    JSONObject cabData = data.containsKey(CABINET_POWER) ? data.getJSONObject(CABINET_POWER) : new JSONObject();
                                    JSONObject envData = data.containsKey(CABINET_ENV) ? data.getJSONObject(CABINET_ENV) : new JSONObject();

                                    RoomCabinetDTO cabinetDTO = map.get(data.getString(CABINET_KEY));

                                    //负载率
                                    if (data.containsKey(LOAD_FACTOR)) {
                                        cabinetDTO.setLoadRate(data.getFloatValue(LOAD_FACTOR));
                                    }
                                    //总数据
                                    if (cabData.containsKey(TOTAL_DATA)) {
                                        JSONObject totalData = cabData.getJSONObject(TOTAL_DATA);
                                        //有功功率
                                        if (totalData.containsKey(POW_ACTIVE)) {
                                            cabinetDTO.setPowActive(totalData.getFloatValue(POW_ACTIVE));
                                        }
                                        //视在功率
                                        if (totalData.containsKey(POW_APPARENT)) {
                                            cabinetDTO.setPowApparent(totalData.getFloatValue(POW_APPARENT));
                                        }
                                        //无功功率
                                        if (totalData.containsKey(POW_REACTIVE)) {
                                            cabinetDTO.setPowReactive(totalData.getFloatValue(POW_REACTIVE));
                                        }
                                        //功率因素
                                        if (totalData.containsKey(POWER_FACTOR)) {
                                            cabinetDTO.setPowerFactor(totalData.getFloatValue(POWER_FACTOR));
                                        }
                                    }

                                    //温度
                                    if (envData.containsKey(TEM_VALUE)) {
                                        JSONObject temData = envData.getJSONObject(TEM_VALUE);

                                        double[] front = temData.getObject("front", double[].class);
                                        double[] black = temData.getObject("black", double[].class);
                                        double maxF = Arrays.stream(front).max().getAsDouble();
                                        double maxB = Arrays.stream(black).max().getAsDouble();
                                        cabinetDTO.setTem(Math.max(maxB, maxF));
                                    }
                                    cabinetDTOList.add(cabinetDTO);
                                }
                            });

                        }
                    }
                    aisleDataDTO.setCabinetList(cabinetDTOList);
                    aisleDataDTOS.add(aisleDataDTO);
                });
            }

            roomDataDTO.setAisleList(aisleDataDTOS);


        }
        return roomDataDTO;

    }

    @Override
    public RoomEqDataDTO getMainEq(int id) {
        RoomEqDataDTO eqDataDTO = new RoomEqDataDTO();
        eqDataDTO.setId(id);
        try {


            getDayChain(id, eqDataDTO);


            getWeekChain(id, eqDataDTO);


            getMonthChain(id, eqDataDTO);


        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return eqDataDTO;
    }

    @Override
    public RoomDevDataDTO getMainDevData(int id) {

        RoomDevDataDTO roomMainDataDTO = new RoomDevDataDTO();
        roomMainDataDTO.setId(id);

        //设备管理
        AtomicInteger deviceNum = new AtomicInteger();
        //获取母线始端箱 插接箱数量
        List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                .eq(AisleIndex::getRoomId,id)
                .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
        if (!CollectionUtils.isEmpty(aisleIndexList)){
            List<Integer> ids = aisleIndexList.stream().map(AisleIndex::getId).collect(Collectors.toList());

            List<AisleBar> bars = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                    .in(AisleBar::getAisleId,ids));

            List<AisleBox> boxs = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
                    .in(AisleBox::getAisleId,ids));
            deviceNum.addAndGet((CollectionUtils.isEmpty(bars) ? 0 : bars.size()));

            deviceNum.addAndGet((CollectionUtils.isEmpty(boxs) ? 0 : boxs.size()));
        }

        //无柜列机柜
        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                .eq(CabinetIndex::getRoomId,id)
                .eq(CabinetIndex::getAisleId,0)
                .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus()));
        if (!CollectionUtils.isEmpty(cabinetIndexList)){
            //获取pdu数量
            List<Integer> ids = cabinetIndexList.stream()
                    .filter(t->t.getPduBox() == PduBoxFlagEnums.PDU.getValue())
                    .map(CabinetIndex::getId).collect(Collectors.toList());

            List<CabinetPdu> cabinetPdus = cabinetPduMapper.selectList(new LambdaQueryWrapper<CabinetPdu>()
                    .in(!CollectionUtils.isEmpty(ids),CabinetPdu::getCabinetId,ids));
            if (!CollectionUtils.isEmpty(cabinetPdus)) {
                cabinetPdus.forEach(cabinetPdu -> {
                    if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyA())){
                        deviceNum.getAndIncrement();
                    }
                    if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyB())){
                        deviceNum.getAndIncrement();
                    }
                });
            }

        }
        //报警数量
        RoomIndex roomIndex = roomIndexMapper.selectById(id);
        int alarmNum = alarmRecordApi.getAlarmRecordNum(roomIndex.getRoomName(),Arrays.asList(AlarmTypeEnums.ELE.getType(),
                AlarmTypeEnums.STATUS.getType()));
        int offLineNum = alarmRecordApi.getAlarmRecordNum(roomIndex.getRoomName(),Arrays.asList(AlarmTypeEnums.OFF_LINE.getType()));
        roomMainDataDTO.setAlarmNum(alarmNum);
        roomMainDataDTO.setOffLineNum(offLineNum);
        int normal = deviceNum.get() - alarmNum -offLineNum;
        roomMainDataDTO.setNormalNum(Math.max(normal, 0));
        roomMainDataDTO.setDeviceNum(deviceNum.get());

        return roomMainDataDTO;
    }

    @Override
    public RoomPowDataDTO getMainPowData(int id) {

        RoomPowDataDTO roomMainDataDTO = new RoomPowDataDTO();
        roomMainDataDTO.setId(id);
        //功率数据
        ValueOperations ops = redisTemplate.opsForValue();
        String key = REDIS_KEY_ROOM + id;
        Object object = ops.get(key);
        if (Objects.nonNull(object)){
            JSONObject data = JSON.parseObject(JSON.toJSONString(object));
            JSONObject roomData = data.containsKey(ROOM_POWER) ? data.getJSONObject(ROOM_POWER) : new JSONObject();
            if (roomData.containsKey(TOTAL_DATA)){
                JSONObject totalData = roomData.getJSONObject(TOTAL_DATA);
                //有功功率
                if (totalData.containsKey(POW_ACTIVE)){
                    roomMainDataDTO.setPowActive(totalData.getFloatValue(POW_ACTIVE));
                }
                //视在功率
                if (totalData.containsKey(POW_APPARENT)){
                    roomMainDataDTO.setPowApparent(totalData.getFloatValue(POW_APPARENT));
                }
                //无功功率
                if (totalData.containsKey(POW_REACTIVE)){
                    roomMainDataDTO.setPowReactive(totalData.getFloatValue(POW_REACTIVE));
                }
                //功率因素
                if (totalData.containsKey(POWER_FACTOR)){
                    roomMainDataDTO.setPowerFactor(totalData.getFloatValue(POWER_FACTOR));
                }
            }

        }

        return roomMainDataDTO;
    }

    @Override
    public RoomCurveDataDTO getMainCurveData(int id) {
        RoomCurveDataDTO roomMainDataDTO = new RoomCurveDataDTO();
        roomMainDataDTO.setId(id);

        //功率曲线
        DateTime end = DateTime.now();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        DateTime start = DateTime.of(calendar.getTime());

        String startTime = DateUtil.formatDateTime(start);
        String endTime = DateUtil.formatDateTime(end);

        List<RoomPowDTO> powDTOS = new ArrayList<>();
        try {
            List<String> data = getData(startTime, endTime, id, ROOM_HDA_POW_HOUR);
            data.forEach(str -> {
                RoomPowDayDo realtimeDo = JsonUtils.parseObject(str, RoomPowDayDo.class);
                RoomPowDTO powDTO = new RoomPowDTO();
                powDTO.setActivePow(realtimeDo.getActiveTotalAvgValue());
                powDTO.setApparentPow(realtimeDo.getApparentTotalAvgValue());
                powDTO.setReactivePow(realtimeDo.getReactiveTotalAvgValue());
                powDTO.setPowerFactor(realtimeDo.getFactorTotalAvgValue());
                powDTO.setDateTime(DateUtil.formatDateTime(realtimeDo.getCreateTime()));
                powDTOS.add(powDTO);
            });
        }catch (Exception e){
            log.error("获取数据异常：",e);
        }
        roomMainDataDTO.setPowList(powDTOS);

        //用能曲线
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        start = DateTime.of(calendar.getTime());
        startTime = DateUtil.formatDateTime(start);

        List<RoomEqDTO> eqDTOS = new ArrayList<>();
        try {
            List<String> data = getData(startTime, endTime, id, ROOM_EQ_TOTAL_DAY);
            data.forEach(str -> {
                RoomEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, RoomEqTotalDayDo.class);
                RoomEqDTO eqDTO = new RoomEqDTO();
                eqDTO.setDateTime(DateUtil.formatDate(totalDayDo.getCreateTime()));
                eqDTO.setEq(totalDayDo.getEqValue());
                eqDTOS.add(eqDTO);
            });
        }catch (Exception e){
            log.error("获取数据异常：",e);
        }
        roomMainDataDTO.setEqList(eqDTOS);


        return roomMainDataDTO;
    }

    @Override
    public RoomEnvDataDTO getMainEnvData(int id) throws IOException {
        RoomEnvDataDTO roomMainDataDTO = new RoomEnvDataDTO();
        roomMainDataDTO.setId(id);

        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                .eq(CabinetIndex::getRoomId,id)
                .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus()));

        if (!CollectionUtils.isEmpty(cabinetIndexList)){
            List<String> pduKeys = new ArrayList<>();
            //获取pdu数量
            List<Integer> ids = cabinetIndexList.stream()
                    .filter(t->t.getPduBox() == PduBoxFlagEnums.PDU.getValue())
                    .map(CabinetIndex::getId).collect(Collectors.toList());

            List<CabinetPdu> cabinetPdus = cabinetPduMapper.selectList(new LambdaQueryWrapper<CabinetPdu>()
                    .in(!CollectionUtils.isEmpty(ids),CabinetPdu::getCabinetId,ids));
            if (!CollectionUtils.isEmpty(cabinetPdus)) {
                cabinetPdus.forEach(cabinetPdu -> {
//                    if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyA())){
//                        pduKeys.add(cabinetPdu.getPduKeyA() + SPLIT_KEY + cabinetPdu.getCasIdA());
//                    }
//                    if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyB())){
//                        pduKeys.add(cabinetPdu.getPduKeyB() + SPLIT_KEY + cabinetPdu.getCasIdB());
//                    }
                    if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyA())){
                       pduKeys.add(cabinetPdu.getPduKeyA() + SPLIT_KEY);
                    }
                    if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyB())){
                        pduKeys.add(cabinetPdu.getPduKeyB() + SPLIT_KEY);
                    }
                });
            }
            if (!CollectionUtils.isEmpty(pduKeys)){
                List<PduIndexDo>  pduIndexDos = pduIndexDoMapper.selectList(new LambdaQueryWrapper<PduIndexDo>()
                        .in(PduIndexDo::getPduKey,pduKeys));

                if (!CollectionUtils.isEmpty(pduIndexDos)){
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, -1);
                    String startTime = DateUtil.formatDateTime(calendar.getTime());
                    String endTime = DateUtil.formatDateTime(new Date());

                    List<Integer> pduIds = pduIndexDos.stream().map(PduIndexDo::getId).collect(Collectors.toList());

                    String index = EsIndexEnum.PDU_ENV_REALTIME.getIndex();
                    // 创建SearchRequest对象, 设置查询索引名
                    SearchRequest searchRequest = new SearchRequest(index);
                    // 通过QueryBuilders构建ES查询条件，
                    SearchSourceBuilder builder = new SearchSourceBuilder();

                    //获取需要处理的数据
                    builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                            .must(QueryBuilders.termsQuery(PDU_ID, pduIds))));
                    builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);

                    // 嵌套聚合
                    TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("time")
                            .field(CREATE_TIME + KEYWORD).size(BUCKET_SIZE);
                    // 设置聚合查询
                    AggregationBuilder agg = aggregationBuilder
                            .subAggregation(AggregationBuilders.avg(TEM_AVG_VALUE).field(TEM))
                            .subAggregation(AggregationBuilders.max(TEM_MAX_VALUE).field(TEM))
                            .subAggregation(AggregationBuilders.min(TEM_MIN_VALUE).field(TEM))
                            //统计平均湿度
                            .subAggregation(AggregationBuilders.avg(HUM_AVG_VALUE).field(HUM))
                            .subAggregation(AggregationBuilders.max(HUM_MAX_VALUE).field(HUM))
                            .subAggregation(AggregationBuilders.min(HUM_MIN_VALUE).field(HUM));

                    builder.aggregation(agg);

                    // 设置搜索条件
                    searchRequest.source(builder);
                    // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
                    builder.size(0);

                    // 执行ES请求
                    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

                    // 处理聚合查询结果
                    Aggregations aggregations = searchResponse.getAggregations();

                    // 根据by_pdu名字查询terms聚合结果
                    Terms byPduAggregation = aggregations.get("time");


                    // 遍历terms聚合结果
                    for (Terms.Bucket bucket : byPduAggregation.getBuckets()) {
                        Avg avg = bucket.getAggregations().get(TEM_AVG_VALUE);
                        roomMainDataDTO.setTemAvg(((Double) avg.getValue()).floatValue());

                        Max max = bucket.getAggregations().get(TEM_MAX_VALUE);
                        roomMainDataDTO.setTemMax(((Double) max.getValue()).floatValue());

                        Min min = bucket.getAggregations().get(TEM_MIN_VALUE);
                        roomMainDataDTO.setTemMin(((Double) min.getValue()).floatValue());

                        avg = bucket.getAggregations().get(HUM_AVG_VALUE);
                        roomMainDataDTO.setHumAvg(((Double) avg.getValue()).floatValue());

                        max = bucket.getAggregations().get(HUM_MAX_VALUE);
                        roomMainDataDTO.setHumMax(((Double) max.getValue()).floatValue());

                        min = bucket.getAggregations().get(HUM_MIN_VALUE);
                        roomMainDataDTO.setHumMin(((Double) min.getValue()).floatValue());


                    }
                }
            }

        }

        roomMainDataDTO.setUpdateTime(DateTime.now());
        return roomMainDataDTO;
    }


    /**
     * 新机房新增/编辑
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer newSaveRoom(RoomSavesVo vo) {
        try {
            RoomIndex index = new RoomIndex();
            index.setRoomName(vo.getRoomName());
            index.setPowerCapacity(vo.getPowerCapacity());
            index.setAirPower(vo.getAirPower());
            index.setYLength(vo.getYLength());
            index.setXLength(vo.getXLength());
            if (Objects.nonNull(vo.getId())){
                //编辑
                RoomIndex roomIndex = roomIndexMapper.selectOne(new LambdaQueryWrapper<RoomIndex>()
                        .eq(RoomIndex::getId,vo.getId()));
                if (Objects.nonNull(roomIndex)){
                    index.setId(vo.getId());
                    int updateById = roomIndexMapper.updateById(index);
                    if (updateById > 0) {
                        roomCfgMapper.updateByRoomCfg(vo);
                    }
                }
            }else{
                //新增
                int insert = roomIndexMapper.insert(index);
                if (insert > 0){
                    //保存配置
                    RoomCfg cfg = new RoomCfg();
                    cfg.setRoomId(index.getId());
                    cfg.setEleAlarmDay(vo.getEleAlarmDay());
                    cfg.setEleAlarmMonth(vo.getEleAlarmMonth());
                    cfg.setEleLimitDay(vo.getEleLimitDay());
                    cfg.setEleLimitMonth(vo.getEleLimitMonth());
                    roomCfgMapper.insert(cfg);
                }
            }
            return index.getId();
        }finally {
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }
    }

    /**
     * 新-机房删除
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void newDeleteRoom(int id) {
        try {
            int affectedRows  = roomIndexMapper.updateByDeleteRoom(id);
            if (affectedRows > 0){
                roomCfgMapper.deleteByRoomCfg(id);
            }
        }finally {
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }
    }


    /**
     * 机房柜列新增/编辑
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer roomAisleSave(AisleSaveVo vo) {
        try {
            AisleIndex index = new AisleIndex();
            index.setAisleName(vo.getAisleName());
            index.setRoomId(vo.getRoomId());
            index.setAisleLength(vo.getAisleLength());
            index.setXCoordinate(vo.getXCoordinate());
            index.setYCoordinate(vo.getYCoordinate());
            index.setPduBar(vo.getPduBar());
            index.setDirection(vo.getDirection());
            if (Objects.nonNull(vo.getId())){
                //编辑
                AisleIndex aisleIndex = aisleIndexMapper.selectOne(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getId,vo.getId()));
                if (Objects.nonNull(aisleIndex)){
                    index.setId(vo.getId());
                    int updateById = aisleIndexMapper.updateById(index);
                    if (updateById > 0) {
                        aisleCfgMapper.updateByAisleCfg(vo);
                    }
                }
            }else {
                int insert = aisleIndexMapper.insert(index);
                if (insert > 0) {
                    //保存配置
                    AisleCfg cfg = new AisleCfg();
                    cfg.setAisleId(index.getId());
                    cfg.setPowerCapacity(vo.getPowerCapacity());
                    cfg.setAisleType("0");
                    cfg.setEleAlarmDay(vo.getEleAlarmDay());
                    cfg.setEleAlarmMonth(vo.getEleAlarmMonth());
                    cfg.setEleLimitDay(vo.getEleLimitDay());
                    cfg.setEleLimitMonth(vo.getEleLimitMonth());
                    aisleCfgMapper.insert(cfg);
                }
            }
            return index.getId();
        }finally {
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }
    }

    /**
     * 新-机房详情
     * @param id
     * @return
     */
    @Override
    public RoomAndRoomCfgDTO getNewRoomDetail(int id) {
        RoomAndRoomCfgDTO roomAndRoomCfgDTO = new RoomAndRoomCfgDTO();
        RoomIndex roomIndex = roomIndexMapper.selectById(id);
        RoomCfg roomCfg = roomCfgMapper.selectRoomCfgByRoomId(id);
        roomAndRoomCfgDTO.setRoomName(roomIndex.getRoomName());
        roomAndRoomCfgDTO.setId(id);
        roomAndRoomCfgDTO.setPowerCapacity(roomIndex.getPowerCapacity());
        roomAndRoomCfgDTO.setAirPower(roomIndex.getAirPower());
        roomAndRoomCfgDTO.setEleAlarmDay(roomCfg.getEleAlarmDay());
        roomAndRoomCfgDTO.setEleAlarmMonth(roomCfg.getEleAlarmMonth());
        roomAndRoomCfgDTO.setEleLimitDay(roomCfg.getEleLimitDay());
        roomAndRoomCfgDTO.setEleLimitMonth(roomCfg.getEleLimitMonth());
        roomAndRoomCfgDTO.setXLength(roomIndex.getXLength());
        roomAndRoomCfgDTO.setYLength(roomIndex.getYLength());
        return roomAndRoomCfgDTO;
    }

    /**
     * 机房柜列新增/编辑
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer roomCabinetSave(CabinetSaveVo vo) {
        try {
            CabinetIndex index = new CabinetIndex();
            index.setCabinetName(vo.getCabinetName());
            index.setRoomId(vo.getRoomId());
            index.setPowerCapacity(vo.getPowerCapacity());
            index.setCabinetHeight(vo.getCabinetHeight());
            if (Objects.nonNull(vo.getId())){
                //编辑
                CabinetIndex cabinetIndex = cabinetIndexMapper.selectOne(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getId,vo.getId()));
                if (Objects.nonNull(cabinetIndex)){
                    index.setId(vo.getId());
                    int updateById = cabinetIndexMapper.updateById(index);
                    if (updateById > 0) {
                        cabinetCfgMapper.updateByCabinetCfg(vo);
                    }
                }
            }else {
                int insert = cabinetIndexMapper.insert(index);
                if (insert > 0) {
                    //保存配置
                    CabinetCfg  cfg = new CabinetCfg();
                    cfg.setCabinetId(index.getId());
                    cfg.setXCoordinate(vo.getXCoordinate());
                    cfg.setYCoordinate(vo.getYCoordinate());
                    cfg.setEleLimitDay(vo.getEleLimitDay());
                    cfg.setEleAlarmDay(vo.getEleAlarmDay());
                    cfg.setEleLimitMonth(vo.getEleLimitMonth());
                    cfg.setEleAlarmMonth(vo.getEleAlarmMonth());
                    cabinetCfgMapper.insert(cfg);
                }
            }
            return index.getId();
        }finally {
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }
    }

    /**
     * 获取柜列详情
     * @param roomId
     * @return
     */
    public List<AisleDetailDTO> getAisleDetail(Integer roomId,String roomName,AtomicInteger totalSpace, AtomicInteger totalUsedSpace) {
        List<AisleDetailDTO> detailDTOList = new ArrayList<>();

//        List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
//                .eq(AisleIndex::getRoomId,roomId)
//                .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
        //查询柜列加柜列配置
        List<AisleIndexVo> aisleIndexList = aisleIndexMapper.selectAisleIndexByCfgList(roomId);
        //机柜
        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                .eq(CabinetIndex::getRoomId,roomId)
                .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus()));
        Map<Integer,List<CabinetIndex>> cabinetIndexMap = new HashMap<>();
        Map<Integer,CabinetCfg> cabinetCfgMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(cabinetIndexList)){
            List<Integer> cabinetIds = cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList());
            cabinetIndexMap.putAll(cabinetIndexList.stream().collect(Collectors.groupingBy(CabinetIndex::getAisleId)));
            List<CabinetCfg> cabinetCfgList = cfgDoMapper.selectList(new LambdaQueryWrapper<CabinetCfg>()
                    .in(CabinetCfg::getCabinetId,cabinetIds));
            if (!CollectionUtils.isEmpty(cabinetCfgList)){
                cabinetCfgMap.putAll(cabinetCfgList.stream().collect(Collectors.toMap(CabinetCfg::getCabinetId,Function.identity())));
            }
        }


        if (!CollectionUtils.isEmpty(aisleIndexList)){
            aisleIndexList.forEach(aisleIndex -> {
                AisleDetailDTO detailDTO = new AisleDetailDTO();
                detailDTO.setAisleName(aisleIndex.getAisleName());
                detailDTO.setId(aisleIndex.getId());
                detailDTO.setLength(aisleIndex.getAisleLength());
                //detailDTO.setType(aisleIndex.getType());
                detailDTO.setPduBar(aisleIndex.getPduBar());
                detailDTO.setRoomName(roomName);
                detailDTO.setRoomId(roomId);
                detailDTO.setPowerCapacity(aisleIndex.getPowerCapacity());
                detailDTO.setEleAlarmDay(aisleIndex.getEleAlarmDay());
                detailDTO.setEleAlarmMonth(aisleIndex.getEleAlarmMonth());
                detailDTO.setEleLimitDay(aisleIndex.getEleLimitDay());
                detailDTO.setEleLimitMonth(aisleIndex.getEleLimitMonth());
                detailDTO.setDirection(aisleIndex.getDirection());
                detailDTO.setXCoordinate(aisleIndex.getXCoordinate());
                detailDTO.setYCoordinate(aisleIndex.getYCoordinate());
                List<CabinetDTO> aisleCabinetDTOList = new ArrayList<>();
                Map<Integer,CabinetDTO> cabMap = new HashMap<>();
                List<CabinetIndex> cabs = cabinetIndexMap.get(aisleIndex.getId());
                if (!CollectionUtils.isEmpty(cabs)){
                    cabs.forEach(cabinetIndex ->{
                        CabinetDTO cabinetDTO = BeanUtils.toBean(cabinetIndex,CabinetDTO.class);
                        CabinetCfg cfg = cabinetCfgMap.get(cabinetIndex.getId());
                        if (Objects.nonNull(cfg)){
                            //cabinetDTO.setCabinetName(cfg.getCabinetName());
                            //cabinetDTO.setCabinetHeight(cfg.getCabinetHeight());
                            cabinetDTO.setCompany(cfg.getCompany());
                            cabinetDTO.setXCoordinate(cfg.getXCoordinate());
                            cabinetDTO.setYCoordinate(cfg.getYCoordinate());
                            //cabinetDTO.setType(cfg.getType());
                            if ("x".equals(aisleIndex.getDirection())){
                                //横向
                                cabinetDTO.setIndex(cfg.getXCoordinate() - aisleIndex.getXCoordinate() + 1);
                            }
                            if ("y".equals(aisleIndex.getDirection())){
                                //纵向
                                cabinetDTO.setIndex(cfg.getYCoordinate() - aisleIndex.getYCoordinate() + 1);
                            }

                            //totalSpace.addAndGet(cfg.getCabinetHeight());
                        }

                        List<RackIndex> rackIndexList = rackIndexDoMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                                .eq(RackIndex::getCabinetId,cabinetIndex.getId())
                                .eq(RackIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
                        if (!CollectionUtils.isEmpty(rackIndexList)){
                            int usedSpace = rackIndexList.stream().map(RackIndex::getUHeight).reduce(0,Integer::sum);
                            int freeSpace = cabinetDTO.getCabinetHeight() - usedSpace;
                            cabinetDTO.setUsedSpace(usedSpace);
                            cabinetDTO.setFreeSpace(freeSpace);
                            totalUsedSpace.addAndGet(usedSpace);
                        }else {
                            cabinetDTO.setFreeSpace(cabinetDTO.getCabinetHeight());
                        }


                        cabMap.put(cabinetDTO.getIndex(),cabinetDTO);

                    });
                }

                for (int i = 0; i< aisleIndex.getAisleLength();i ++ ){
                    CabinetDTO cabinetDTO = cabMap.get(i+1);
                    if (Objects.isNull(cabinetDTO)){

                        cabinetDTO = new CabinetDTO();
                        cabinetDTO.setIndex(i+1);
                    }
                    aisleCabinetDTOList.add(i,cabinetDTO);
                }

                detailDTO.setCabinetList(aisleCabinetDTOList);
                detailDTOList.add(detailDTO);
            });
        }

        return detailDTOList;
    }


    /**
     * 柜列新增/修改
     * @param aisleSaveVo
     * @return
     */
    private Integer aisleSave(AisleSaveVo aisleSaveVo) {

        AisleIndex index = new AisleIndex();
        index.setAisleName(aisleSaveVo.getAisleName());
        index.setAisleLength(aisleSaveVo.getAisleLength());
        index.setRoomId(aisleSaveVo.getRoomId());
        //index.setType(aisleSaveVo.getType());
        index.setPduBar(aisleSaveVo.getPduBar());
//        index.setEleAlarmDay(aisleSaveVo.getEleAlarmDay());
//        index.setEleAlarmMonth(aisleSaveVo.getEleAlarmMonth());
//        index.setEleLimitDay(aisleSaveVo.getEleLimitDay());
//        index.setEleLimitMonth(aisleSaveVo.getEleLimitMonth());
        index.setDirection(aisleSaveVo.getDirection());
        index.setXCoordinate(aisleSaveVo.getXCoordinate());
        index.setYCoordinate(aisleSaveVo.getYCoordinate());

        Integer roomId = aisleSaveVo.getRoomId();
        RoomIndex roomIndex = roomIndexMapper.selectById(roomId);
        if (Objects.nonNull(roomIndex)){
            if (StringUtils.isNotEmpty(aisleSaveVo.getDirection())
                    && "x".equals(aisleSaveVo.getDirection())){
                //横向
                if (aisleSaveVo.getXCoordinate() + aisleSaveVo.getAisleLength()>roomIndex.getXLength() + 1){
                    throw new RuntimeException("柜列长度超出");
                }
            }
            if ( StringUtils.isNotEmpty(aisleSaveVo.getDirection())
                    && "y".equals(aisleSaveVo.getDirection())){
                //纵向
                if (aisleSaveVo.getYCoordinate() + aisleSaveVo.getAisleLength()>roomIndex.getYLength() + 1){
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
                    //柜列位置变动修改
                    if (aisleSaveVo.getXCoordinate() != aisleIndex.getXCoordinate()
                            || aisleSaveVo.getYCoordinate() != aisleIndex.getYCoordinate()
                            || !aisleSaveVo.getDirection().equals(aisleIndex.getDirection()) ){
                        //修改柜列下机柜
                        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                                .eq(CabinetIndex::getAisleId,aisleIndex.getId())
                                .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                                .eq(CabinetIndex::getIsDisabled,DisableEnums.ENABLE.getStatus()));

                        if (!CollectionUtils.isEmpty(cabinetIndexList)){
                            List<Integer> cabinetIds = cabinetIndexList.stream().map(CabinetIndex::getId).collect(Collectors.toList());
                            List<CabinetCfg> cabinetCfgList = cfgDoMapper.selectList(new LambdaQueryWrapper<CabinetCfg>()
                                    .in(CabinetCfg::getCabinetId,cabinetIds));
                            Map<Integer,CabinetCfg> cfgMap = cabinetCfgList.stream().collect(Collectors.toMap(CabinetCfg::getCabinetId,Function.identity()));
                            Map<Integer,Integer>  indexMap = new HashMap<>();
                            if ("x".equals(aisleIndex.getDirection())){
                                //横向  计算机柜位置
                                cabinetIds.forEach(id -> {
                                    Integer i = cfgMap.get(id).getXCoordinate()-aisleIndex.getXCoordinate();
                                    indexMap.put(id,i);
                                });
                            }
                            if ("y".equals(aisleIndex.getDirection())){
                                //纵向  计算机柜位置
                                cabinetIds.forEach(id -> {
                                    Integer i = cfgMap.get(id).getYCoordinate()-aisleIndex.getYCoordinate();
                                    indexMap.put(id,i);
                                });
                            }

                            //修改
                            cabinetCfgList.forEach(cabinetCfg ->{
                                int x = 0;
                                int y = 0;
                                if ("x".equals(aisleSaveVo.getDirection())){
                                    //横向  计算机柜位置
                                    x = aisleSaveVo.getXCoordinate() + indexMap.get(cabinetCfg.getCabinetId());
                                    y = aisleSaveVo.getYCoordinate();
                                }
                                if ("y".equals(aisleSaveVo.getDirection())){
                                    //纵向  计算机柜位置
                                    y = aisleSaveVo.getYCoordinate() + indexMap.get(cabinetCfg.getCabinetId());
                                    x = aisleSaveVo.getXCoordinate();
                                }
                                cabinetCfg.setYCoordinate(y);
                                cabinetCfg.setXCoordinate(x);
                                cfgDoMapper.updateById(cabinetCfg);
                            });
                        }

                    }
            }

        }else {
            //新增
            aisleIndexMapper.insert(index);
        }
        return index.getId();
    }

    /**
     * 保存机柜
     * @param vo
     * @throws Exception
     */
    public void saveCabinet(CabinetVo vo) {


        CabinetIndex index;
        //编辑
        if (vo.getId() > 0) {
            //index 索引表
            index = cabinetIndexMapper.selectById(vo.getId());

            //修改
            cabinetIndexMapper.updateById( convertIndex(vo, index));
        } else {
            //新增
            //判断机柜名称是否重复（已删除的或者已禁用的恢复）
            index = cabinetIndexMapper.selectOne(new LambdaQueryWrapper<CabinetIndex>()
                    .eq(CabinetIndex::getCabinetName, vo.getCabinetName())
                    .eq(CabinetIndex::getRoomId, vo.getRoomId()));
            if (Objects.nonNull(index)) {
                if (index.getIsDeleted() == DelFlagEnums.DELETE.getStatus() || index.getIsDisabled() == DisableFlagEnums.DISABLE.getStatus()) {
                    //index 索引表
                    //修改
                    cabinetIndexMapper.updateById(convertIndex(vo, index));
                }

            } else {
                index = new CabinetIndex();
                //index 索引表
                //新增
                CabinetIndex cabinetIndex = convertIndex(vo, index);
                cabinetIndexMapper.insert(cabinetIndex);
                vo.setId(cabinetIndex.getId());
            }
        }

        log.info("vo : " + vo);

        //配置表
        CabinetCfg cfg = cfgDoMapper.selectOne(new LambdaQueryWrapper<CabinetCfg>()
                .eq(CabinetCfg::getCabinetId, vo.getId()));
        if (Objects.nonNull(cfg)) {
            //修改
            cfgDoMapper.updateById(convertCfg(vo, cfg));
        } else {
            cfg = new CabinetCfg();

            //新增
            cfgDoMapper.insert(convertCfg(vo, cfg));
        }
    }
    /**
     * 实体转换
     *
     * @param vo
     * @param index
     * @return
     */
    private CabinetIndex convertIndex(CabinetVo vo, CabinetIndex index) {
        CabinetIndex cabinetIndex = new CabinetIndex();
        cabinetIndex.setAisleId(vo.getAisleId());
        cabinetIndex.setCabinetName(vo.getCabinetName());
        cabinetIndex.setPduBox(vo.getPduBox());
        //未删除
        cabinetIndex.setIsDeleted(DelFlagEnums.NO_DEL.getStatus());
        //未禁用
        cabinetIndex.setIsDisabled(DisableFlagEnums.ENABLE.getStatus());
        cabinetIndex.setPowerCapacity(vo.getPowCapacity());
        cabinetIndex.setRoomId(vo.getRoomId());
        cabinetIndex.setId(index.getId());
//        cabinetIndex.setEleAlarmDay(vo.getEleAlarmDay());
//        cabinetIndex.setEleAlarmMonth(vo.getEleAlarmMonth());
//        cabinetIndex.setEleLimitDay(vo.getEleLimitDay());
//        cabinetIndex.setEleLimitMonth(vo.getEleLimitMonth());
        return cabinetIndex;
    }


    /**
     * 实体转换
     *
     * @param vo
     * @param cfg
     * @return
     */
    private CabinetCfg convertCfg(CabinetVo vo, CabinetCfg cfg) {
        CabinetCfg cabinetCfg = new CabinetCfg();
        cabinetCfg.setCabinetId(vo.getId());
        //cabinetCfg.setCabinetHeight(vo.getCabinetHeight());
        //cabinetCfg.setCabinetName(vo.getCabinetName());
        cabinetCfg.setCompany(vo.getCompany());
        //cabinetCfg.setType(vo.getType());
        cabinetCfg.setXCoordinate(vo.getXCoordinate());
        cabinetCfg.setYCoordinate(vo.getYCoordinate());
        cabinetCfg.setId(cfg.getId());
        return cabinetCfg;
    }

    /**
     * 获取日环比
     *
     * @param id
     * @param eqDataDTO
     * @return
     */
    private void getDayChain(int id, RoomEqDataDTO eqDataDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //日环比
        //今日
        startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double todayEq = getDayEq(startTime, endTime, id);
        eqDataDTO.setTodayEq(todayEq);
        //昨日
        List<String> list = getData(startTime, endTime, id, ROOM_EQ_TOTAL_DAY);
        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            RoomEqTotalDayDo dayDo = JsonUtils.parseObject(str, RoomEqTotalDayDo.class);
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
    private void getWeekChain(int id, RoomEqDataDTO eqDataDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //周环比
        //本周
        startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double thisWeekEq = getDayEq(startTime, endTime, id);
        eqDataDTO.setThisWeekEq(thisWeekEq);

        List<String> list = getData(startTime, endTime, id, ROOM_EQ_TOTAL_WEEK);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            RoomEqTotalWeekDo weekDo = JsonUtils.parseObject(str, RoomEqTotalWeekDo.class);
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
    private void getMonthChain(int id, RoomEqDataDTO eqDataDTO) throws IOException {
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
            RoomEqTotalMonthDo monthDo = JsonUtils.parseObject(str, RoomEqTotalMonthDo.class);
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
        RoomEleTotalRealtimeDo endRealtimeDo = getEleData(startTime, endTime,
                SortOrder.DESC,
                ROOM_ELE_TOTAL_REALTIME, id);
        RoomEleTotalRealtimeDo startRealtimeDo = getEleData(startTime, endTime,
                SortOrder.ASC,
                ROOM_ELE_TOTAL_REALTIME, id);
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
    private RoomEleTotalRealtimeDo getEleData(String startTime, String endTime, SortOrder sortOrder, String index, int id) {
        RoomEleTotalRealtimeDo realtimeDo = new RoomEleTotalRealtimeDo();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                    .must(QueryBuilders.termQuery(ROOM_ID, id))));

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
                realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), RoomEleTotalRealtimeDo.class);
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
                .must(QueryBuilders.termQuery(ROOM_ID, id))));
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
