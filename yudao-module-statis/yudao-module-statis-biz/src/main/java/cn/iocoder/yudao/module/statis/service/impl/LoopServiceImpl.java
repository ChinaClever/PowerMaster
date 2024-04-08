package cn.iocoder.yudao.module.statis.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.enums.EsIndexEnum;
import cn.iocoder.yudao.module.statis.dao.PduLoopDao;
import cn.iocoder.yudao.module.statis.entity.es.PduHdaLoopBaseDo;
import cn.iocoder.yudao.module.statis.service.EsHandleService;
import cn.iocoder.yudao.module.statis.service.LoopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: chenwany
 * @Date: 2024/4/3 14:40
 * @Description: 回路历史数据统计
 */
@Slf4j
@Service
public class LoopServiceImpl implements LoopService {

    @Autowired
    PduLoopDao loopDao;

    @Autowired
    EsHandleService esHandleService;

    @Override
    public void hourDeal() {
        log.info("回路历史按小时数据统计");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        String startTime = DateUtil.formatDateTime(calendar.getTime());
        String endTime = DateUtil.formatDateTime(new Date());

        Map<Object, Map<Object, PduHdaLoopBaseDo>> map = loopDao.statisLoop(startTime, endTime);
        List<PduHdaLoopBaseDo> list = new ArrayList<>();
        map.keySet().forEach(pduId -> list.addAll(map.get(pduId).values()));
        list.forEach(t-> log.info("回路历史数据：" + t));
        esHandleService.batchInsert(list, EsIndexEnum.PDU_HDA_LOOP_HOUR.getIndex());
    }

    @Override
    public void dayDeal() {
        log.info("回路历史数据按天统计");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        String startTime = DateUtil.formatDateTime(calendar.getTime());
        String endTime = DateUtil.formatDateTime(new Date());

        Map<Object, Map<Object, PduHdaLoopBaseDo>> map = loopDao.statisLoop(startTime, endTime);
        List<PduHdaLoopBaseDo> list = new ArrayList<>();
        map.keySet().forEach(pduId -> list.addAll(map.get(pduId).values()));
        esHandleService.batchInsert(list, EsIndexEnum.PDU_HDA_LOOP_DAY.getIndex());
    }
}
