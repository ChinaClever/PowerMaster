package cn.iocoder.yudao.framework.common.dto.aisle;

import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜详情
 * @date 2024/4/28 15:27
 */
@Schema(description = "管理后台 - 机柜详情 Response VO")
@Data
public class AisleCabinetDTO {

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
     * 电力容量
     */
    @Schema(description = "电力容量", example = "2")
    private float powerCapacity;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源 0：PDU 1：母线", example = "0")
    private int pduBox;


    /**
     * 机柜高度
     */
    @Schema(description = "机柜高度", example = "5")
    private int cabinetHeight;


    /**
     * 机柜类型
     */
    @Schema(description = "机柜类型", example = "IT机柜")
    private String type;
    /**
     * x
     */
    @Schema(description = "x坐标", example = "xxx")
    @JsonProperty(value="xCoordinate")
    private int xCoordinate;

    /**
     * y
     */
    @Schema(description = "y坐标", example = "yyy")
    @JsonProperty(value="yCoordinate")
    private int yCoordinate;

    /**
     * 所属于公司
     */
    @Schema(description = "所属于公司", example = "公司名")
    private String company;

    /**
     * 标记位
     */
    private Integer index;

    @Schema(description = "A路母线ip地址", example = "1")
    private String busIpA;

    @Schema(description = "A路母线名称", example = "1")
    private String busNameA;


    @Schema(description = "A路插接箱名称", example = "1")
    private String boxNameA;


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
     * 日用能告警开关
     */
    private  int eleAlarmDay;

    /**
     * 月用能告警开关
     */
    private  int eleAlarmMonth;

    /**
     * 日用能限制
     */
    private double eleLimitDay;

    /**
     * 月用能限制
     */
    private double eleLimitMonth;

    /**
     * 运行状态
     */
    @Schema(description = "运行状态 0：空载 1：正常 2：预警 3：告警 4:未绑定 5：离线", example = "2")
    private int runStatus;

    /**
     * 已用空间
     */
    @Schema(description = "已用空间", example = "1")
    private int usedSpace;

    /**
     * 剩余空间
     */
    @Schema(description = "剩余空间", example = "1")
    private int freeSpace;
}
