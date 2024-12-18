package cn.iocoder.yudao.module.rack.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RackPageResVO implements Serializable {

    private Integer id;
    /**
     * 机柜id
     */
    @Schema(description = "机柜id")
    private Integer cabinetId;

    /**
     * U位名称
     */
    @Schema(description = "U位名称")
    private String rackName;

    /**
     * A路输出位
     */
    @Schema(description = "A路输出位")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> outletIdA;

    /**
     * B路输出位
     */
    @Schema(description = "B路输出位")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> outletIdB;
    /**
     * 所属公司
     */
    @Schema(description = "所属公司")
    private String company;
    /**
     * U位位置
     */
    @Schema(description = "U位位置")
    @JsonProperty(value = "uAddress")
    private Integer uAddress;

    /**
     * U位高度
     */
    @Schema(description = "U位高度")
    @JsonProperty(value = "uHeight")
    private Integer uHeight;
    /**
     * 设备类型
     */
    @Schema(description = "设备类型")
    private String rackType;

    /**
     * 是否删除 0未删除 1已删除
     */
    @Schema(description = "是否删除 0未删除 1已删除")
    private Boolean isDelete;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "机房名称")
    private String roomName;
    @Schema(description = "机柜名称")
    private String cabinetName;

    @Schema(description = "A路电流")
    private BigDecimal cura;

    @Schema(description = "b电流")
    private BigDecimal curb;

    @Schema(description = "有功功率")
    private BigDecimal powActive;

    @Schema(description = "无功功率")
    private BigDecimal powReactive;

    @Schema(description = "功率因数")
    private BigDecimal powerFactor;

    @Schema(description = "位置")
    private String local;
}
