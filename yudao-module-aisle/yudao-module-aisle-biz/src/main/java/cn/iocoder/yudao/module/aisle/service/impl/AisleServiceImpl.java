package cn.iocoder.yudao.module.aisle.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleBarDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleBoxDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleDetailDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.oulet.BoxEqOutletDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBox;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.enums.DisableEnums;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.aisle.dto.AisleDataDTO;
import cn.iocoder.yudao.module.aisle.dto.BoxDetailDataDTO;
import cn.iocoder.yudao.module.aisle.dto.BusDetailDataDTO;
import cn.iocoder.yudao.module.aisle.dto.CabinetDetailDataDTO;
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
import java.util.function.Function;
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
    AisleCfgMapper aisleCfgMapper;

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

    @Value("${aisle-refresh-url}")
    public String adder;

    @Resource
    CabinetApi cabinetApi;

    @Autowired
    private RestHighLevelClient client;

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

            if (Objects.nonNull(aisleSaveVo.getId())){
                //编辑
                AisleIndex aisleIndex = aisleIndexMapper.selectOne(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getId,aisleSaveVo.getId()));
                if (Objects.nonNull(aisleIndex)){
                    index.setId(aisleSaveVo.getId());
                    aisleIndexMapper.updateById(index);

                    //修改配置表
                    AisleCfg aisleCfg = aisleCfgMapper.selectOne(new LambdaQueryWrapper<AisleCfg>()
                            .eq(AisleCfg::getAisleId,aisleIndex.getId()));
                    AisleCfg cfg = new AisleCfg();
                    cfg.setAisleId(aisleIndex.getId());
                    cfg.setDirection(aisleSaveVo.getDirection());
                    cfg.setXCoordinate(aisleSaveVo.getXCoordinate());
                    cfg.setYCoordinate(aisleSaveVo.getYCoordinate());

                    if (Objects.nonNull(aisleCfg)){
                        //修改
                        cfg.setId(aisleCfg.getId());
                        aisleCfgMapper.updateById(cfg);
                    }else {
                        aisleCfgMapper.insert(cfg);
                    }
                }

            }else {
                //新增
                aisleIndexMapper.insert(index);
                AisleCfg cfg = new AisleCfg();
                cfg.setAisleId(index.getId());
                cfg.setDirection(aisleSaveVo.getDirection());
                cfg.setXCoordinate(aisleSaveVo.getXCoordinate());
                cfg.setYCoordinate(aisleSaveVo.getYCoordinate());
                aisleCfgMapper.insert(cfg);
            }
            //母线信息
            List<AisleBar> aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                    .eq(AisleBar::getAisleId,index.getId()));
            List<AisleBarDTO> barVos = new ArrayList<>();
            if (Objects.nonNull(aisleSaveVo.getBarA())){
                barVos.add(aisleSaveVo.getBarA());
            }
            if (Objects.nonNull(aisleSaveVo.getBarB())){
                barVos.add(aisleSaveVo.getBarB());
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
            //机柜信息
            if (!CollectionUtils.isEmpty(aisleSaveVo.getCabinetList())){
                //删除
                List<Integer> ids =  aisleSaveVo.getCabinetList().stream().map(CabinetVo::getId).filter(id -> id >0).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(ids)){
                    cabinetIndexMapper.update(new LambdaUpdateWrapper<CabinetIndex>()
                            .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                            .eq(CabinetIndex::getRoomId,aisleSaveVo.getRoomId())
                            .eq(CabinetIndex::getAisleId,index.getId())
                            .notIn(CabinetIndex::getId,ids)
                            .set(CabinetIndex::getIsDeleted,DelEnums.DELETE.getStatus()));
                }else {
                    cabinetIndexMapper.update(new LambdaUpdateWrapper<CabinetIndex>()
                            .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                            .eq(CabinetIndex::getRoomId,aisleSaveVo.getRoomId())
                            .eq(CabinetIndex::getAisleId,index.getId())
                            .set(CabinetIndex::getIsDeleted,DelEnums.DELETE.getStatus()));
                }

                //新增/保存
                aisleSaveVo.getCabinetList().forEach(cabinetVo -> {
                    cabinetVo.setRoomId(aisleSaveVo.getRoomId());
                    cabinetVo.setAisleId(index.getId());
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
                cabinetIndexMapper.update(new LambdaUpdateWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                        .eq(CabinetIndex::getRoomId,aisleSaveVo.getRoomId())
                        .eq(CabinetIndex::getAisleId,index.getId())
                        .set(CabinetIndex::getIsDeleted,DelEnums.DELETE.getStatus()));
            }

            return index.getId();
        }finally {
            //刷新柜列计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }

    }



    private Integer aisleSave2(AisleSaveVo aisleSaveVo) {

        try {
            AisleIndex index = new AisleIndex();
            index.setName(aisleSaveVo.getAisleName());
            index.setLength(aisleSaveVo.getLength());
            index.setRoomId(aisleSaveVo.getRoomId());
            index.setType(aisleSaveVo.getType());
            index.setPduBar(aisleSaveVo.getPduBar());

            if (Objects.nonNull(aisleSaveVo.getId())){
                //编辑
                AisleIndex aisleIndex = aisleIndexMapper.selectOne(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getId,aisleSaveVo.getId()));
                if (Objects.nonNull(aisleIndex)){
                    index.setId(aisleSaveVo.getId());
                    aisleIndexMapper.updateById(index);

                    //修改配置表
                    AisleCfg aisleCfg = aisleCfgMapper.selectOne(new LambdaQueryWrapper<AisleCfg>()
                            .eq(AisleCfg::getAisleId,aisleIndex.getId()));
                    AisleCfg cfg = new AisleCfg();
                    cfg.setAisleId(aisleIndex.getId());
                    cfg.setDirection(aisleSaveVo.getDirection());
                    cfg.setXCoordinate(aisleSaveVo.getXCoordinate());
                    cfg.setYCoordinate(aisleSaveVo.getYCoordinate());

                    if (Objects.nonNull(aisleCfg)){
                        //修改
                        cfg.setId(aisleCfg.getId());
                        aisleCfgMapper.updateById(cfg);
                        //柜列位置变动修改
                        if (aisleSaveVo.getXCoordinate() != aisleCfg.getXCoordinate()
                                || aisleSaveVo.getYCoordinate() != aisleCfg.getYCoordinate()
                                || !aisleSaveVo.getDirection().equals(aisleCfg.getDirection()) ){
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
                                if ("x".equals(aisleCfg.getDirection())){
                                    //横向  计算机柜位置
                                    cabinetIds.forEach(id -> {
                                        Integer i = cfgMap.get(id).getXCoordinate()-aisleCfg.getXCoordinate();
                                        indexMap.put(id,i);
                                    });
                                }
                                if ("y".equals(aisleCfg.getDirection())){
                                    //纵向  计算机柜位置
                                    cabinetIds.forEach(id -> {
                                        Integer i = cfgMap.get(id).getYCoordinate()-aisleCfg.getYCoordinate();
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

                    }else {
                        aisleCfgMapper.insert(cfg);
                    }
                }

            }else {
                //新增
                aisleIndexMapper.insert(index);
                AisleCfg cfg = new AisleCfg();
                cfg.setAisleId(index.getId());
                cfg.setDirection(aisleSaveVo.getDirection());
                cfg.setXCoordinate(aisleSaveVo.getXCoordinate());
                cfg.setYCoordinate(aisleSaveVo.getYCoordinate());
                aisleCfgMapper.insert(cfg);
            }
            return index.getId();
        }finally {
            //刷新柜列计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void aisleBusSave(AisleBusSaveVo busSaveVo) {

        try {
            Integer aisleId = busSaveVo.getAisleId();

            if (!CollectionUtils.isEmpty(busSaveVo.getBarVos())){
                //绑定始端箱
                List<AisleBarDTO> barVos = busSaveVo.getBarVos();
                barVos.forEach(barVo -> {
                    AisleBar  bar = BeanUtils.toBean(barVo,AisleBar.class);
                    bar.setAisleId(aisleId);
                    bar.setBarKey(barVo.getDevIp() + SPLIT_KEY_BUS + barVo.getBusName());
                    AisleBar aisleBar = aisleBarMapper.selectById(bar.getId());
                    if (Objects.nonNull(aisleBar)){
                        aisleBarMapper.updateById(bar);
                    }else {
                        aisleBarMapper.insert(bar);
                    }

                    List<AisleBoxDTO> boxList = barVo.getBoxList();
                    if (!CollectionUtils.isEmpty(boxList)){
                        boxList.forEach(boxDTO ->{
                            AisleBox box = BeanUtils.toBean(boxDTO,AisleBox.class);
                            box.setAisleId(aisleId);
                            box.setAisleBarId(bar.getId());
                            box.setBarKey(bar.getBarKey() + SPLIT_KEY_BUS + box.getBoxName());
                            AisleBox aisleBox = aisleBoxMapper.selectById(box.getId());
                            if (Objects.nonNull(aisleBox)){
                                aisleBoxMapper.updateById(box);

                            }else {
                                aisleBoxMapper.insert(box);
                            }
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
                //逻辑删除
                if (aisleIndex.getIsDelete().equals(DelEnums.NO_DEL.getStatus())){
                    aisleIndexMapper.update(new LambdaUpdateWrapper<AisleIndex>()
                            .eq(AisleIndex::getId, aisleId)
                            .set(AisleIndex::getIsDelete, DelEnums.DELETE.getStatus()));

                }else {
                    //物理删除
                    //1.删除绑定关系
                    aisleBoxMapper.delete(new LambdaQueryWrapper<AisleBox>()
                            .eq(AisleBox::getAisleId,aisleId));
                    aisleBarMapper.delete(new LambdaQueryWrapper<AisleBar>()
                            .eq(AisleBar::getAisleId,aisleId));
                    //2.删除配置
                    aisleCfgMapper.delete(new LambdaQueryWrapper<AisleCfg>()
                            .eq(AisleCfg::getAisleId,aisleId));
                    //3.删除柜列
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

        AisleCfg aisleCfg = aisleCfgMapper.selectOne(new LambdaQueryWrapper<AisleCfg>()
                .eq(AisleCfg::getAisleId,aisleId));

        if (Objects.nonNull(aisleIndex)){
            detailDTO.setAisleName(aisleIndex.getName());
            detailDTO.setId(aisleId);
            detailDTO.setLength(aisleIndex.getLength());
            detailDTO.setType(aisleIndex.getType());
            detailDTO.setPduBar(aisleIndex.getPduBar());
            Integer roomId = aisleIndex.getRoomId();
            RoomIndex roomIndex = roomIndexMapper.selectById(roomId);
            if (Objects.nonNull(roomIndex)){
                detailDTO.setRoomName(roomIndex.getName());
                detailDTO.setRoomId(roomId);
            }

        }
        if (Objects.nonNull(aisleCfg)){
            detailDTO.setDirection(aisleCfg.getDirection());
            detailDTO.setXCoordinate(aisleCfg.getXCoordinate());
            detailDTO.setYCoordinate(aisleCfg.getYCoordinate());
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
               if ("x".equals(aisleCfg.getDirection())){
                   //横向
                   cabinetDTO.setIndex(cabinetDTO.getXCoordinate() - aisleCfg.getXCoordinate() + 1);
               }
               if ("y".equals(aisleCfg.getDirection())){
                   //纵向
                   cabinetDTO.setIndex(cabinetDTO.getYCoordinate() - aisleCfg.getYCoordinate() + 1);
               }
               cabinetDTO.setYesterdayEq(yesterdayMap.getOrDefault(cabinetIndex.getId(), 0.0));
               aisleCabinetDTOList.add(cabinetDTO);
           });
       }

       detailDTO.setCabinetList(aisleCabinetDTOList.stream().sorted(Comparator.comparing(CabinetDTO::getIndex)).collect(Collectors.toList()));
       return detailDTO;
    }

    @Override
    public List<AisleIndex> getAisleList(int roomId) {
        return aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                .eq(AisleIndex::getRoomId,roomId)
                .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
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
                        cabDto.setTemData(envData.getJSONObject(TEM_VALUE));
                    }
                }
               cabList.add(cabDto);
            });
            detailDTO.setCabinetList(cabList);
        }


        return detailDTO;
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
        builder.sort(idStr, SortOrder.ASC);
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
