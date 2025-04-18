package cn.iocoder.yudao.module.system.service.init;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCfg;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.module.alarm.dal.mysql.cfgmail.AlarmCfgMailMapper;
import cn.iocoder.yudao.module.alarm.dal.mysql.cfgprompt.AlarmCfgPromptMapper;
import cn.iocoder.yudao.module.alarm.dal.mysql.cfgsms.AlarmCfgSmsMapper;
import cn.iocoder.yudao.module.alarm.dal.mysql.logrecord.AlarmLogRecordMapper;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2025/4/15 13:37
 */
@Service("adminInitService")
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AdminInitServiceImpl implements AdminInitService {

    @Autowired
    private PduIndexDoMapper pduIndexDoMapper;
    @Autowired
    private RoomIndexMapper  roomIndexMapper;
    @Autowired
    private RoomCfgMapper roomCfgMapper;
    @Autowired
    private AisleIndexMapper aisleIndexMapper;
    @Autowired
    private AisleBarMapper aisleBarMapper;
    @Autowired
    private AisleBoxMapper aisleBoxMapper;
    @Autowired
    private AisleCfgMapper aisleCfgMapper;
    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;
    @Autowired
    private CabinetCfgMapper cabinetCfgMapper;
    @Autowired
    private CabinetPduMapper cabinetPduMapper;
    @Autowired
    private CabinetBoxMapper cabinetBoxMapper;
    @Autowired
    private RackIndexDoMapper rackIndexDoMapper;
    @Autowired
    private BusIndexDoMapper busIndexDoMapper;
    @Autowired
    private BoxIndexMapper boxIndexMapper;

    @Autowired
    private AlarmLogRecordMapper alarmLogRecordMapper;
    @Autowired
    private AlarmCfgPromptMapper alarmCfgPromptMapper;
    @Autowired
    private AlarmCfgMailMapper alarmCfgMailMapper;
    @Autowired
    private AlarmCfgSmsMapper alarmCfgSmsMapper;
    @Override
    public Boolean systemInit() {
        try {
            //初始化 alarm 数据
            alarmLogRecordMapper.initLogRecordData();
            alarmCfgPromptMapper.initCfgPromptData();
            alarmCfgMailMapper.initCfgMailData();
            alarmCfgSmsMapper.initCfgSmsData();

            //初始化 room 数据
            roomIndexMapper.initRoomData();
            roomCfgMapper.initRoomCfgData();

            //初始化 aisle 数据
            aisleIndexMapper.initaisleData();
            aisleBarMapper.initaisleBarData();
            aisleBoxMapper.initaisleBoxData();
            aisleCfgMapper.initaisleCfgData();

            //初始化 cabinet 数据
            cabinetIndexMapper.initCabinetData();
            cabinetCfgMapper.initCabineCfgData();
            cabinetPduMapper.initCabinePduData();
            cabinetBoxMapper.initCabineBoxData();

            //初始化 rack 数据
            rackIndexDoMapper.initrackData();

            //初始化 pdu 数据
            pduIndexDoMapper.initPduData();

            //初始化 bus 数据
            busIndexDoMapper.initBusData();
            boxIndexMapper.initBoxData();
            return true;

        } catch (Exception e) {
            throw new RuntimeException("初始化失败: " + e.getMessage());
        }
    }
}
