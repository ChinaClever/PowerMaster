package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetEnvSensor;
import cn.iocoder.yudao.framework.common.mapper.CabinetEnvSensorMapper;
import cn.iocoder.yudao.module.cabinet.service.ICabinetEnvSensorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CabinetEnvSensorServiceImpl extends ServiceImpl<CabinetEnvSensorMapper, CabinetEnvSensor> implements ICabinetEnvSensorService {
    @Autowired
    private CabinetEnvSensorMapper envSensorMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEnvSensor(int cabinetId, CabinetVo vo) {
        //环境数据为空，删除数据后返回
        if (CollectionUtils.isEmpty(vo.getSensorList())) {
            envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, cabinetId));
            return;
        }else {
            List<CabinetEnvSensor> envSensors = envSensorMapper.selectList(new LambdaQueryWrapper<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, cabinetId));
            vo.getSensorList().forEach(cabinetEnvSensor -> {
                cabinetEnvSensor.setCabinetId(cabinetId);
            });
            List<CabinetEnvSensor> sensorList = vo.getSensorList();
            saveOrUpdateBatch(sensorList);

            if (envSensors.size()>0){
                List<Integer> idOld = envSensors.stream().map(CabinetEnvSensor::getId).collect(Collectors.toList());
                List<Integer> id = sensorList.stream().filter(i -> Objects.nonNull(i.getId())).map(CabinetEnvSensor::getId).collect(Collectors.toList());
                idOld.removeAll(id);
                if (idOld.size()>0){
                    envSensorMapper.deleteBatchIds(idOld);
                }
            }
        }
    }
}
