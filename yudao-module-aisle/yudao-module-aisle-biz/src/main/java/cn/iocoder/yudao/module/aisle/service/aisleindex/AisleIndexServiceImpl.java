package cn.iocoder.yudao.module.aisle.service.aisleindex;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.mapper.AisleBarMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.aisle.dal.mysql.aisleindex.AisleIndexCopyMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo.*;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.aisle.enums.ErrorCodeConstants.*;

/**
 * 通道列 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class AisleIndexServiceImpl implements AisleIndexService {

    @Resource
    private AisleIndexCopyMapper aisleIndexCopyMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private AisleBarMapper aisleBarMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public Integer createIndex(AisleIndexSaveReqVO createReqVO) {
        // 插入
        AisleIndexDO index = BeanUtils.toBean(createReqVO, AisleIndexDO.class);
        aisleIndexCopyMapper.insert(index);
        // 返回
        return index.getId();
    }

    @Override
    public void updateIndex(AisleIndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        AisleIndexDO updateObj = BeanUtils.toBean(updateReqVO, AisleIndexDO.class);
        aisleIndexCopyMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Integer id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        aisleIndexCopyMapper.deleteById(id);
    }

    private void validateIndexExists(Integer id) {
        if (aisleIndexCopyMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public AisleIndexDO getIndex(Integer id) {
        return aisleIndexCopyMapper.selectById(id);
    }

    @Override
    public PageResult<AisleIndexRes> getIndexPage(AisleIndexPageReqVO pageReqVO) {
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
        List<AisleIndexDO> list = aisleIndexDOPageResult.getList();
        List<Integer> aisleIds = list.stream().map(AisleIndexDO::getId).collect(Collectors.toList());
        List<AisleBar> aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapperX<AisleBar>().inIfPresent(AisleBar::getAisleId, aisleIds));
        List<String> devKey = aisleBars.stream().map(AisleBar::getBarKey).collect(Collectors.toList());
        Map<String, Map<Integer, String>> aisleBarMap = aisleBars.stream()
                .collect(Collectors.groupingBy(
                        AisleBar::getBarKey,
                        Collectors.toMap(AisleBar::getAisleId, AisleBar::getPath)
                ));
        List<AisleIndexRes> res = new ArrayList<>();
        List redisList = getMutiBusRedis(devKey);
        for (AisleIndexDO aisleIndexDO : list) {
            AisleIndexRes busIndexRes = new AisleIndexRes();
            busIndexRes.setId(aisleIndexDO.getId());
            busIndexRes.setName(aisleIndexDO.getName());
            busIndexRes.setRoomId(aisleIndexDO.getRoomId());
            res.add(busIndexRes);
        }
        Map<Integer, AisleIndexRes> resMap = res.stream().collect(Collectors.toMap(AisleIndexRes::getId, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String busDevKey = jsonObject.getString("dev_ip") + SPLIT_KEY_BUS + jsonObject.getString("bus_name");
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray loadRate = lineItemList.getJSONArray("load_rate");
            List<Double> rateList = loadRate.toList(Double.class);
            for (Integer i : aisleBarMap.get(busDevKey).keySet()) {
                AisleIndexRes aisleIndexRes = resMap.get(i);
                String path = aisleBarMap.get(busDevKey).get(i);
                LoadRate rate = new LoadRate();
                if(rateList.size() > 1) {
                    rate.setALoadRate(loadRate.getDouble(0));
                    rate.setBLoadRate(loadRate.getDouble(1));
                    rate.setCLoadRate(loadRate.getDouble(2));
                } else{
                    rate.setALoadRate(loadRate.getDouble(0));
                }
                rateList.sort(Collections.reverseOrder());
                Double biggest = rateList.get(0) * 100;
                Integer color = 0;
                if (biggest == 0){
                    color= 0;
                } else if (biggest < 30){
                    color = 1;
                } else if (biggest < 60){
                    color = 2;
                } else if (biggest < 90){
                    color = 3;
                } else if (biggest >= 90){
                    color = 4;
                }
                if (path.equals("A")){
                    if(rateList.size() > 1) {
                        aisleIndexRes.setALoadRateA(loadRate.getDouble(0));
                        aisleIndexRes.setBLoadRateA(loadRate.getDouble(1));
                        aisleIndexRes.setCLoadRateA(loadRate.getDouble(2));
                    }else {
                        aisleIndexRes.setALoadRateA(loadRate.getDouble(0));
                    }
                    aisleIndexRes.setColorA(color);
                    aisleIndexRes.setDevKeyA(busDevKey);
                }else{
                    if(rateList.size() > 1) {
                        aisleIndexRes.setALoadRateB(loadRate.getDouble(0));
                        aisleIndexRes.setBLoadRateB(loadRate.getDouble(1));
                        aisleIndexRes.setCLoadRateB(loadRate.getDouble(2));
                    }else{
                        aisleIndexRes.setALoadRateB(loadRate.getDouble(0));
                    }
                    aisleIndexRes.setColorB(color);
                    aisleIndexRes.setDevKeyB(busDevKey);
                }
            }
        }

        return new PageResult<>(res,aisleIndexDOPageResult.getTotal());
    }

    @Override
    public List<Integer> getDevKeyList() {
        List<Integer> result = aisleIndexCopyMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(AisleIndexDO::getId).collect(Collectors.toList());
        return result;
    }

    private List getMutiRedis(List<AisleIndexDO> list){
        List<String> devKeys = list.stream().map(busIndexDo -> REDIS_KEY_AISLE + busIndexDo.getId()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    private List getMutiBusRedis(List<String> devKey){
        List<String> devKeys = devKey.stream().map(busIndexDo -> REDIS_KEY_BUS + busIndexDo).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    private void getPosition(List<? extends AisleIndexRespVO> res){
        if (CollectionUtils.isAnyEmpty(res)){
            return;
        }
        List<Integer> roomIds = res.stream().map(AisleIndexRespVO::getRoomId).collect(Collectors.toList());
        Map<Integer, String> roomMap = roomIndexMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getName));
        res.forEach(aisleIndexRespVO -> {
            aisleIndexRespVO.setLocation(roomMap.get(aisleIndexRespVO.getRoomId()) + SPLIT_KEY +aisleIndexRespVO.getName());
        });
    }

}