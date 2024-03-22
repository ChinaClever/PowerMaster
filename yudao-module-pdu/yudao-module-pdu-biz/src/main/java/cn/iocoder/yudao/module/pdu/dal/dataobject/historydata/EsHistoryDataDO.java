package cn.iocoder.yudao.module.pdu.dal.dataobject.historydata;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@TableName("pdu_history_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "pdu")
public class EsHistoryDataDO{
    @Id
    @Field(store=true, index = false, type = FieldType.Long)
    private Long id;

    @Field(store=true, index = true, type = FieldType.Short)
    private Short pduId;

    @Field(store=true, index = true, type = FieldType.Short)
    private Short type;

    @Field(store=true, index = true, type = FieldType.Short)
    private Short topic;

    @Field(store=true, index = true, type = FieldType.Short)
    private Short indexes;

    @Field(store=true, index = false, type = FieldType.Float)
    private Float value;

    @Field(store=true, index = true, type = FieldType.Date)
    private LocalDateTime createTime;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test1;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test2;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test3;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test4;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test5;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test6;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test7;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test8;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test9;

    @Field(store=true, index = false, type = FieldType.Short)
    private Short test10;
}