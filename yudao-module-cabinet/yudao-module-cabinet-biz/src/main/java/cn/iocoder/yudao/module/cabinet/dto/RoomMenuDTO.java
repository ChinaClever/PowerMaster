package cn.iocoder.yudao.module.cabinet.dto;

import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房菜单
 * @date 2024/5/6 9:36
 */
@Data
public class RoomMenuDTO {

    private Integer id;
    private Integer parentId;
    private String name;
    // 下级列表
    private List<RoomMenuDTO> children;
    // 是否选中
    private boolean isSelect;

    /**
     * 当前节点数据类型
     */
    private int type;

    /**
     * 父节点类型
     */
    private int parentType;

    /**
     * 唯一标识
     */
    private String unique;

}
