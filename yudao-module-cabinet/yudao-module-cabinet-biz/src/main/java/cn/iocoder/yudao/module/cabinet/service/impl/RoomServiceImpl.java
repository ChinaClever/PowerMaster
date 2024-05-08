package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.enums.DisableEnums;
import cn.iocoder.yudao.module.cabinet.dto.RoomMenuDTO;
import cn.iocoder.yudao.module.cabinet.enums.MenuTypeEnums;
import cn.iocoder.yudao.module.cabinet.mapper.AisleIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.RoomIndexMapper;
import cn.iocoder.yudao.module.cabinet.service.RoomService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.SPLIT;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房操作
 * @date 2024/5/6 9:10
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    RoomIndexMapper roomIndexMapper;
    @Autowired
    CabinetIndexMapper cabinetIndexMapper;
    @Autowired
    AisleIndexMapper aisleIndexMapper;

    @Override
    public List<RoomIndex> roomList(String name) {

        List<RoomIndex> roomIndexList = new ArrayList<>();

        try {

            roomIndexList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                    .eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus())
                    .like(StringUtils.isNotEmpty(name), RoomIndex::getName, name));

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
                    roomMenuDTO.setName(roomIndex.getName());
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
                    roomMenuDTO.setName(aisleIndex.getName());
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
                    roomMenuDTO.setName(cabinetIndex.getName());
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


            List<RoomMenuDTO> menuDTOS = new ArrayList<>();

            if (!CollectionUtils.isEmpty(roomIndexList)) {
                roomIndexList.forEach(roomIndex -> {
                    RoomMenuDTO roomMenuDTO = new RoomMenuDTO();
                    roomMenuDTO.setChildren(new ArrayList<>());
                    roomMenuDTO.setId(roomIndex.getId());
                    roomMenuDTO.setType(MenuTypeEnums.ROOM.getType());
                    roomMenuDTO.setName(roomIndex.getName());
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
                    roomMenuDTO.setName(aisleIndex.getName());
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
                    roomMenuDTO.setName(cabinetIndex.getName());
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
            if (menuDTO.getId().equals(menu.getParentId()) && menuDTO.getType() == menu.getParentType()) {
                menuDTO.getChildren().add(getChildren(menu, roomMenuDTOS));
            }
        });
        return menuDTO;
    }
}
