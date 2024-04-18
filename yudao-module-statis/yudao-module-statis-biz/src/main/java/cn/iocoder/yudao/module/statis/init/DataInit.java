package cn.iocoder.yudao.module.statis.init;

import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduEqBillConfig;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduStatisConfig;
import cn.iocoder.yudao.module.statis.mapper.PduEqBillConfigMapper;
import cn.iocoder.yudao.module.statis.mapper.PduStatisConfigMapper;
import cn.iocoder.yudao.module.statis.schedule.ScheduleDayTask;
import cn.iocoder.yudao.module.statis.schedule.ScheduleEleTask;
import cn.iocoder.yudao.module.statis.schedule.ScheduleHourTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.statis.constant.Constants.STATIS_CONFIG;

/**
 * @author chenwany
 * @Date: 2024/3/27 13:58
 * @Description: 系统初始化操作
 */
@Slf4j
@Component
public class DataInit {

    @Autowired
    PduEqBillConfigMapper billConfigMapper;
    @Autowired
    PduStatisConfigMapper statisConfigMapper;

    public static final Map<String,PduStatisConfig> STATIS_CONFIG_MAP = new HashMap<>();
    public static final Map<String,PduEqBillConfig> BILL_CONFIG_MAP = new HashMap<>();

    /**
     * 系统初始化操作
     */
    @PostConstruct
    public void init() {
        log.info("------------系统初始化-----------");
        //缓存配置数据
        initPduConfig();
    }

    /**
     * 缓存配置
     */
    private void initPduConfig() {
        try {
            PduStatisConfig statisConfig = statisConfigMapper.selectOne(new LambdaQueryWrapper<PduStatisConfig>()
                    .orderByDesc(PduStatisConfig::getCreateTime)
                    .last("limit 1"));
            STATIS_CONFIG_MAP.put(STATIS_CONFIG,statisConfig);

            List<PduEqBillConfig> eqBillConfigs = billConfigMapper.selectList(new LambdaQueryWrapper<PduEqBillConfig>()
                    .eq(PduEqBillConfig::getBillMode, statisConfig.getBillMode()));

            log.info("缓存数据：{}", eqBillConfigs.size());
            if (!CollectionUtils.isEmpty(eqBillConfigs)) {
                Map<String, PduEqBillConfig> pduIndexDoMap = eqBillConfigs.stream().
                        collect(Collectors.toMap(PduEqBillConfig::getBillPeriod, Function.identity()));
                BILL_CONFIG_MAP.putAll(pduIndexDoMap);
            }

        } catch (Exception e) {
            log.error("缓存mysql数据失败", e);
        }

    }

}
