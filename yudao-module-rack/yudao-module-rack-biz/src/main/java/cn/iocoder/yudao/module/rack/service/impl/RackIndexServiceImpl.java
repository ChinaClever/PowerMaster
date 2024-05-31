package cn.iocoder.yudao.module.rack.service.impl;

import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.exception.ServerException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rack.dto.RackIndexDTO;
import cn.iocoder.yudao.module.rack.mapper.RackIndexDoMapper;
import cn.iocoder.yudao.module.rack.service.RackIndexService;
import cn.iocoder.yudao.module.rack.vo.RackIndexVo;
import cn.iocoder.yudao.module.rack.vo.RackSaveVo;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.rack.constant.RackConstant.REDIS_KEY_RACK;
import static cn.iocoder.yudao.module.rack.enums.ErrorCodeConstants.NAME_REPEAT;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架业务
 * @date 2024/5/27 13:48
 */
@Service
@Slf4j
public class RackIndexServiceImpl implements RackIndexService {


    @Resource
    private RackIndexDoMapper rackIndexDoMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${rack-cal-refresh-url}")
    public String adder;


    @Override
    public PageResult<JSONObject> getRackPage(RackIndexVo vo) {
        try {
            //获取列表
            if (Objects.nonNull(vo.getRackIds()) && CollectionUtils.isEmpty(vo.getRackIds())){
                List<Integer> list = new ArrayList<>();
                list.add(-1);
                vo.setRackIds(list);
            }
            Page<RackIndex> page =  new Page<>(vo.getPageNo(), vo.getPageSize());

            Page<RackIndex> result = rackIndexDoMapper.selectPage(page,new LambdaQueryWrapperX<RackIndex>()
                    .eq(RackIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                    .like(StringUtils.isNotEmpty(vo.getRackName()),RackIndex::getRackName,vo.getRackName())
                    .like(StringUtils.isNotEmpty(vo.getCompany()),RackIndex::getCompany,vo.getCompany())
                    .like(StringUtils.isNotEmpty(vo.getType()),RackIndex::getType,vo.getType())
                    .in(!CollectionUtils.isEmpty(vo.getRackIds()),RackIndex::getId,vo.getRackIds()));

            List<JSONObject> indexRes = new ArrayList<>();

            if (Objects.nonNull(result) && !CollectionUtils.isEmpty(result.getRecords())) {
                result.getRecords().forEach(dto -> {
                    String key = REDIS_KEY_RACK  + dto.getId();
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
                    if (Objects.nonNull(jsonObject)) {
                        indexRes.add(jsonObject);
                    }else {
                        indexRes.add(new JSONObject());
                    }
                });
            }
            return new PageResult<>(indexRes, result.getTotal());
        }catch (Exception e){
            log.error("获取列表失败：",e);
        }

        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public JSONObject getRackDataDetail(int id) {

        try {
            //获取redis数据
            String key = REDIS_KEY_RACK + id;
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
            return jsonObject;
        } catch (Exception e) {
            log.error("获取基础数据失败: ", e);
        }
        return new JSONObject();
    }

    @Override
    public RackIndexDTO getRackDetail(int id) {

        RackIndexDTO rackIndexDTO = new RackIndexDTO();

        try {
            RackIndex rackIndex = rackIndexDoMapper.selectById(id);

            rackIndexDTO = BeanUtils.toBean(rackIndex,RackIndexDTO.class);

            rackIndexDTO.setRackId(id);
            JSONObject jsonObject = getRackDataDetail(id);
            rackIndexDTO.setCabinetName(jsonObject.getString("cabinet_name"));
            rackIndexDTO.setRoomName(jsonObject.getString("room_name"));

            return rackIndexDTO;

        }catch (Exception e){
            log.error("获取详情失败，",e);
        }
        return rackIndexDTO;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RackIndex> batchSave(RackSaveVo vo) {
        try {
            //新增
            if (!CollectionUtils.isEmpty(vo.getRacks())){
                List<RackIndex> racks = vo.getRacks();

                List<RackIndex> inserts = racks.stream().filter(rackIndex -> rackIndex.getId() == 0).collect(Collectors.toList());

                inserts.forEach(rackIndex -> {

                    //判断名称是否重复
                    RackIndex index = rackIndexDoMapper.selectOne(new LambdaQueryWrapper<RackIndex>()
                            .eq(RackIndex::getRackName,rackIndex.getRackName())
                            .eq(RackIndex::getCabinetId,vo.getCabinetId()));
                    if (Objects.nonNull(index)){
                        throw new ServerException(NAME_REPEAT.getCode(),NAME_REPEAT.getMsg());
                    }

                    rackIndex.setCabinetId(vo.getCabinetId());
                    rackIndex.setRoomId(vo.getRoomId());
                    rackIndexDoMapper.insert(rackIndex);
                });

                List<RackIndex> updates = racks.stream().filter(rackIndex -> rackIndex.getId() > 0).collect(Collectors.toList());
                updates.forEach(rackIndex ->
                {
                    RackIndex index = rackIndexDoMapper.selectOne(new LambdaQueryWrapper<RackIndex>()
                            .eq(RackIndex::getRackName,rackIndex.getRackName())
                            .eq(RackIndex::getCabinetId,vo.getCabinetId())
                            .ne(RackIndex::getId,rackIndex.getId()));
                    if (Objects.nonNull(index)){
                        throw new ServerException(NAME_REPEAT.getCode(),NAME_REPEAT.getMsg());
                    }
                    rackIndex.setCabinetId(vo.getCabinetId());
                    rackIndex.setRoomId(vo.getRoomId());
                    rackIndexDoMapper.updateById(rackIndex);

                });
            }
            return rackIndexDoMapper.selectList(new LambdaQueryWrapperX<RackIndex>()
                    .eq(RackIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                    .eq(RackIndex::getCabinetId,vo.getCabinetId()));
        }finally {
            //刷新机柜计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDel(List<Integer> ids) {
        //删除列表为空
        if (CollectionUtils.isEmpty(ids)){
            return;
        }
        try {
            List<RackIndex> rackIndexList = rackIndexDoMapper.selectList(new LambdaQueryWrapperX<RackIndex>()
                    .in(RackIndex::getId,ids));
            if (CollectionUtils.isEmpty(rackIndexList)){
                return;
            }
            rackIndexList.forEach(rackIndex -> {
                //已经删除则物理删除
                if (rackIndex.getIsDelete() == DelEnums.DELETE.getStatus()) {
                    rackIndexDoMapper.deleteById(rackIndex);
                }else {
                    //逻辑删除
                    rackIndexDoMapper.update(new LambdaUpdateWrapper<RackIndex>()
                            .eq(RackIndex::getId, rackIndex.getId())
                            .set(RackIndex::getIsDelete, DelEnums.DELETE.getStatus()));
                }

                //删除key
                String key = REDIS_KEY_RACK + rackIndex.getId();

                boolean flag = redisTemplate.delete(key);
                log.info("key: " + key + " flag : " + flag);
            });

        }finally {
            //刷新机柜计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }
    }
}
