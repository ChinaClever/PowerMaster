package cn.iocoder.yudao.module.cabinet.service;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetEnvSensor;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ICabinetEnvSensorService extends IService<CabinetEnvSensor> {
    void saveEnvSensor(int id, CabinetVo vo);
}
