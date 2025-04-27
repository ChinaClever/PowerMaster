package cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord;

import lombok.Data;

import java.io.Serializable;

@Data
public class BinlogEventHeader implements Serializable {
    private long timestamp;
    private int eventType;
    private long serverId;
    private long eventLength;
    private long nextPosition;
    private int flags;
}
