package cn.iocoder.yudao.module.cabinet.dto;

import jdk.internal.dynalink.linker.LinkerServices;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 总有功功率数据
 * @date 2024/5/6 15:24
 */
@Data
public class CabinetActivePowDTO {

    /**
     * 今日有功功率峰值
     */
    private Float todayMax;

    /**
     * 今日有功功率峰值时间
     */
    private String todayMaxTime;
    /**
     * 昨日有功功率峰值
     */
    private Float yesterdayMax;

    /**
     * 昨日有功功率峰值时间
     */
    private String yesterdayMaxTime;

    /**
     * 今日总有功功率趋势数据
     */
    private List<CabinetActivePowTrendDTO> todayList;

    /**
     * 昨日总有功功率趋势数据
     */
    private List<CabinetActivePowTrendDTO> yesterdayList;
}
