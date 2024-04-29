package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetIndexDTO;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetCfgMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetIndexMapper;
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
    CabinetIndexMapper cabinetIndexMapper;
    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public PageResult<JSONObject> getPageCabinet(CabinetIndexVo vo) {
        try {
            Page<CabinetIndexDTO>  page = new Page<>(vo.getPageNo(), vo.getPageSize());
            //获取机柜列表
            Page<CabinetIndexDTO> indexDTOPage = cabinetCfgMapper.selectList(page,vo);
            List<JSONObject> result = new ArrayList<>();
            //获取redis 实时数据
            if (Objects.nonNull(indexDTOPage) && !CollectionUtils.isEmpty(indexDTOPage.getRecords())){
                indexDTOPage.getRecords().forEach(dto -> {
                    String key = REDIS_KEY_CABINET + dto.getRoomId()+SPLIT_KEY + dto.getId();
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
                    result.add(jsonObject);
                });

            }

            return new PageResult<JSONObject>(result, indexDTOPage.getTotal());
        }catch (Exception e){
            log.error("获取数据失败：",e);
        }
        return new PageResult<JSONObject>(new ArrayList<>(), 0L);

    }

    @Override
    public JSONObject getCabinetDetail(int id) {

        try{

            CabinetIndex index = cabinetIndexMapper.selectById(id);

            String key = REDIS_KEY_CABINET + index.getRoomId()+SPLIT_KEY + id;
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
            return jsonObject;
        }catch (Exception e){
            log.error("获取基础数据失败: ",e);
        }
        return new JSONObject();
    }

    @Override
    public int insertCabinet(CabinetIndexVo vo) {
        return 0;
    }
}
