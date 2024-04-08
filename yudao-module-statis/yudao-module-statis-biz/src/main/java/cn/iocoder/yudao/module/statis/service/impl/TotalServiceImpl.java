package cn.iocoder.yudao.module.statis.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.enums.EsIndexEnum;
import cn.iocoder.yudao.module.statis.dao.PduOutletDao;
import cn.iocoder.yudao.module.statis.dao.PduTotalDao;
import cn.iocoder.yudao.module.statis.entity.es.PduBaseDo;
import cn.iocoder.yudao.module.statis.entity.es.PduHdaOutletBaseDo;
import cn.iocoder.yudao.module.statis.service.EsHandleService;
import cn.iocoder.yudao.module.statis.service.OutletService;
import cn.iocoder.yudao.module.statis.service.TotalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: chenwany
 * @Date: 2024/4/3 14:40
 * @Description: 总历史数据统计
 */
@Slf4j
@Service
public class TotalServiceImpl implements TotalService {

    @Autowired
    PduTotalDao totalDao;

    @Autowired
    EsHandleService esHandleService;

    @Override
    public void hourDeal() {
        log.info("总历史按小时数据统计");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        String startTime = DateUtil.formatDateTime(calendar.getTime());
        String endTime = DateUtil.formatDateTime(new Date());

        Map<Object, PduBaseDo> map = totalDao.statisTotal(startTime, endTime);
        List<PduBaseDo> list = new ArrayList<>();
        map.keySet().forEach(pduId -> {
            PduBaseDo pduBaseDo = map.get(pduId);
            list.add(pduBaseDo);
        });
        list.forEach(t->{
            log.info("总历史数据：" + t);
        });
        esHandleService.batchInsert(list, EsIndexEnum.PDU_HDA_TOTAL_HOUR.getIndex());
    }

    @Override
    public void dayDeal() {
        log.info("总历史数据按天统计");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        String startTime = DateUtil.formatDateTime(calendar.getTime());
        String endTime = DateUtil.formatDateTime(new Date());

        Map<Object, PduBaseDo> map = totalDao.statisTotal(startTime, endTime);
        List<PduBaseDo> list = new ArrayList<>();
        map.keySet().forEach(pduId -> {
            PduBaseDo pduBaseDo = map.get(pduId);
            list.add(pduBaseDo);
        });
        esHandleService.batchInsert(list, EsIndexEnum.PDU_HDA_TOTAL_DAY.getIndex());
    }
}
