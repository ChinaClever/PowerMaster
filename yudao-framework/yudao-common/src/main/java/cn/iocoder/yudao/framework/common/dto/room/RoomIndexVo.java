package cn.iocoder.yudao.framework.common.dto.room;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 机房列表分页 Request VO")
@Data
public class RoomIndexVo extends PageParam {
    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     * 机房名
     */
    private String roomName;

    /**
     * 地址（楼层）
     */
    private String addr;

    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
