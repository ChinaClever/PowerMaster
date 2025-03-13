package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBox;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduIndexDo;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.enums.DisableEnums;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.dto.room.RoomMenuDTO;
import cn.iocoder.yudao.framework.common.vo.AisleBoxVO;
import cn.iocoder.yudao.framework.common.vo.CabinetIndexBoxResVO;
import cn.iocoder.yudao.framework.common.vo.CabinetPduResVO;
import cn.iocoder.yudao.module.cabinet.dto.RoomPduMenuDTO;
import cn.iocoder.yudao.module.cabinet.mapper.*;
import cn.iocoder.yudao.module.cabinet.enums.MenuTypeEnums;
import cn.iocoder.yudao.module.cabinet.service.RoomMenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.SPLIT;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房操作
 * @date 2024/5/6 9:10
 */
@Slf4j
@Service
public class RoomMenuServiceImpl implements RoomMenuService {

    @Autowired
    RoomIndexMapper roomIndexMapper;
    @Autowired
    CabinetIndexMapper cabinetIndexMapper;
    @Autowired
    AisleIndexMapper aisleIndexMapper;
    @Autowired
    CabinetPduMapper cabinetPduMapper;
    @Autowired
    RackIndexMapper rackIndexMapper;
    @Autowired
    AisleBarMapper aisleBarMapper;
    @Autowired
    AisleBoxMapper aisleBoxMapper;
    @Autowired
    BoxIndexMapper boxIndexMapper;
    @Autowired
    private BusIndexDoMapper busIndexDoMapper;
    @Autowired
    private PduIndexDoMapper pduIndexDoMapper;
    @Override
    public List<RoomIndex> roomList(String name) {

        List<RoomIndex> roomIndexList = new ArrayList<>();

        try {

            roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                    .eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus())
                    .like(StringUtils.isNotEmpty(name), RoomIndex::getRoomName, name));

        } catch (Exception e) {
            log.error("获取机房列表异常：", e);
        }
        return roomIndexList;
    }

    @Override
    public List<RoomMenuDTO> roomMenuList(Integer id) {

        try {

            //获取机柜列表
            List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                    .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus())
                    .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus()));

            //获取柜列
            List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                    .eq(AisleIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));

            //获取机房
            List<RoomIndex> roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                    .eq(Objects.nonNull(id), RoomIndex::getId, id));


            List<RoomMenuDTO> menuDTOS = new ArrayList<>();

            if (!CollectionUtils.isEmpty(roomIndexList)) {
                roomIndexList.forEach(roomIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(roomIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.ROOM.getType());
                    roomMenuDTO.setName(roomIndex.getRoomName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + roomIndex.getId());
                    //父id设置0
                    roomMenuDTO.setParentId(0);
                    roomMenuDTO.setParentType(0);
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleIndexList.forEach(aisleIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(aisleIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.AISLE.getType());
                    roomMenuDTO.setName(aisleIndex.getAisleName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.AISLE.getType()) + SPLIT + aisleIndex.getId());
                    //父id设置机房
                    roomMenuDTO.setParentId(aisleIndex.getRoomId());
                    roomMenuDTO.setParentType(MenuTypeEnums.ROOM.getType());
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(cabinetIndexList)) {
                cabinetIndexList.forEach(cabinetIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(cabinetIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.CABINET.getType());
                    roomMenuDTO.setName(cabinetIndex.getCabinetName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.CABINET.getType()) + SPLIT + cabinetIndex.getId());
                    //父id设置通道/机房
                    roomMenuDTO.setParentId(cabinetIndex.getAisleId() == 0 ? cabinetIndex.getRoomId() : cabinetIndex.getAisleId());
                    roomMenuDTO.setParentType(cabinetIndex.getAisleId() == 0 ? MenuTypeEnums.ROOM.getType() : MenuTypeEnums.AISLE.getType());
                    menuDTOS.add(roomMenuDTO);
                });

            }

            return buildTree(menuDTOS);
        } catch (Exception e) {
            log.error("获取菜单失败：", e);
        }

        return new ArrayList<>();
    }

    @Override
    public List<AisleIndex> aisleList(Integer roomId) {
        List<AisleIndex> aisleIndexList = new ArrayList<>();
        try {

            aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                    .eq(AisleIndex::getIsDelete, DelEnums.NO_DEL.getStatus())
                    .eq(AisleIndex::getRoomId, roomId));

        } catch (Exception e) {
            log.error("获取柜列列表失败：", e);
        }
        return aisleIndexList;
    }

    @Override
    public List<RoomMenuDTO> roomMenuListAll() {
        try {
            List<RoomMenuDTO> menuDTOS = new ArrayList<>();
            //获取机柜列表
            List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                    .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus())
                    .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus()));
            if (CollectionUtils.isEmpty(cabinetIndexList)){
                return menuDTOS;
            }

            List<Integer> aisleIds = cabinetIndexList.stream().map(CabinetIndex::getAisleId).distinct().collect(Collectors.toList());
            List<Integer> roomIds = cabinetIndexList.stream().map(CabinetIndex::getRoomId).distinct().collect(Collectors.toList());
            //获取柜列
            List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                    .in(AisleIndex::getId, aisleIds)
                    .eq(AisleIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));

            //获取机房
            List<RoomIndex> roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                    .in(RoomIndex::getId, roomIds)
                    .eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));




            if (!CollectionUtils.isEmpty(roomIndexList)) {
                roomIndexList.forEach(roomIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(roomIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.ROOM.getType());
                    roomMenuDTO.setName(roomIndex.getRoomName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + roomIndex.getId());
                    //父id设置0
                    roomMenuDTO.setParentId(0);
                    roomMenuDTO.setParentType(0);
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleIndexList.forEach(aisleIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(aisleIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.AISLE.getType());
                    roomMenuDTO.setName(aisleIndex.getAisleName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.AISLE.getType()) + SPLIT + aisleIndex.getId());
                    //父id设置机房
                    roomMenuDTO.setParentId(aisleIndex.getRoomId());
                    roomMenuDTO.setParentType(MenuTypeEnums.ROOM.getType());
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(cabinetIndexList)) {
                cabinetIndexList.forEach(cabinetIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(cabinetIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.CABINET.getType());
                    roomMenuDTO.setName(cabinetIndex.getCabinetName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.CABINET.getType()) + SPLIT + cabinetIndex.getId());
                    //父id设置通道/机房
                    roomMenuDTO.setParentId(cabinetIndex.getAisleId() == 0 ? cabinetIndex.getRoomId() : cabinetIndex.getAisleId());
                    roomMenuDTO.setParentType(cabinetIndex.getAisleId() == 0 ? MenuTypeEnums.ROOM.getType() : MenuTypeEnums.AISLE.getType());
                    menuDTOS.add(roomMenuDTO);
                });

            }

            return buildTree(menuDTOS);
        } catch (Exception e) {
            log.error("获取菜单失败：", e);
        }

        return new ArrayList<>();
    }

    @Override
    public List<RoomPduMenuDTO> roomPduMenuList() {
        try {
            List<CabinetPduResVO> cabinetIndexList = cabinetIndexMapper.selectListAndPdu();
            List<AisleIndex> aisleIndexList = new ArrayList<>();
            List<RoomIndex> roomIndexList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(cabinetIndexList)) {
                List<Integer> roomIds = cabinetIndexList.stream().map(CabinetPduResVO::getRoomId).distinct().collect(Collectors.toList());

                List<Integer> aisleIds = cabinetIndexList.stream().map(CabinetPduResVO::getAisleId).collect(Collectors.toList());
                //获取柜列
                aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getIsDelete, DelEnums.NO_DEL.getStatus())
                        .in(AisleIndex::getId, aisleIds));

                roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                        .eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus()).in(RoomIndex::getId, roomIds));
            }
            List<RoomPduMenuDTO> menuDTOS = new ArrayList<>();

            List<String> pduKeys = new ArrayList<>();
            if (!CollectionUtils.isEmpty(roomIndexList)) {
                roomIndexList.forEach(roomIndex -> {
                    RoomPduMenuDTO roomMenuDTO = new RoomPduMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(roomIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.ROOM.getType());
                    roomMenuDTO.setName(roomIndex.getRoomName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + roomIndex.getId());
                    //父id设置0
                    roomMenuDTO.setParentId(0);
                    roomMenuDTO.setParentType(0);
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleIndexList.forEach(aisleIndex -> {
                    RoomPduMenuDTO roomMenuDTO = new RoomPduMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(aisleIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.AISLE.getType());
                    roomMenuDTO.setName(aisleIndex.getAisleName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.AISLE.getType()) + SPLIT + aisleIndex.getId());
                    //父id设置机房
                    roomMenuDTO.setParentId(aisleIndex.getRoomId());
                    roomMenuDTO.setParentType(MenuTypeEnums.ROOM.getType());
                    menuDTOS.add(roomMenuDTO);
                });

            }


            if (!CollectionUtils.isEmpty(cabinetIndexList)) {

                cabinetIndexList.forEach(cabinetIndex -> {
                    RoomPduMenuDTO roomMenuDTO = new RoomPduMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(cabinetIndex.getCabinetId());
                    roomMenuDTO.setType(MenuTypeEnums.CABINET.getType());
                    roomMenuDTO.setName(cabinetIndex.getCabinetName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.CABINET.getType()) + SPLIT + cabinetIndex.getCabinetId());
                    //父id设置通道/机房
                    roomMenuDTO.setParentId(cabinetIndex.getAisleId() == 0 ? cabinetIndex.getRoomId() : cabinetIndex.getAisleId());
                    roomMenuDTO.setParentType(cabinetIndex.getAisleId() == 0 ? MenuTypeEnums.ROOM.getType() : MenuTypeEnums.AISLE.getType());
                    menuDTOS.add(roomMenuDTO);
                });

                //Map<String, CabinetPduResVO> pduAmap = cabinetIndexList.stream().collect(Collectors.toMap(CabinetPduResVO::getPduKeyA, Function.identity()));
                if (!CollectionUtils.isEmpty(cabinetIndexList)){
                    pduKeys = cabinetIndexList.stream().map(CabinetPduResVO::getPduKeyA).collect(Collectors.toList());
                    List<String> collect = cabinetIndexList.stream().map(CabinetPduResVO::getPduKeyB).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(collect)){
                        pduKeys.addAll(collect);
                    }
                    cabinetIndexList.forEach(cabinetPdu -> {
                        if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyA())){
                            RoomPduMenuDTO roomMenuDTOA = new RoomPduMenuDTO();
                            roomMenuDTOA.setChildren(new ArrayList<>());
                            roomMenuDTOA.setType(MenuTypeEnums.PDU.getType());
                            roomMenuDTOA.setName(cabinetPdu.getCabinetName()+"-A路");

                            roomMenuDTOA.setIp(cabinetPdu.getPduKeyA());
                            roomMenuDTOA.setUnique(cabinetPdu.getPduKeyA());
                            //父id设置机柜
                            roomMenuDTOA.setParentId(cabinetPdu.getCabinetId());
                            roomMenuDTOA.setParentType( MenuTypeEnums.CABINET.getType());
                            menuDTOS.add(roomMenuDTOA);
                        }
                        if (StringUtils.isNotEmpty(cabinetPdu.getPduKeyB())){
                            RoomPduMenuDTO roomMenuDTOB = new RoomPduMenuDTO();
                            roomMenuDTOB.setChildren(new ArrayList<>());
                            roomMenuDTOB.setType(MenuTypeEnums.PDU.getType());
                            roomMenuDTOB.setName(cabinetPdu.getCabinetName()+"-B路");
                            roomMenuDTOB.setIp(cabinetPdu.getPduKeyB());
                            roomMenuDTOB.setUnique(cabinetPdu.getPduKeyB());
                            //父id设置机柜
                            roomMenuDTOB.setParentId(cabinetPdu.getCabinetId());
                            roomMenuDTOB.setParentType( MenuTypeEnums.CABINET.getType());
                            menuDTOS.add(roomMenuDTOB);
                        }
                    });
                }
            }
            List<RoomPduMenuDTO> roomPduMenuDTOS = buildPduTree(menuDTOS);
            RoomPduMenuDTO roomMenuDTO = new RoomPduMenuDTO();
            roomMenuDTO.setId(-1);
            roomMenuDTO.setName("未绑定");

            roomMenuDTO.setType(MenuTypeEnums.BUS.getType());
            roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + -1);
            roomMenuDTO.setParentId(0);
            roomMenuDTO.setParentType(0);
            List<PduIndexDo> pduIndexDos = pduIndexDoMapper.selectList(new LambdaQueryWrapper<PduIndexDo>().eq(PduIndexDo::getIsDeleted, false));
            Map<String, PduIndexDo> pduMap = pduIndexDos.stream().collect(Collectors.toMap(PduIndexDo::getPduKey, Function.identity()));
            pduMap.keySet().removeAll(pduKeys);

            List<RoomMenuDTO> boxIndices =new ArrayList<>();
            pduMap.keySet().forEach(pduKey -> {
                PduIndexDo pduIndexDo = pduMap.get(pduKey);

                RoomPduMenuDTO pduMenuDTO = new RoomPduMenuDTO();
                pduMenuDTO.setId(pduIndexDo.getId());
                pduMenuDTO.setName(pduIndexDo.getPduKey());
                pduMenuDTO.setIp(pduIndexDo.getPduKey());
                pduMenuDTO.setType(4);
                pduMenuDTO.setParentType(3);
                pduMenuDTO.setUnique(pduIndexDo.getPduKey());
                boxIndices.add(pduMenuDTO);
            });

            roomMenuDTO.setChildren(boxIndices);
            roomPduMenuDTOS.add(roomMenuDTO);

            return roomPduMenuDTOS;
        } catch (Exception e) {
            log.error("获取菜单失败：", e);
        }

        return new ArrayList<>();
    }

    @Override
    public List<RoomMenuDTO> roomRackMenuList() {
        try {

            //获取机柜列表
            List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                    .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus())
                    .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus()));

            //获取柜列
            List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                    .eq(AisleIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));

            //获取机房
            List<RoomIndex> roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                    .eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));

            //机架列表
            List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                    .eq(RackIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));


            List<RoomMenuDTO> menuDTOS = new ArrayList<>();

            if (!CollectionUtils.isEmpty(roomIndexList)) {
                roomIndexList.forEach(roomIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(roomIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.ROOM.getType());
                    roomMenuDTO.setName(roomIndex.getRoomName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + roomIndex.getId());
                    //父id设置0
                    roomMenuDTO.setParentId(0);
                    roomMenuDTO.setParentType(0);
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleIndexList.forEach(aisleIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(aisleIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.AISLE.getType());
                    roomMenuDTO.setName(aisleIndex.getAisleName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.AISLE.getType()) + SPLIT + aisleIndex.getId());
                    //父id设置机房
                    roomMenuDTO.setParentId(aisleIndex.getRoomId());
                    roomMenuDTO.setParentType(MenuTypeEnums.ROOM.getType());
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(cabinetIndexList)) {
                cabinetIndexList.forEach(cabinetIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(cabinetIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.CABINET.getType());
                    roomMenuDTO.setName(cabinetIndex.getCabinetName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.CABINET.getType()) + SPLIT + cabinetIndex.getId());
                    //父id设置通道/机房
                    roomMenuDTO.setParentId(cabinetIndex.getAisleId() == 0 ? cabinetIndex.getRoomId() : cabinetIndex.getAisleId());
                    roomMenuDTO.setParentType(cabinetIndex.getAisleId() == 0 ? MenuTypeEnums.ROOM.getType() : MenuTypeEnums.AISLE.getType());
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(rackIndexList)) {
                rackIndexList.forEach(rackIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(rackIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.RACK.getType());
                    roomMenuDTO.setName(rackIndex.getRackName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.RACK.getType()) + SPLIT + rackIndex.getId());
                    //父id设置机柜
                    roomMenuDTO.setParentId(rackIndex.getCabinetId());
                    roomMenuDTO.setParentType(MenuTypeEnums.CABINET.getType());
                    menuDTOS.add(roomMenuDTO);
                });

            }

            return buildTree(menuDTOS);
        } catch (Exception e) {
            log.error("获取菜单失败：", e);
        }

        return new ArrayList<>();
    }

    @Override
    public List<RoomMenuDTO> roomBusMenuList() {
        try {

            //获取柜列
            List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                    .eq(AisleIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));

            //获取机房
            List<RoomIndex> roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                    .eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));


            List<RoomMenuDTO> menuDTOS = new ArrayList<>();
            List<String> busKeys = new ArrayList<>();
            if (!CollectionUtils.isEmpty(roomIndexList)) {
                roomIndexList.forEach(roomIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(roomIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.ROOM.getType());
                    roomMenuDTO.setName(roomIndex.getRoomName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + roomIndex.getId());
                    //父id设置0
                    roomMenuDTO.setParentId(0);
                    roomMenuDTO.setParentType(0);
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleIndexList.forEach(aisleIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(aisleIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.AISLE.getType());
                    roomMenuDTO.setName(aisleIndex.getAisleName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.AISLE.getType()) + SPLIT + aisleIndex.getId());
                    //父id设置机房
                    roomMenuDTO.setParentId(aisleIndex.getRoomId());
                    roomMenuDTO.setParentType(MenuTypeEnums.ROOM.getType());
                    menuDTOS.add(roomMenuDTO);
                });

                List<Integer> ids = aisleIndexList.stream().map(AisleIndex::getId).collect(Collectors.toList());

                List<AisleBar>  barList = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                        .in(AisleBar::getAisleId,ids));
                List<String> collect = barList.stream().map(AisleBar::getBusKey).collect(Collectors.toList());
                busKeys.addAll(collect);
                if (!CollectionUtils.isEmpty(barList)){
                    barList.forEach(aisleBar -> {
                        if (StringUtils.isNotEmpty(aisleBar.getBusKey())){
                            RoomMenuDTO roomMenuDTOA = new RoomMenuDTO();
                            roomMenuDTOA.setChildren(new ArrayList<>());
                            roomMenuDTOA.setType(MenuTypeEnums.BUS.getType());
                            roomMenuDTOA.setName(aisleBar.getPath() + "路");
                            roomMenuDTOA.setUnique(aisleBar.getBusKey());
                            //父id设置柜列
                            roomMenuDTOA.setParentId(aisleBar.getAisleId());
                            roomMenuDTOA.setParentType( MenuTypeEnums.AISLE.getType());
                            //非始端箱id  绑定ID
                            roomMenuDTOA.setId(aisleBar.getId());
                            menuDTOS.add(roomMenuDTOA);
                        }
                    });
                }

            }
            List<RoomMenuDTO> roomMenuDTOS = buildTree(menuDTOS);
            RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
            roomMenuDTO.setId(-1);
            roomMenuDTO.setName("未绑定");
            roomMenuDTO.setType(MenuTypeEnums.BUS.getType());
            roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + -1);
            roomMenuDTO.setParentId(0);
            roomMenuDTO.setParentType(0);

            List<RoomMenuDTO> boxIndices = busIndexDoMapper.queryRoomMenuDTO(busKeys);

            roomMenuDTO.setChildren(boxIndices);
            roomMenuDTOS.add(roomMenuDTO);

            return roomMenuDTOS;
        } catch (Exception e) {
            log.error("获取菜单失败：", e);
        }

        return new ArrayList<>();
    }

    @Override
    public List<RoomMenuDTO> roomBoxMenuList() {
        try {

            //获取柜列
            List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                    .eq(AisleIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));

            //获取机房
            List<RoomIndex> roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                    .eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));

            List<CabinetIndexBoxResVO> cabinetIndexList = cabinetIndexMapper.selectCabinetBox();

            List<RoomMenuDTO> menuDTOS = new ArrayList<>();
            List<String> boxKeys = new ArrayList<>();
            List<BoxIndex> boxIndexlist1 = null;
            if (!CollectionUtils.isEmpty(roomIndexList)) {
                roomIndexList.forEach(roomIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(roomIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.ROOM.getType());
                    roomMenuDTO.setName(roomIndex.getRoomName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + roomIndex.getId());
                    //父id设置0
                    roomMenuDTO.setParentId(0);
                    roomMenuDTO.setParentType(0);
                    menuDTOS.add(roomMenuDTO);
                });

            }
            if (!CollectionUtils.isEmpty(cabinetIndexList)) {
                Map<Integer, List<CabinetIndexBoxResVO>> cabinetMap = cabinetIndexList.stream().collect(Collectors.groupingBy(CabinetIndexBoxResVO::getId));
                cabinetMap.keySet().forEach(key -> {
                    List<CabinetIndexBoxResVO> vos = cabinetMap.get(key);
                    CabinetIndexBoxResVO vo = vos.get(0);
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setId(vo.getId());
                    roomMenuDTO.setParentId(vo.getRoomId());
                    roomMenuDTO.setName(vo.getCabinetName());
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setType(MenuTypeEnums.CABINET.getType());
                    roomMenuDTO.setParentType(MenuTypeEnums.ROOM.getType());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.CABINET.getType()) + SPLIT + vo.getId());
                    menuDTOS.add(roomMenuDTO);
                });
                Map<String, List<CabinetIndexBoxResVO>> aBoxs = cabinetIndexList.stream().filter(i -> ObjectUtils.isNotEmpty(i.getBoxKeyA()))
                        .collect(Collectors.groupingBy(CabinetIndexBoxResVO::getBoxKeyA));
                boxKeys.addAll(aBoxs.keySet());
                aBoxs.keySet().forEach(key ->{
                    List<CabinetIndexBoxResVO> vos = aBoxs.get(key);
                    for (CabinetIndexBoxResVO vo : vos) {
                        RoomMenuDTO roomMenuDTOA = new RoomMenuDTO();
                        roomMenuDTOA.setChildren(new ArrayList<>());
                        roomMenuDTOA.setType(MenuTypeEnums.BOX.getType());
                        roomMenuDTOA.setName(vo.getCabinetName() + "-A路");
                        roomMenuDTOA.setUnique(key);
                        roomMenuDTOA.setId(-1);
                        //父id设置柜列
                        roomMenuDTOA.setParentId(vo.getId());
                        roomMenuDTOA.setParentType(MenuTypeEnums.CABINET.getType());
                        menuDTOS.add(roomMenuDTOA);
                    }
                });

                Map<String, List<CabinetIndexBoxResVO>> bBoxs = cabinetIndexList.stream().filter(i -> ObjectUtils.isNotEmpty(i.getBoxKeyB()))
                        .collect(Collectors.groupingBy(CabinetIndexBoxResVO::getBoxKeyB));
                boxKeys.addAll(bBoxs.keySet());
                bBoxs.keySet().forEach(key ->{
                    List<CabinetIndexBoxResVO> vos = bBoxs.get(key);
                    for (CabinetIndexBoxResVO vo : vos) {
                        RoomMenuDTO roomMenuDTOB = new RoomMenuDTO();
                        roomMenuDTOB.setChildren(new ArrayList<>());
                        roomMenuDTOB.setType(MenuTypeEnums.BOX.getType());
                        roomMenuDTOB.setName(vo.getCabinetName() + "-B路");
                        roomMenuDTOB.setUnique(key);
                        roomMenuDTOB.setId(-1);
                        //父id设置柜列
                        roomMenuDTOB.setParentId(vo.getId());
                        roomMenuDTOB.setParentType(MenuTypeEnums.CABINET.getType());
                        menuDTOS.add(roomMenuDTOB);
                    }

                });
            }
            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleIndexList.forEach(aisleIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(aisleIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.AISLE.getType());
                    roomMenuDTO.setName(aisleIndex.getAisleName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.AISLE.getType()) + SPLIT + aisleIndex.getId());
                    //父id设置机房
                    roomMenuDTO.setParentId(aisleIndex.getRoomId());
                    roomMenuDTO.setParentType(MenuTypeEnums.ROOM.getType());
                    menuDTOS.add(roomMenuDTO);
                });

                List<Integer> ids = aisleIndexList.stream().map(AisleIndex::getId).collect(Collectors.toList());

                List<AisleBar>  barList = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                        .in(AisleBar::getAisleId,ids));

                if (!CollectionUtils.isEmpty(barList)){
                    barList.forEach(aisleBar -> {
                        if (StringUtils.isNotEmpty(aisleBar.getBusKey())){
                            RoomMenuDTO roomMenuDTOA = new RoomMenuDTO();
                            roomMenuDTOA.setChildren(new ArrayList<>());
                            roomMenuDTOA.setType(MenuTypeEnums.BUS.getType());
                            roomMenuDTOA.setName(aisleBar.getPath() + "路");
                            roomMenuDTOA.setUnique(aisleBar.getBusKey());
                            //父id设置柜列
                            roomMenuDTOA.setParentId(aisleBar.getAisleId());
                            roomMenuDTOA.setParentType( MenuTypeEnums.AISLE.getType());
                            //非始端箱id  绑定ID
                            roomMenuDTOA.setId(aisleBar.getId());
                            menuDTOS.add(roomMenuDTOA);
                        }
                    });

                    List<Integer> aisleBarIds = barList.stream().map(AisleBar::getId).collect(Collectors.toList());
//                    List<AisleBox> boxList = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
//                            .in(AisleBox::getAisleBarId,aisleBarIds));
//                    List<String> barKeys=boxList.stream().map(AisleBox::getBoxKey).collect(Collectors.toList());
//
//                    boxIndexlist1=boxIndexMapper.selectList(new LambdaQueryWrapper<BoxIndex>()
//                            .in(BoxIndex::getBoxKey,barKeys).eq(BoxIndex::getIsDeleted,0));

                    List<AisleBoxVO> aisleBoxVOList = aisleBoxMapper.selectMenuAisleByBoxId(aisleBarIds);

                    List<String> collect = aisleBoxVOList.stream().map(AisleBoxVO::getBoxKey).collect(Collectors.toList());
                    boxKeys.addAll(collect);
                    Collections.sort(aisleBoxVOList, Comparator.comparing(AisleBoxVO::getBoxName));
                    if (!CollectionUtils.isEmpty(aisleBoxVOList)) {
                        aisleBoxVOList.forEach(aisleBox -> {
                            if (StringUtils.isNotEmpty(aisleBox.getBoxKey())) {
                                RoomMenuDTO roomMenuDTOA = new RoomMenuDTO();
                                roomMenuDTOA.setChildren(new ArrayList<>());
                                roomMenuDTOA.setType(MenuTypeEnums.BOX.getType());
                                roomMenuDTOA.setName(aisleBox.getBoxName());
                                roomMenuDTOA.setUnique(aisleBox.getBoxKey());
                                //父id设置柜列
//                                roomMenuDTOA.setParentId(aisleBoxMapper.selectOne(new LambdaQueryWrapper<AisleBox>().in(AisleBox::getBoxKey,aisleBox.getBoxKey())).getAisleBarId());
                                roomMenuDTOA.setParentId(aisleBox.getAisleBarId());
                                roomMenuDTOA.setParentType(MenuTypeEnums.BUS.getType());
                                //非插接箱id  绑定ID
                                roomMenuDTOA.setId(aisleBox.getId());
                                menuDTOS.add(roomMenuDTOA);
                            }
                        });
                    }
                }
            }

            List<RoomMenuDTO> roomMenuDTOS = buildTree(menuDTOS);
            RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
            roomMenuDTO.setId(-1);
            roomMenuDTO.setName("未绑定");

            roomMenuDTO.setType(MenuTypeEnums.BUS.getType());
            roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + -1);
            roomMenuDTO.setParentId(0);
            roomMenuDTO.setParentType(0);
            boxKeys.stream().distinct().collect(Collectors.toList());
            List<RoomMenuDTO> boxIndices = boxIndexMapper.queryRoomMenuDTO(boxKeys);
            roomMenuDTO.setChildren(boxIndices);
            roomMenuDTOS.add(roomMenuDTO);
            return roomMenuDTOS;
        } catch (Exception e) {
            log.error("获取菜单失败：", e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<RoomMenuDTO> roomAisleMenuList() {
        try {
            List<RoomMenuDTO> menuDTOS = new ArrayList<>();
            //获取柜列
            List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                    .eq(AisleIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));
            if (CollectionUtils.isEmpty(aisleIndexList)){
                return menuDTOS;
            }
            List<Integer> roomIds = aisleIndexList.stream().map(AisleIndex::getRoomId).distinct().collect(Collectors.toList());
            //获取机房
            List<RoomIndex> roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                    .in(RoomIndex::getId,roomIds)
                    .eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));

            if (!CollectionUtils.isEmpty(roomIndexList)) {
                roomIndexList.forEach(roomIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(roomIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.ROOM.getType());
                    roomMenuDTO.setName(roomIndex.getRoomName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.ROOM.getType()) + SPLIT + roomIndex.getId());
                    //父id设置0
                    roomMenuDTO.setParentId(0);
                    roomMenuDTO.setParentType(0);
                    menuDTOS.add(roomMenuDTO);
                });

            }

            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleIndexList.forEach(aisleIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(aisleIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.AISLE.getType());
                    roomMenuDTO.setName(aisleIndex.getAisleName());
                    roomMenuDTO.setUnique(String.valueOf(MenuTypeEnums.AISLE.getType()) + SPLIT + aisleIndex.getId());
                    //父id设置机房
                    roomMenuDTO.setParentId(aisleIndex.getRoomId());
                    roomMenuDTO.setParentType(MenuTypeEnums.ROOM.getType());
                    menuDTOS.add(roomMenuDTO);
                });
            }
           return buildTree(menuDTOS);
        } catch (Exception e) {
            log.error("获取菜单失败：", e);
        }
        return new ArrayList<>();
    }

    /**
     * 构建树型结构
     *
     * @param roomMenuDTOS 初始列表
     * @return 返回树型结构
     */
    public static List<RoomMenuDTO> buildTree(List<RoomMenuDTO> roomMenuDTOS) {
        ArrayList<RoomMenuDTO> trees = new ArrayList<>();
        // 把菜单进行遍历
        roomMenuDTOS.forEach(menu -> {
            // 设置入口条件
            if (menu.getParentId() == 0) {
                trees.add(getChildren(menu, roomMenuDTOS));
            }
        });
        return trees;
    }

    /**
     * 构建子节点
     *
     * @param menuDTO      当前
     * @param roomMenuDTOS 初始解雇
     * @return 构建的子节点
     */
    public static RoomMenuDTO getChildren(RoomMenuDTO menuDTO, List<RoomMenuDTO> roomMenuDTOS) {
        // 设置子节点为空数组
        menuDTO.setChildren(new ArrayList<>());

        roomMenuDTOS.forEach(menu -> {
            // 当子节点为 空设置为空数组
            if (menu.getChildren() == null) {
                menu.setChildren(new ArrayList<>());
            }
            // 当前id与父级id相同添加当前元素
            if (Objects.nonNull(menuDTO.getId()) && menuDTO.getId().equals(menu.getParentId()) && menuDTO.getType() == menu.getParentType()) {
                menuDTO.getChildren().add(getChildren(menu, roomMenuDTOS));
            }
        });
        return menuDTO;
    }



    /**
     * 构建树型结构
     *
     * @param roomMenuDTOS 初始列表
     * @return 返回树型结构
     */
    public static List<RoomPduMenuDTO> buildPduTree(List<RoomPduMenuDTO> roomMenuDTOS) {
        ArrayList<RoomPduMenuDTO> trees = new ArrayList<>();
        // 把菜单进行遍历
        roomMenuDTOS.forEach(menu -> {
            // 设置入口条件
            if (menu.getParentId() == 0) {
                trees.add(getPduChildren(menu, roomMenuDTOS));
            }
        });
        return trees;
    }

    /**
     * 构建子节点
     *
     * @param menuDTO      当前
     * @param roomMenuDTOS 初始解雇
     * @return 构建的子节点
     */
    public static RoomPduMenuDTO getPduChildren(RoomPduMenuDTO menuDTO, List<RoomPduMenuDTO> roomMenuDTOS) {
        // 设置子节点为空数组
        menuDTO.setChildren(new ArrayList<>());

        roomMenuDTOS.forEach(menu -> {
            // 当子节点为 空设置为空数组
            if (menu.getChildren() == null) {
                menu.setChildren(new ArrayList<>());
            }
            // 当前id与父级id相同添加当前元素
            if (Objects.nonNull(menuDTO.getId()) && menuDTO.getId().equals(menu.getParentId()) && menuDTO.getType() == menu.getParentType()) {
                menuDTO.getChildren().add(getPduChildren(menu, roomMenuDTOS));
            }
        });
        return menuDTO;
    }
}
