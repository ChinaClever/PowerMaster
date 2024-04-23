package cn.iocoder.yudao.module.statis.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletBaseDo;
import cn.iocoder.yudao.framework.common.enums.EsIndexEnum;
import cn.iocoder.yudao.module.statis.dao.PduOutletDao;
import cn.iocoder.yudao.module.statis.service.EsHandleService;
import cn.iocoder.yudao.module.statis.service.OutletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: chenwany
 * @Date: 2024/4/3 14:40
 * @Description: 输出位历史数据统计
 */
@Slf4j
@Service
public class OutletServiceImpl implements OutletService {

    @Autowired
    PduOutletDao outletDao;

    @Autowired
    EsHandleService esHandleService;

    @Override
    public void hourDeal() {
        log.info("输出位历史按小时数据统计");
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            String startTime = DateUtil.formatDateTime(calendar.getTime());
            String endTime = DateUtil.formatDateTime(new Date());

            Map<Integer, Map<Integer, PduHdaOutletBaseDo>> map = outletDao.statisOutletHour(startTime, endTime);
            List<PduHdaOutletBaseDo> list = new ArrayList<>();
            map.keySet().forEach(pduId -> list.addAll(map.get(pduId).values()));
            list.forEach(t->{
                log.info("输出历史数据：" + t);
            });
            esHandleService.batchInsert(list, EsIndexEnum.PDU_HDA_OUTLET_HOUR.getIndex());
        }catch (Exception e){
            log.error("输出位历史数据统计异常：" ,e);
        }

    }

    @Override
    public void dayDeal() {
        log.info("输出位历史数据按天统计");
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            String startTime = DateUtil.formatDateTime(calendar.getTime());
            String endTime = DateUtil.formatDateTime(new Date());

            Map<Integer, Map<Integer, PduHdaOutletBaseDo>> map = outletDao.statisOutletDay(startTime, endTime);
            List<PduHdaOutletBaseDo> list = new ArrayList<>();
            map.keySet().forEach(pduId -> list.addAll(map.get(pduId).values()));
            esHandleService.batchInsert(list, EsIndexEnum.PDU_HDA_OUTLET_DAY.getIndex());
        }catch (Exception e){
            log.error("输出位历史数据统计异常：" ,e);
        }

    }
}
