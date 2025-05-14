package cn.iocoder.yudao.framework.common.elasticsearch.mapping;

import cn.easyes.annotation.IndexField;
import cn.easyes.annotation.IndexId;
import cn.easyes.annotation.IndexName;
import cn.easyes.common.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@IndexName(value = "cabinet_eq_total_day", shardsNum = 1, replicasNum = 0)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CabinetDayPower {
    @IndexId
    private String id;

    // 机柜id
    @IndexField(fieldType = FieldType.LONG)
    private Integer cabinet_id;

    // 开始时间
    @IndexField(fieldType = FieldType.KEYWORD_TEXT, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private String start_time;

    // 开始电能
    @IndexField(fieldType = FieldType.FLOAT)
    private float start_ele;

    // 结束时间
    @IndexField(fieldType = FieldType.KEYWORD_TEXT, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private String end_time;

    // 结束电能
    @IndexField(fieldType = FieldType.FLOAT)
    private float end_ele;

    // 电能
    @IndexField(fieldType = FieldType.FLOAT)
    private float eq_value;

    // 电费
    @IndexField(fieldType = FieldType.FLOAT)
    private float bill_value;

    // 计费方式
    @IndexField(fieldType = FieldType.LONG)
    private Integer bill_mode;

    // 计费时间段
    @IndexField(fieldType = FieldType.TEXT)
    private String bill_period;

    
}
