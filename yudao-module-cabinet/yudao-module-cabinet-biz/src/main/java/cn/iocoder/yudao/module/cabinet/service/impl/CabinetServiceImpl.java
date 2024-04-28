package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetIndexDTO;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetCfgMapper;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import cn.iocoder.yudao.module.cabinet.vo.CabinetIndexVo;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.*;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜页面
 * @date 2024/4/28 14:25
 */
@Slf4j
@Service
public class CabinetServiceImpl implements CabinetService {

    @Autowired
    CabinetCfgMapper cabinetCfgMapper;
    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public PageResult<CabinetIndexDTO> getPageCabinet(CabinetIndexVo vo) {
        try {
            Page<CabinetIndexDTO>  page = new Page<>(vo.getPageNo(), vo.getPageSize());
            //获取机柜列表
            Page<CabinetIndexDTO> indexDTOPage = cabinetCfgMapper.selectList(page,vo);
            List<CabinetIndexDTO> result = new ArrayList<>();
            //获取redis 实时数据
            if (Objects.nonNull(indexDTOPage) && !CollectionUtils.isEmpty(indexDTOPage.getRecords())){
                indexDTOPage.getRecords().forEach(dto -> {
                    String key = REDIS_KEY_CABINET + dto.getRoomId()+SPLIT_KEY + dto.getId();
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
                    JSONObject cabinetPower = jsonObject.getJSONObject(CABINET_POWER);

                    if (cabinetPower.containsKey(PATH_A)) {

                        JSONObject aPath = cabinetPower.getJSONObject(PATH_A);

                        dto.setApparentA(aPath.getFloat(POW_APPARENT));
                        dto.setActiveA(aPath.getFloat(POW_ACTIVE));
                        dto.setEleA(aPath.getDouble(ELE_ACTIVE));

                    }
                    if (cabinetPower.containsKey(PATH_B)) {
                        JSONObject bPath = cabinetPower.getJSONObject(PATH_B);

                        dto.setApparentB(bPath.getFloat(POW_APPARENT));
                        dto.setActiveB(bPath.getFloat(POW_ACTIVE));
                        dto.setEleA(bPath.getDouble(ELE_ACTIVE));
                    }
                    if (cabinetPower.containsKey(TOTAL_DATA)) {
                        JSONObject total = cabinetPower.getJSONObject(TOTAL_DATA);

                        dto.setApparentTotal(total.getFloatValue(POW_APPARENT));
                        dto.setActiveTotal(total.getFloatValue(POW_ACTIVE));
                        dto.setEleA(total.getDouble(ELE_ACTIVE));
                    }
                    result.add(dto);
                });

            }

            return new PageResult<CabinetIndexDTO>(result, indexDTOPage.getTotal());
        }catch (Exception e){
            log.error("获取数据失败：",e);
        }
        return new PageResult<CabinetIndexDTO>(new ArrayList<>(), 0L);

    }
}
