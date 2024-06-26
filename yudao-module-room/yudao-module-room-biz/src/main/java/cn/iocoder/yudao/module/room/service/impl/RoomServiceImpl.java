package cn.iocoder.yudao.module.room.service.impl;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleBarDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleCabinetDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleDetailDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBox;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.*;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.enums.DisableEnums;
import cn.iocoder.yudao.framework.common.enums.PduBoxEnums;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.room.dto.RoomDetailDTO;
import cn.iocoder.yudao.module.room.service.RoomService;
import cn.iocoder.yudao.module.room.vo.RoomSaveVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.REDIS_KEY_AISLE;

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
    @Resource
    RoomCfgMapper roomCfgMapper;

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
    CabinetIndexMapper cabinetIndexMapper;
    @Resource
    CabinetCfgMapper cabinetCfgMapper;


    /**
     * 机房保存
     * @param roomSaveVo 保存参数
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer roomSave(RoomSaveVo roomSaveVo) {

        RoomIndex index = new RoomIndex();
        index.setName(roomSaveVo.getRoomName());
        index.setPowerCapacity(roomSaveVo.getPowCapacity());

        if (Objects.nonNull(roomSaveVo.getId())){
            //编辑
            RoomIndex roomIndex = roomIndexMapper.selectOne(new LambdaQueryWrapper<RoomIndex>()
                    .eq(RoomIndex::getId,roomSaveVo.getId()));
            if (Objects.nonNull(roomIndex)){
                index.setId(roomSaveVo.getId());
                roomIndexMapper.updateById(index);

                //修改配置表
                RoomCfg roomCfg = roomCfgMapper.selectOne(new LambdaQueryWrapper<RoomCfg>()
                        .eq(RoomCfg::getRoomId,roomIndex.getId()));
                RoomCfg cfg = new RoomCfg();
                cfg.setRoomId(roomIndex.getId());
                cfg.setYLength(roomSaveVo.getYLength());
                cfg.setXLength(roomSaveVo.getXLength());

                if (Objects.nonNull(roomCfg)){
                    //修改
                    cfg.setId(roomCfg.getId());
                    roomCfgMapper.updateById(cfg);
                }else {
                    roomCfgMapper.insert(cfg);
                }
            }

        }else {
            //新增
            roomIndexMapper.insert(index);
            RoomCfg cfg = new RoomCfg();
            cfg.setRoomId(index.getId());
            cfg.setYLength(roomSaveVo.getYLength());
            cfg.setXLength(roomSaveVo.getXLength());
            roomCfgMapper.insert(cfg);
        }

        //柜列
        if (!CollectionUtils.isEmpty(roomSaveVo.getAisleList())){
            roomSaveVo.getAisleList().forEach(aisleSaveVo -> {
                aisleSaveVo.setRoomId(index.getId());
                aisleSave(aisleSaveVo);
            });
        }

        //机柜
        if (!CollectionUtils.isEmpty(roomSaveVo.getCabinetList())){
            roomSaveVo.getCabinetList().forEach(cabinetVo -> {
                cabinetVo.setRoomId(index.getId());
                saveCabinet(cabinetVo);
            });
        }

        return index.getId();
    }

    @Override
    public void deleteRoom(Integer roomId) {

        //删除机房
        RoomIndex roomIndex = roomIndexMapper.selectById(roomId);
        if (Objects.nonNull(roomIndex)){
            //逻辑删除
            if (roomIndex.getIsDelete() == (DelEnums.NO_DEL.getStatus())){
                roomIndexMapper.update(new LambdaUpdateWrapper<RoomIndex>()
                        .eq(RoomIndex::getId, roomId)
                        .set(RoomIndex::getIsDelete, DelEnums.DELETE.getStatus()));

            }else {
                //物理删除
                //删除配置
                roomCfgMapper.delete(new LambdaQueryWrapper<RoomCfg>()
                        .eq(RoomCfg::getRoomId,roomId));
                //删除机房
                roomIndexMapper.deleteById(roomId);
            }
        }
        //删除key
//        String key = REDIS_KEY_AISLE + aisleId;
//
//        boolean flag = redisTemplate.delete(key);
//        log.info("key: " + key + " flag : " + flag);


    }

    @Override
    public RoomDetailDTO getDetail(Integer roomId) {
        RoomDetailDTO roomDetailDTO = new RoomDetailDTO();

        RoomIndex roomIndex = roomIndexMapper.selectById(roomId);
        RoomCfg roomCfg = roomCfgMapper.selectOne(new LambdaQueryWrapper<RoomCfg>()
                .eq(RoomCfg::getRoomId,roomId));
        if (Objects.nonNull(roomIndex) && Objects.nonNull(roomCfg)){
            roomDetailDTO.setRoomName(roomIndex.getName());
            roomDetailDTO.setId(roomId);
            roomDetailDTO.setXLength(roomCfg.getXLength());
            roomDetailDTO.setYLength(roomCfg.getYLength());
            roomDetailDTO.setPowerCapacity(roomIndex.getPowerCapacity());
        }
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
            List<CabinetCfg> cabinetCfgList = cabinetCfgMapper.selectList(new LambdaQueryWrapper<CabinetCfg>()
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
                    cabinetDTO.setCabinetName(cfg.getCabinetName());
                    cabinetDTO.setCabinetHeight(cfg.getCabinetHeight());
                    cabinetDTO.setCompany(cfg.getCompany());
                    cabinetDTO.setXCoordinate(cfg.getXCoordinate());
                    cabinetDTO.setYCoordinate(cfg.getYCoordinate());
                    cabinetDTO.setType(cfg.getType());
                }
                aisleCabinetDTOList.add(cabinetDTO);

            });
        }
        roomDetailDTO.setCabinetList(aisleCabinetDTOList);
        //柜列
        roomDetailDTO.setAisleList(getAisleDetail(roomId,roomIndex.getName()));

        return roomDetailDTO;
    }


    /**
     * 获取柜列详情
     * @param roomId
     * @return
     */
    public List<AisleDetailDTO> getAisleDetail(Integer roomId,String roomName) {
        List<AisleDetailDTO> detailDTOList = new ArrayList<>();

        List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                .eq(AisleIndex::getRoomId,roomId)
                .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));

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
            List<CabinetCfg> cabinetCfgList = cabinetCfgMapper.selectList(new LambdaQueryWrapper<CabinetCfg>()
                    .in(CabinetCfg::getCabinetId,cabinetIds));
            if (!CollectionUtils.isEmpty(cabinetCfgList)){
                cabinetCfgMap.putAll(cabinetCfgList.stream().collect(Collectors.toMap(CabinetCfg::getCabinetId,Function.identity())));
            }
        }


        if (!CollectionUtils.isEmpty(aisleIndexList)){
            List<Integer> aisleIds = aisleIndexList.stream().map(AisleIndex::getId).collect(Collectors.toList());
            List<AisleCfg> aisleCfgList = aisleCfgMapper.selectList(new LambdaQueryWrapper<AisleCfg>()
                    .in(AisleCfg::getAisleId,aisleIds));
            Map<Integer,AisleCfg> aisleCfgMap = aisleCfgList.stream().collect(Collectors.toMap(AisleCfg::getAisleId,Function.identity()));

            aisleIndexList.forEach(aisleIndex -> {
                AisleDetailDTO detailDTO = new AisleDetailDTO();

                detailDTO.setAisleName(aisleIndex.getName());
                detailDTO.setId(aisleIndex.getId());
                detailDTO.setLength(aisleIndex.getLength());
                detailDTO.setType(aisleIndex.getType());
                detailDTO.setPduBar(aisleIndex.getPduBar());
                detailDTO.setRoomName(roomName);
                detailDTO.setRoomId(roomId);

                AisleCfg aisleCfg = aisleCfgMap.get(aisleIndex.getId());
                if (Objects.nonNull(aisleCfg)){
                    detailDTO.setDirection(aisleCfg.getDirection());
                    detailDTO.setXCoordinate(aisleCfg.getXCoordinate());
                    detailDTO.setYCoordinate(aisleCfg.getYCoordinate());
                }


                List<AisleCabinetDTO> aisleCabinetDTOList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(cabinetIndexMap.get(aisleIndex.getId()))){

                    cabinetIndexMap.get(aisleIndex.getId()).forEach(cabinetIndex ->{
                        AisleCabinetDTO cabinetDTO = BeanUtils.toBean(cabinetIndex,AisleCabinetDTO.class);
                        CabinetCfg cfg = cabinetCfgMap.get(cabinetIndex.getId());
                        if (Objects.nonNull(cfg)){
                            cabinetDTO.setCabinetName(cfg.getCabinetName());
                            cabinetDTO.setCabinetHeight(cfg.getCabinetHeight());
                            cabinetDTO.setCompany(cfg.getCompany());
                            cabinetDTO.setXCoordinate(cfg.getXCoordinate());
                            cabinetDTO.setYCoordinate(cfg.getYCoordinate());
                            cabinetDTO.setType(cfg.getType());
                            if ("x".equals(aisleCfg.getDirection())){
                                //横向
                                cabinetDTO.setIndex(cfg.getXCoordinate() - aisleCfg.getXCoordinate() + 1);
                            }
                            if ("y".equals(aisleCfg.getDirection())){
                                //纵向
                                cabinetDTO.setIndex(cfg.getYCoordinate() - aisleCfg.getYCoordinate() + 1);
                            }
                        }
                        aisleCabinetDTOList.add(cabinetDTO);

                    });
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
                            List<CabinetCfg> cabinetCfgList = cabinetCfgMapper.selectList(new LambdaQueryWrapper<CabinetCfg>()
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
                                    y = aisleSaveVo.getYCoordinate() + indexMap.get(cabinetCfg.getId());
                                    x = aisleSaveVo.getXCoordinate();
                                }
                                cabinetCfg.setYCoordinate(y);
                                cabinetCfg.setXCoordinate(x);
                                cabinetCfgMapper.updateById(cabinetCfg);
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
                    .eq(CabinetIndex::getName, vo.getCabinetName())
                    .eq(CabinetIndex::getRoomId, vo.getRoomId()));
            if (Objects.nonNull(index)) {
                if (index.getIsDeleted() == DelEnums.DELETE.getStatus() || index.getIsDisabled() == DisableEnums.DISABLE.getStatus()) {
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
        CabinetCfg cfg = cabinetCfgMapper.selectOne(new LambdaQueryWrapper<CabinetCfg>()
                .eq(CabinetCfg::getCabinetId, vo.getId()));
        if (Objects.nonNull(cfg)) {
            //修改
            cabinetCfgMapper.updateById(convertCfg(vo, cfg));
        } else {
            cfg = new CabinetCfg();

            //新增
            cabinetCfgMapper.insert(convertCfg(vo, cfg));
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
        cabinetIndex.setName(vo.getCabinetName());
        cabinetIndex.setPduBox(vo.getPduBox());
        //未删除
        cabinetIndex.setIsDeleted(DelEnums.NO_DEL.getStatus());
        //未禁用
        cabinetIndex.setIsDisabled(DisableEnums.ENABLE.getStatus());
        cabinetIndex.setPowCapacity(vo.getPowCapacity());
        cabinetIndex.setRoomId(vo.getRoomId());
        cabinetIndex.setId(index.getId());
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
        cabinetCfg.setCabinetHeight(vo.getCabinetHeight());
        cabinetCfg.setCabinetName(vo.getCabinetName());
        cabinetCfg.setCompany(vo.getCompany());
        cabinetCfg.setType(vo.getType());
        cabinetCfg.setXCoordinate(vo.getXCoordinate());
        cabinetCfg.setYCoordinate(vo.getYCoordinate());
        cabinetCfg.setId(cfg.getId());
        return cabinetCfg;
    }

}
