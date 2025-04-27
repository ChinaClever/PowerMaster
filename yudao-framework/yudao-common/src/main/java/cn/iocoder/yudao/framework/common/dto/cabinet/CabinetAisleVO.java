package cn.iocoder.yudao.framework.common.dto.cabinet;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetBox;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.vo.RackIndexResVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜详情
 * @date 2024/4/28 15:27
 */
@Schema(description = "管理后台 - 机柜详情 Response VO")
@Data
public class CabinetAisleVO {

    /**
     * 机柜id
     */
    @Schema(description = "机柜id", example = "1")
    private int id;
    /**
     * 机柜名称
     */
    @Schema(description = "机柜名称", example = "机柜123")
    private String cabinetName;

    /**
     * 机房编号
     */
    @Schema(description = "机房编号", example = "1")
    private int roomId;
    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "机房123")
    private String roomName;
    /**
     * 通道编号
     */
    @Schema(description = "通道编号", example = "2")
    private int aisleId;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "2")
    private Double powCapacity;
    /**
     * 机柜类型
     */
    @Schema(description = "机柜类型", example = "5")
    private String cabinetType;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源 0：PDU 1：母线", example = "0")
    private Boolean pduBox;
    /**
     * 是否禁用
     */
    @Schema(description = "禁用 0：启用 1：禁用", example = "0")
    private Integer isDisabled;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private Integer isDeleted;


    /**
     * 机柜高度
     */
    @Schema(description = "机柜高度", example = "5")
    private int cabinetHeight;

    private int cabinetUseHeight;
    /**
     * 机柜类型
     */
    @Schema(description = "机柜类型", example = "IT机柜")
    private String type;
    /**
     * 注释
     */
    @Schema(description = "注释", example = "xxx")
    @JsonProperty(value="xCoordinate")
    private int xCoordinate;

    /**
     * 类名
     */
    @Schema(description = "类名", example = "yyy")
    @JsonProperty(value="yCoordinate")
    private int yCoordinate;

    /**
     * 所属于公司
     */
    @Schema(description = "所属于公司", example = "公司名")
    private String company;
    /**
     * A路IP地址
     */
    @Schema(description = "A路IP地址", example = "127.0.0.1")
    private String pduIpA;
    /**
     * A路级联编号
     */
    @Schema(description = "A路级联编号", example = "0")
    private int casIdA;
    /**
     * B路IP地址
     */
    @Schema(description = "B路IP地址", example = "127.0.0.1")
    private String pduIpB;
    /**
     * B路级联编号
     */
    @Schema(description = "B路级联编号", example = "0")
    private int casIdB;
    /**
     * 运行状态
     */
    @Schema(description = "运行状态 0：空载 1：正常 2：预警 3：告警 4:未绑定 5：离线", example = "2")
    private int runStatus;

    @Schema(description = "环境数据")
    private List<CabinetEnvSensorDTO> sensorList;

    @Schema(description = "u位数据", example = "[]")
    private List<RackIndexResVO> rackIndexList;


    @Schema(description = "A路输出位长度", example = "10")
    private BigDecimal outletA;

    @Schema(description = "B路输出位长度", example = "10")
    private BigDecimal outletB;

    /**
     * 已用空间
     */
    @Schema(description = "已用空间", example = "1")
    private int usedSpace;

    /**
     * 设备总数
     */
    @Schema(description = "设备总数", example = "1")
    private int rackNum;

    /**
     * 剩余空间
     */
    @Schema(description = "剩余空间", example = "1")
    private int freeSpace;


    @Schema(description = "A路母线ip地址", example = "1")
    private String busIpA;

    @Schema(description = "A路母线名称", example = "1")
    private String busNameA;


    @Schema(description = "A路插接箱名称", example = "1")
    private String boxNameA;

    @Schema(description = "A路母线id", example = "1")
    private Integer barIdA;


    @Schema(description = "A路级联地址", example = "1")
    private Integer addrA;


    @Schema(description = "B路母线id", example = "1")
    private Integer barIdB;


    @Schema(description = "B路级联地址", example = "1")
    private Integer addrB;


    @Schema(description = "A路插接箱输出位id", example = "1")
    private Integer boxOutletIdA;


    @Schema(description = "B路母线ip地址", example = "1")
    private String busIpB;

    @Schema(description = "B路母线名称", example = "1")
    private String busNameB;


    @Schema(description = "B路插接箱名称", example = "1")
    private String boxNameB;


    @Schema(description = "B路插接箱输出位id", example = "1")
    private Integer boxOutletIdB;

    /**
     * 柜列中机柜位置
     */
    @Schema(description = "柜列中机柜位置", example = "1")
    private Integer index;

    /**
     * 昨日电量
     */
    @Schema(description = "昨日电量", example = "1")
    private Double yesterdayEq;

    /**
     * 插接箱ID编号
     */
    @Schema(description = "昨日电量")
    private Integer boxIndexA;

    /**
     * 插接箱ID编号
     */
    @Schema(description = "昨日电量")
    private Integer boxIndexB;
    /**
     * 日用能告警开关
     */
    @Schema(description = "日用能告警开关")
    private  Boolean eleAlarmDay;

    /**
     * 月用能告警开关
     */
    @Schema(description = "月用能告警开关")
    private  Boolean eleAlarmMonth;

    /**
     * 日用能限制
     */
    @Schema(description = "日用能限制")
    private BigDecimal eleLimitDay;

    /**
     * 月用能限制
     */
    @Schema(description = "月用能限制")
    private BigDecimal eleLimitMonth;




    /**
     * 负载率
     */
    @Schema(description = "负载率")
    private float loadRate;
    /**
     * A路相电流
     */
    @Schema(description = "A路相电流")
    private  float[] lineCurA;

    /**
     * b路相电流
     */
    @Schema(description = "b路相电流")
    private  float[] lineCurB;

    /**
     * A路相电压
     */
    @Schema(description = "A路相电压")
    private  float[] lineVolA;

    /**
     * B路相电压
     */
    @Schema(description = "B路相电压")
    private  float[] lineVolB;

    /**
     * 总功率因素
     */
    @Schema(description = "总功率因素")
    private float powerFactor;

    /**
     * A功率因素
     */
    @Schema(description = "A功率因素")
    private float powerFactorA;

    /**
     * b功率因素
     */
    @Schema(description = "b功率因素")
    private float powerFactorB;

    /**
     * 有功功率
     */
    @Schema(description = "有功功率")
    private  float powActive;

    /**
     * a有功功率
     */
    @Schema(description = "a有功功率")
    private  float powActiveA;

    /**
     * b有功功率
     */
    @Schema(description = "b有功功率")
    private  float powActiveB;

    /**
     * 无功功率
     */
    @Schema(description = "无功功率")
    private  float powReactive;

    /**
     * A无功功率
     */
    @Schema(description = "A无功功率")
    private  float powReactiveA;

    /**
     * B无功功率
     */
    @Schema(description = "B无功功率")
    private  float powReactiveB;
    /**
     * 视在功率
     */
    @Schema(description = "视在功率")
    private  float powApparent;

    /**
     * a视在功率
     */
    @Schema(description = "a视在功率")
    private  float powApparentA;


    /**
     * b视在功率
     */
    @Schema(description = "b视在功率")
    private  float powApparentB;


    /**
     * 温度
     */
    @Schema(description = "冷通道温度")
    private BigDecimal temData;

    /**
     * 温度
     */
    @Schema(description = "热通道温度")
    private BigDecimal temDataHot;

    private CabinetBox cabinetBoxes;

    private CabinetPdu cabinetPdus;

    private List<RackIndex> rackIndices;

}
