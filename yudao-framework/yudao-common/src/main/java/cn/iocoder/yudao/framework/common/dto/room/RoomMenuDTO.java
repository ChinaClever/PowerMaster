package cn.iocoder.yudao.framework.common.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房菜单
 * @date 2024/5/6 9:36
 */
@Schema(description = "管理后台 - 机房菜单 Response VO")
@Data
public class RoomMenuDTO {

    @Schema(description = "id", example = "1")
    private Integer id;

    @Schema(description = "父id", example = "1")
    private Integer parentId;

    @Schema(description = "节点名", example = "1")
    private String name;
    // 下级列表
    @Schema(description = "下级列表", example = "[]")
    private List<RoomMenuDTO> children;
    // 是否选中
    @Schema(description = "是否选中", example = "0")
    private boolean isSelect;

    /**
     * 当前节点数据类型
     */
    @Schema(description = "当前节点数据类型 1机房 2柜列 3机柜 4pdu", example = "2")
    private int type;

    /**
     * 父节点类型
     */
    @Schema(description = "父节点类型 1机房 2柜列 3机柜 4pdu", example = "2")
    private int parentType;

    /**
     * 唯一标识
     */
    @Schema(description = "唯一标识", example = "3-2")
    private String unique;

}
