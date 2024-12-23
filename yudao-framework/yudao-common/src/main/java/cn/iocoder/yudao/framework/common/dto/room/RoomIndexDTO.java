package cn.iocoder.yudao.framework.common.dto.room;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 机柜用能 Response VO")
@Data
public class RoomIndexDTO {

    @Schema(description = "机柜id", example = "1")
    private int id;

    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "xxx")
    private String roomName;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
