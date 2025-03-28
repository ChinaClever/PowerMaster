package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AisleIndexDelResVO {

    private Integer id;

    private String roomName;

    /**
     * 柜列名称
     */
    @Schema(description = "柜列名称", example = "柜列1")
    private String aisleName;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-05-07 01:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
