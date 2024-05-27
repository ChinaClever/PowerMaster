package cn.iocoder.yudao.module.bus.service.busindex;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.*;

/**
 * 始端箱索引 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class BusIndexServiceImpl implements BusIndexService {

    @Resource
    private BusIndexMapper busIndexMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Long createIndex(BusIndexSaveReqVO createReqVO) {
        // 插入
        BusIndexDO index = BeanUtils.toBean(createReqVO, BusIndexDO.class);
        busIndexMapper.insert(index);
        // 返回
        return index.getId();
    }

    @Override
    public void updateIndex(BusIndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        BusIndexDO updateObj = BeanUtils.toBean(updateReqVO, BusIndexDO.class);
        busIndexMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Long id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        busIndexMapper.deleteById(id);
    }

    private void validateIndexExists(Long id) {
        if (busIndexMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public BusIndexDO getIndex(Long id) {
        return busIndexMapper.selectById(id);
    }

    @Override
    public PageResult<BusIndexRes> getIndexPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusIndexRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BusIndexDO busIndexDO : list) {
            BusIndexRes busIndexRes = new BusIndexRes();
            res.add(busIndexRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + busIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray loadRate = lineItemList.getJSONArray("load_rate");
            List<Double> rateList = loadRate.toList(Double.class);
            if(rateList.size() > 1) {
                busIndexRes.setALoadRate(loadRate.getDouble(0));
                busIndexRes.setBLoadRate(loadRate.getDouble(1));
                busIndexRes.setCLoadRate(loadRate.getDouble(2));
            } else{
                busIndexRes.setALoadRate(loadRate.getDouble(0));
            }
            rateList.sort(Collections.reverseOrder());
            Double biggest = rateList.get(0) * 100;
            if (biggest == 0){
                busIndexRes.setColor(0);
            } else if (biggest < 30){
                busIndexRes.setColor(1);
            } else if (biggest < 60){
                busIndexRes.setColor(2);
            } else if (biggest < 90){
                busIndexRes.setColor(3);
            } else if (biggest >= 90){
                busIndexRes.setColor(4);
            }
            if(pageReqVO.getColor() != null){
                if(!pageReqVO.getColor().contains(busIndexRes.getColor())){
                    continue;
                }
            }

        }

        return new PageResult<>(res,busIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BusRedisDataRes> getBusRedisPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusRedisDataRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BusIndexDO busIndexDO : list) {
            BusRedisDataRes busRedisDataRes = new BusRedisDataRes();
            res.add(busRedisDataRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + busIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray volValue = lineItemList.getJSONArray("vol_value");
            JSONArray volStatus = lineItemList.getJSONArray("vol_status");
            JSONArray curValue = lineItemList.getJSONArray("cur_value");
            JSONArray curStatus = lineItemList.getJSONArray("cur_status");
            JSONArray powValue = lineItemList.getJSONArray("pow_value");
            JSONArray powStatus = lineItemList.getJSONArray("pow_status");
            JSONArray powReactive = lineItemList.getJSONArray("pow_reactive");
            for (int i = 0; i < 3; i++) {
                double vol = volValue.getDoubleValue(i);
                Integer volSta = volStatus.getInteger(i);
                double cur = curValue.getDoubleValue(i);
                Integer curSta =curStatus.getInteger(i);
                double activePow = powValue.getDoubleValue(i);
                Integer activePowSta =powStatus.getInteger(i);
                double reactivePow = powReactive.getDoubleValue(i);
                if (i == 0){
                    busRedisDataRes.setACur(cur);
                    busRedisDataRes.setACurStatus(curSta);
                    if(curSta != 0){
                        busRedisDataRes.setACurColor("red");
                    }
                    busRedisDataRes.setAVol(vol);
                    busRedisDataRes.setAVolStatus(volSta);
                    if(volSta != 0){
                        busRedisDataRes.setAVolColor("red");
                    }
                    busRedisDataRes.setAActivePow(activePow);
                    busRedisDataRes.setAActivePowStatus(activePowSta);
                    if(activePowSta != 0){
                        busRedisDataRes.setAActivePowColor("red");
                    }
                    busRedisDataRes.setAReactivePow(reactivePow);
                }else if(i == 1){
                    busRedisDataRes.setBCur(cur);
                    busRedisDataRes.setBCurStatus(curSta);
                    if(curSta != 0){
                        busRedisDataRes.setBCurColor("red");
                    }
                    busRedisDataRes.setBVol(vol);
                    busRedisDataRes.setBVolStatus(volSta);
                    if(volSta != 0){
                        busRedisDataRes.setBVolColor("red");
                    }
                    busRedisDataRes.setBActivePow(activePow);
                    busRedisDataRes.setBActivePowStatus(activePowSta);
                    if(activePowSta != 0){
                        busRedisDataRes.setBActivePowColor("red");
                    }
                    busRedisDataRes.setBReactivePow(reactivePow);
                }else if(i == 2){
                    busRedisDataRes.setCCur(cur);
                    busRedisDataRes.setCCurStatus(curSta);
                    if(curSta != 0){
                        busRedisDataRes.setCCurColor("red");
                    }
                    busRedisDataRes.setCVol(vol);
                    busRedisDataRes.setCVolStatus(volSta);
                    if(volSta != 0){
                        busRedisDataRes.setCVolColor("red");
                    }
                    busRedisDataRes.setCActivePow(activePow);
                    busRedisDataRes.setCActivePowStatus(activePowSta);
                    if(activePowSta != 0){
                        busRedisDataRes.setCActivePowColor("red");
                    }
                    busRedisDataRes.setCReactivePow(reactivePow);
                }
            }

        }
        return new PageResult<>(res,busIndexDOPageResult.getTotal());
    }

}