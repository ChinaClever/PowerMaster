package cn.iocoder.yudao.module.statis.service.impl;

import cn.iocoder.yudao.framework.common.enums.EsIndexEnum;
import cn.iocoder.yudao.module.statis.dao.PduEleTotalDao;
import cn.iocoder.yudao.module.statis.entity.ele.PduEqBaseDo;
import cn.iocoder.yudao.module.statis.service.EleService;
import cn.iocoder.yudao.module.statis.service.EsHandleService;
import cn.iocoder.yudao.module.statis.vo.EqBillConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: chenwany
 * @Date: 2024/4/9 10:10
 * @Description:
 */
@Slf4j
@Service
public class EleServiceImpl implements EleService {

    @Autowired
    PduEleTotalDao pduEleTotalDao;
    @Autowired
    EsHandleService  esHandleService;

    @Override
    public void eleTotalDayDeal() {
        //获取配置时间段
        List<EqBillConfigVo> configVos = new ArrayList<>();
        //电量统计
        List<PduEqBaseDo> eqBaseDos = pduEleTotalDao.statisEleDay(configVos);
        //数据入库
        esHandleService.batchInsert(eqBaseDos, EsIndexEnum.PDU_EQ_TOTAL_DAY.getIndex());
    }

    @Override
    public void eleTotalWeekDeal() {

    }

    @Override
    public void eleTotalMonthDeal() {

    }

    @Override
    public void eleOutletDayDeal() {

    }

    @Override
    public void eleOutletWeekDeal() {

    }

    @Override
    public void eleOutletMonthDeal() {

    }
}
