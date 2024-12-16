package cn.iocoder.yudao.module.bus.service.impl;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleBoxDTO;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBox;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.module.bus.dto.BusDataDTO;
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

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Resource
    AisleBarMapper aisleBarMapper;

    @Resource
    AisleBoxMapper aisleBoxMapper;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<BusDataDTO> getBusData(BusIndexVo vo) {
        List<BusDataDTO> busDataDTOS = new ArrayList<>();

        try{

            //母线
            List<AisleBar>  aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                    .eq(AisleBar::getAisleId,vo.getId()));


            //始端箱数据
            if (!CollectionUtils.isEmpty(aisleBars)){
                ValueOperations ops = redisTemplate.opsForValue();

                //插接箱数据
                aisleBars.forEach(busIndex -> {
                    BusDataDTO busDataDTO = new BusDataDTO();
                    //busDataDTO.setBusName(busIndex.getBusName());
                    //busDataDTO.setDevIp(busIndex.getDevIp());
                    busDataDTO.setBarKey(busIndex.getBusKey());
                    busDataDTO.setPath(busIndex.getPath());
                    busDataDTO.setDirection(busIndex.getDirection());

                    //始端箱数据
                    Object object = ops.get(REDIS_KEY_BUS + busIndex.getBusKey());
                    if (Objects.nonNull(object)){
                        JSONObject busData = JSON.parseObject(JSON.toJSONString(object));
                        busDataDTO.setBusData(busData);
                    }

                    //插接箱数据
                    List<JSONObject> boxDataList = new ArrayList<>();

                    List<AisleBox> aisleBoxList = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
                            .eq(AisleBox::getAisleBarId,busIndex.getId()));

                    if (!CollectionUtils.isEmpty(aisleBoxList)){
                        List<AisleBox> boxList = aisleBoxList.stream().sorted(Comparator.comparing(AisleBox::getBoxIndex)).collect(Collectors.toList());
                        for (int i = 0; i < boxList.size(); i++) {
                            //插接箱基础数据
                            Object box = ops.get(REDIS_KEY_BOX + boxList.get(i).getBoxKey());
                            JSONObject boxData = JSON.parseObject(JSON.toJSONString(boxList.get(i)));

                            if (Objects.nonNull(box)){
                                boxData.put("data",JSON.parseObject(JSON.toJSONString(box)));
                            }
                            boxDataList.add(i,boxData);
                        }
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
