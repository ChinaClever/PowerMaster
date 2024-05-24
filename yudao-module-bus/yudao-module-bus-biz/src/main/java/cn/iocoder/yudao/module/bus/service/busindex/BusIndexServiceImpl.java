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

}