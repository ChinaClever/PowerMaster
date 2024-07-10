package cn.iocoder.yudao.module.bus.service.impl;

import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.module.bus.dto.BusDataDTO;
import cn.iocoder.yudao.framework.common.mapper.BoxIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.BusIndexDoMapper;
import cn.iocoder.yudao.module.bus.service.BusService;
import cn.iocoder.yudao.module.bus.vo.BusIndexVo;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.module.bus.BusConstant.REDIS_KEY_BOX;
import static cn.iocoder.yudao.module.bus.BusConstant.REDIS_KEY_BUS;

/**
 * @author luowei
 * @version 1.0
 * @description: 母线数据
 * @date 2024/5/30 14:46
 */
@Slf4j
@Service
public class BusServiceImpl implements BusService {

    @Autowired
    BusIndexDoMapper busIndexDoMapper;
    @Autowired
    BoxIndexMapper boxIndexMapper;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<BusDataDTO> getBusData(BusIndexVo vo) {
        List<BusDataDTO> busDataDTOS = new ArrayList<>();

        try{

            //获取id列表
            if (Objects.nonNull(vo.getIds()) && CollectionUtils.isEmpty(vo.getIds())){
                List<Integer> list = new ArrayList<>();
                list.add(-1);
                vo.setIds(list);
            }

            //始端箱数据
            List<BusIndex> busIndexList = busIndexDoMapper.selectList(new LambdaQueryWrapper<BusIndex>()
                    .in(!CollectionUtils.isEmpty(vo.getIds()),BusIndex::getId,vo.getIds())
                    .eq(BusIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                    .orderByAsc(BusIndex::getBarId)
                    .last("limit 2"));
            if (!CollectionUtils.isEmpty(busIndexList)){
                ValueOperations ops = redisTemplate.opsForValue();

                //插接箱数据
                busIndexList.forEach(busIndex -> {
                    String devIp = busIndex.getIpAddr();
                    Integer barId = busIndex.getBarId();
                    BusDataDTO busDataDTO = new BusDataDTO();
                    busDataDTO.setBusId(busIndex.getId());
                    busDataDTO.setBusName(busIndex.getBusName());
                    busDataDTO.setDevIp(busIndex.getIpAddr());

                    //始端箱数据
                    String busKey = REDIS_KEY_BUS + busIndex.getDevKey();
                    JSONObject busData = JSON.parseObject(JSON.toJSONString(ops.get(busKey)));
                    busDataDTO.setBusData(busData);

                    //插接箱数据
                    List<JSONObject> boxDataList = new ArrayList<>();

                    List<BoxIndex> boxIndexList = boxIndexMapper.selectList(new LambdaQueryWrapper<BoxIndex>()
                            .eq(BoxIndex::getIsDeleted,DelEnums.NO_DEL.getStatus())
                            .eq(BoxIndex::getIpAddr,devIp)
                            .eq(BoxIndex::getBarId,barId));
                    if (!CollectionUtils.isEmpty(boxIndexList)){
                        boxIndexList.forEach(boxIndex -> {
                            //插接箱基础数据
                            String boxKey = REDIS_KEY_BOX + boxIndex.getDevKey();
                            JSONObject boxData = JSON.parseObject(JSON.toJSONString(ops.get(boxKey)));

                            boxDataList.add(boxData);
                        });
                    }

                    busDataDTO.setBoxDataList(boxDataList);
                    busDataDTOS.add(busDataDTO);
                });

            }

        }catch (Exception e){
            log.error("获取数据失败：",e);
        }
        return busDataDTOS;
    }
}
