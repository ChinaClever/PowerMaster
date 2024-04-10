package cn.iocoder.yudao.module.statis.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.enums.EsIndexEnum;
import cn.iocoder.yudao.module.statis.dao.PduTotalDao;
import cn.iocoder.yudao.module.statis.entity.total.PduBaseDo;
import cn.iocoder.yudao.module.statis.service.EsHandleService;
import cn.iocoder.yudao.module.statis.service.TotalService;
import lombok.extern.slf4j.Slf4j;
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
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            String startTime = DateUtil.formatDateTime(calendar.getTime());
            String endTime = DateUtil.formatDateTime(new Date());

            Map<Integer, PduBaseDo> map = totalDao.statisTotalHour(startTime, endTime);
            List<PduBaseDo> list = new ArrayList<>();
            map.keySet().forEach(pduId -> {
                PduBaseDo pduBaseDo = map.get(pduId);
                list.add(pduBaseDo);
            });
            list.forEach(t->{
                log.info("总历史数据：" + t);
            });
            esHandleService.batchInsert(list, EsIndexEnum.PDU_HDA_TOTAL_HOUR.getIndex());
        }catch (Exception e){
            log.error("总历史数据统计异常：" ,e);
        }

    }

    @Override
    public void dayDeal() {
        log.info("总历史数据按天统计");
        try{
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            String startTime = DateUtil.formatDateTime(calendar.getTime());
            String endTime = DateUtil.formatDateTime(new Date());

            Map<Integer, PduBaseDo> map = totalDao.statisTotalDay(startTime, endTime);
            List<PduBaseDo> list = new ArrayList<>();
            map.keySet().forEach(pduId -> {
                PduBaseDo pduBaseDo = map.get(pduId);
                list.add(pduBaseDo);
            });
            esHandleService.batchInsert(list, EsIndexEnum.PDU_HDA_TOTAL_DAY.getIndex());
        }catch (Exception e){
            log.error("总历史数据统计异常：" ,e);
        }

    }
}
