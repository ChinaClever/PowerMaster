package cn.iocoder.yudao.framework.mybatis.core.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 表字段信息封装类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnInfo {
    String name;
    int type;
}
