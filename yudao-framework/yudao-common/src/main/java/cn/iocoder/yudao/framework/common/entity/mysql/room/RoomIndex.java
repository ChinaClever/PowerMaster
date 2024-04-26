package cn.iocoder.yudao.framework.common.entity.mysql.room;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房索引表
 * @date 2024/4/24 10:34
 */
@Data
@TableName(value = "room_index")
public class RoomIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;


    /**
     * 机房名
     */
    private String name;

    /**
     * 是否删除
     */
    private int isDelete;

    /**
     * 电力容量
     */
    private float powerCapacity;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
