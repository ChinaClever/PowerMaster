package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.enums.DisableEnums;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dto.CabinetDTO;
import cn.iocoder.yudao.module.cabinet.dto.CabinetIndexDTO;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetCfgMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetPduMapper;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.module.cabinet.vo.CabinetIndexVo;
import cn.iocoder.yudao.module.cabinet.vo.CabinetVo;
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
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    CabinetPduMapper cabinetPduMapper;
    @Autowired
    RedisTemplate redisTemplate;
//    @Autowired
//    HttpUtil httpUtil;

    @Value("${cabinet-refresh-url}")
    public String addr;


    @Override
    public PageResult<JSONObject> getPageCabinet(CabinetIndexVo vo) {
        try {
            Page<CabinetIndexDTO> page = new Page<>(vo.getPageNo(), vo.getPageSize());
            //获取机柜列表
            Page<CabinetIndexDTO> indexDTOPage = cabinetCfgMapper.selectCabList(page, vo);
            List<JSONObject> result = new ArrayList<>();
            //获取redis 实时数据
            if (Objects.nonNull(indexDTOPage) && !CollectionUtils.isEmpty(indexDTOPage.getRecords())) {
                indexDTOPage.getRecords().forEach(dto -> {
                    String key = REDIS_KEY_CABINET + dto.getRoomId() + SPLIT_KEY + dto.getId();
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
                    if (Objects.nonNull(jsonObject)) {
                        jsonObject.put(COMPANY, Objects.nonNull(dto.getCompany()) ? dto.getCompany() : "");
                        result.add(jsonObject);
                    }
                });

            }

            return new PageResult<>(result, indexDTOPage.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);

    }

    @Override
    public JSONObject getCabinetDetail(int id) {

        try {

            CabinetIndex index = cabinetIndexMapper.selectById(id);
            //获取redis数据
            String key = REDIS_KEY_CABINET + index.getRoomId() + SPLIT_KEY + id;
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
            return jsonObject;
        } catch (Exception e) {
            log.error("获取基础数据失败: ", e);
        }
        return new JSONObject();
    }

    @Override
    public CabinetDTO getCabinetDetailV2(int id) {
        CabinetDTO dto = new CabinetDTO();

        try {
            //获取数据库保存数据
            CabinetIndex index = cabinetIndexMapper.selectById(id);
            if (Objects.nonNull(index)) {

                dto.setId(id);
                dto.setCabinetName(index.getName());
                dto.setAisleId(index.getAisleId());
                dto.setRoomId(index.getRoomId());
                dto.setPowCapacity(index.getPowCapacity());
                dto.setRunStatus(index.getRunStatus());
                dto.setPduBox(index.getPduBox());

                CabinetCfg cfg = cabinetCfgMapper.selectOne(new LambdaQueryWrapper<CabinetCfg>()
                        .eq(CabinetCfg::getCabinetId, index.getId()));
                if (Objects.nonNull(cfg)) {
                    dto.setCabinetHeight(cfg.getCabinetHeight());
                    dto.setType(cfg.getType());
                    dto.setXCoordinate(cfg.getXCoordinate());
                    dto.setYCoordinate(cfg.getYCoordinate());
                    dto.setCompany(cfg.getCompany());
                }
                CabinetPdu pdu = cabinetPduMapper.selectOne(new LambdaQueryWrapper<CabinetPdu>()
                        .eq(CabinetPdu::getCabinetId, index.getId()));

                if (Objects.nonNull(pdu)) {
                    dto.setPduIpA(pdu.getPduIpA());
                    dto.setPduIpB(pdu.getPduIpB());
                    dto.setCasIdA(pdu.getCasIdA());
                    dto.setCasIdB(pdu.getCasIdB());
                }
            }
            return dto;
        } catch (Exception e) {
            log.error("获取机柜信息失败：", e);
        }
        return null;
    }

    @Override
    public CommonResult saveCabinet(CabinetVo vo) {
        try {

            //判断pdu是否已经关联其他机柜
            List<CabinetIndex> indexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                    .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                    .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus())
                    .ne(CabinetIndex::getId, vo.getId()));
            if (!CollectionUtils.isEmpty(indexList)) {
                List<Integer> ids = indexList.stream().map(CabinetIndex::getId).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(ids)) {
                    if (isExist(vo.getPduIpA(), vo.getCasIdA(), ids)) {
                        log.info("---- " + vo.getPduIpA() + vo.getCasIdA());
                        return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "pdu已关联其他机柜");
                    }

                    if (isExist(vo.getPduIpB(), vo.getCasIdB(), ids)) {
                        log.info("---- " + vo.getPduIpB() + vo.getCasIdB());
                        return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "pdu已关联其他机柜");
                    }
                }
            }

            //判断AB路pdu是否一样
            if (StringUtils.isNotEmpty(vo.getPduIpA()) && StringUtils.isNotEmpty(vo.getPduIpB())) {
                if (vo.getPduIpA().equals(vo.getPduIpB()) && vo.getCasIdA() == vo.getCasIdB()) {
                    return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "AB路pdu一致，请重新输入");
                }
            }

            CabinetIndex index;
            //编辑
            if (vo.getId() > 0) {
                //index 索引表
                index = cabinetIndexMapper.selectById(vo.getId());

                index = convertIndex(vo, index);
                //修改
                cabinetIndexMapper.updateById(index);
            } else {
                //新增
                //判断机柜名称是否重复（已删除的或者已禁用的恢复）
                index = cabinetIndexMapper.selectOne(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getName, vo.getCabinetName())
                        .eq(CabinetIndex::getRoomId, vo.getRoomId()));
                if (Objects.nonNull(index)) {
                    if (index.getIsDeleted() == DelEnums.DELETE.getStatus() || index.getIsDisabled() == DisableEnums.DISABLE.getStatus()) {
                        //index 索引表
                        index = convertIndex(vo, index);
                        //修改
                        cabinetIndexMapper.updateById(index);
                    } else {
                        return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "机柜名称重复");
                    }

                } else {
                    index = new CabinetIndex();
                    //index 索引表
                    index = convertIndex(vo, index);
                    //新增
                    cabinetIndexMapper.insert(index);

                }
            }
            vo.setId(index.getId());
            log.info("index : " + index);

            //配置表
            CabinetCfg cfg = cabinetCfgMapper.selectOne(new LambdaQueryWrapper<CabinetCfg>()
                    .eq(CabinetCfg::getCabinetId, index.getId()));
            if (Objects.nonNull(cfg)) {
                cfg = convertCfg(vo, cfg);
                //修改
                cabinetCfgMapper.updateById(cfg);
            } else {
                cfg = new CabinetCfg();

                cfg = convertCfg(vo, cfg);
                //新增
                cabinetCfgMapper.insert(cfg);
            }

            //pdu关联表
            CabinetPdu pdu = cabinetPduMapper.selectOne(new LambdaQueryWrapper<CabinetPdu>()
                    .eq(CabinetPdu::getCabinetId, index.getId()));

            if (Objects.nonNull(pdu)) {
                pdu = convertPdu(vo, pdu);
                //修改
                cabinetPduMapper.updateById(pdu);
            } else {
                pdu = new CabinetPdu();

                pdu = convertPdu(vo, pdu);
                //新增
                cabinetPduMapper.insert(pdu);
            }


            return CommonResult.success(index.getId());
        } catch (Exception e) {
            log.error("保存失败：", e);

        } finally {
            //刷新机柜计算服务缓存
            log.info("刷新计算服务缓存 --- " + addr);
            HttpUtil.get(addr);
        }
        return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "保存失败");
    }


    @Override
    public int delCabinet(int id) {
        try {
            CabinetIndex index = cabinetIndexMapper.selectById(id);
            if (Objects.isNull(index)) {
                return -1;
            }
            if (index.getIsDeleted() == DelEnums.DELETE.getStatus()) {
                //已经删除则物理删除
                cabinetIndexMapper.deleteById(id);
                cabinetPduMapper.delete(new LambdaQueryWrapper<CabinetPdu>()
                        .eq(CabinetPdu::getCabinetId, id));
                cabinetCfgMapper.delete(new LambdaQueryWrapper<CabinetCfg>()
                        .eq(CabinetCfg::getCabinetId, id));
            } else {
                //逻辑删除
                cabinetIndexMapper.update(new LambdaUpdateWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getId, id)
                        .set(CabinetIndex::getIsDeleted, DelEnums.DELETE.getStatus()));
            }

            //删除key
            String key = REDIS_KEY_CABINET + index.getRoomId() + SPLIT_KEY + index.getId();

            boolean flag = redisTemplate.delete(key);
            log.info("key: " + key + " flag : " + flag);

            return id;
        } catch (Exception e) {
            log.error("删除失败：", e);
        } finally {
            log.info("刷新计算服务缓存 --- " + addr);
            //刷新机柜计算服务缓存
            HttpUtil.get(addr);
        }
        return -1;
    }

    /**
     * 实体转换
     *
     * @param vo
     * @param index
     * @return
     */
    private CabinetIndex convertIndex(CabinetVo vo, CabinetIndex index) {
        index.setAisleId(vo.getAisleId());
        index.setName(vo.getCabinetName());
        index.setPduBox(vo.getPduBox());
        //未删除
        index.setIsDeleted(DelEnums.NO_DEL.getStatus());
        //未禁用
        index.setIsDisabled(DisableEnums.ENABLE.getStatus());
        index.setPowCapacity(vo.getPowCapacity());
        index.setRoomId(vo.getRoomId());
        return index;
    }

    /**
     * 实体转换
     *
     * @param vo
     * @param pdu
     * @return
     */
    private CabinetPdu convertPdu(CabinetVo vo, CabinetPdu pdu) {
        pdu.setCabinetId(vo.getId());
        pdu.setPduIpA(vo.getPduIpA());
        pdu.setPduIpB(vo.getPduIpB());
        pdu.setCasIdB(vo.getCasIdB());
        pdu.setCasIdA(vo.getCasIdA());
        return pdu;
    }


    /**
     * 实体转换
     *
     * @param vo
     * @param cfg
     * @return
     */
    private CabinetCfg convertCfg(CabinetVo vo, CabinetCfg cfg) {
        cfg.setCabinetId(vo.getId());
        cfg.setCabinetHeight(vo.getCabinetHeight());
        cfg.setCabinetName(vo.getCabinetName());
        cfg.setCompany(vo.getCompany());
        cfg.setType(vo.getType());
        cfg.setXCoordinate(vo.getXCoordinate());
        cfg.setYCoordinate(vo.getYCoordinate());
        return cfg;
    }

    /**
     * 判断pdu是否已经绑定
     *
     * @param ip
     * @param cas
     * @param ids
     * @return
     */
    private boolean isExist(String ip, Integer cas, List<Integer> ids) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        List<CabinetPdu> pduFlag = cabinetPduMapper.selectList(new LambdaQueryWrapper<CabinetPdu>()
                .in(CabinetPdu::getCabinetId, ids)
                .and((wq -> wq.and(qr -> qr.eq(CabinetPdu::getPduIpA, ip)
                                .eq(CabinetPdu::getCasIdA, cas))
                        .or(qr -> qr.eq(CabinetPdu::getPduIpB, ip)
                                .eq(CabinetPdu::getCasIdB, cas)))
                )
        );
        if (!CollectionUtils.isEmpty(pduFlag)) {
            return true;
        } else {
            return false;
        }
    }
}
