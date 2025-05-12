package cn.iocoder.yudao.module.pdu.enums;

import lombok.Getter;

@Getter
public enum DataNameType {
    最大(1),
    平均(0),
    最小(-1);

    private final int value;

    DataNameType(int value) {
        this.value = value;
    }

    public static DataNameType fromValue(int value) {
        for (DataNameType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid data type value: " + value);
    }
}
