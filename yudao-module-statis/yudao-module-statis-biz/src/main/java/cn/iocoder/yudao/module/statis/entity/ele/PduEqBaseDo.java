package cn.iocoder.yudao.module.statis.entity.ele;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.module.statis.entity.BaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/9 09:39
 * @Description: 电量计算基础数据
 */
@Data
public class PduEqBaseDo extends BaseDo {

    /**
     * 开始时间
     */
    @JsonProperty("start_time")
    private DateTime startTime;

    /**
     * 结束时间
     */
    @JsonProperty("end_time")
    private DateTime endTime;

    /**
     * 开始电能
     */
    @JsonProperty("start_ele")
    private double startEle;

    /**
     * 结束电能
     */
    @JsonProperty("end_ele")
    private double endEle;

    /**
     * 电量
     */
    private double eq;

    /**
     * 电费
     */
    private double bill;
}
