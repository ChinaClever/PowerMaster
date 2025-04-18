package cn.iocoder.yudao.module.pdu.api;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.vo.CabinetPduResVO;
import com.alibaba.druid.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PduDeviceApiImpl implements PduDeviceApi{

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private AisleIndexMapper aisleIndexMapper;


    @Override
    public Map<String, String> getPositionByKeys(List<String> pduKeys) {
        Map<String, String> locationMap = new HashMap<>();
        if (CollectionUtils.isEmpty(pduKeys)) {
            return locationMap;
        }
        List<CabinetPduResVO> cabinetPdus = cabinetIndexMapper.selectCabinetPduList(pduKeys);

        // 将查询结果按 ipAddr 和 cascadeAddr 分组
        Map<String, List<CabinetPduResVO>> cabinetPduAMap = cabinetPdus.stream()
                .filter(vo -> pduKeys.contains(vo.getPduKeyA()))
                .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduKeyA()));


        Map<String, List<CabinetPduResVO>> cabinetPduBMap = cabinetPdus.stream()
                .filter(vo -> pduKeys.contains(vo.getPduKeyB()))
                .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduKeyB()));

        List<Integer> roomIds = cabinetPdus.stream()
                .map(CabinetPduResVO::getRoomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (roomIds.isEmpty()) {
            roomIds.add(0);
        }
        List<RoomIndex> roomIndices = roomIndexMapper.selectBatchIds(roomIds);

        Map<Integer, String> roomMap = roomIndices.stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getRoomName));
        List<Integer> cabIds = cabinetPdus.stream().filter(dto -> dto.getAisleId() != 0).map(CabinetPduResVO::getAisleId).collect(Collectors.toList());
        Map<Integer, String> aisleMap;
        if (!CollectionUtils.isEmpty(cabIds)) {
            List<AisleIndex> aisleIndexList = aisleIndexMapper.selectBatchIds(cabIds);
            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleMap = aisleIndexList.stream().collect(Collectors.toMap(AisleIndex::getId, AisleIndex::getAisleName));
            } else {
                aisleMap = new HashMap<>();
            }
        } else {
            aisleMap = new HashMap<>();
        }

        pduKeys.forEach(pduIndex -> {
            String localtion = null;
            List<CabinetPduResVO> cabinetPduAList = cabinetPduAMap.get(pduIndex);
            List<CabinetPduResVO> cabinetPduBList = cabinetPduBMap.get(pduIndex);

            if (cabinetPduAList != null && !cabinetPduAList.isEmpty()) {
                CabinetPduResVO cabinetIndex = cabinetPduAList.get(0); // 假设结果唯一
                if (Objects.nonNull(cabinetIndex)) {
                    if (cabinetIndex.getAisleId() != 0) {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + aisleMap.get(cabinetIndex.getAisleId()) + "-" + cabinetIndex.getCabinetName() + "-" + "A路";
                    } else {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + cabinetIndex.getCabinetName() + "-" + "A路";
                    }
                }
            }

            if (cabinetPduBList != null && !cabinetPduBList.isEmpty()) {
                CabinetPduResVO cabinetIndex = cabinetPduBList.get(0); // 假设结果唯一
                if (Objects.nonNull(cabinetIndex)) {
                    if (cabinetIndex.getAisleId() != 0) {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + aisleMap.get(cabinetIndex.getAisleId()) + "-" + cabinetIndex.getCabinetName() + "-" + "B路";
                    } else {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + cabinetIndex.getCabinetName() + "-" + "B路";
                    }
                }
            }
            if (StringUtils.isEmpty(localtion)) {
                locationMap.put(pduIndex, "未绑定");
            } else {
                locationMap.put(pduIndex, localtion);
            }
        });
        return locationMap;
    }
}
