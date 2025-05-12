package cn.iocoder.yudao.module.pdu.enums;

import lombok.Getter;

@Getter
public enum DataType {
    MAX(1),
    AVG(0),
    MIN(-1);

    private final int value;

    DataType(int value) {
        this.value = value;
    }

    public static DataType fromValue(int value) {
        for (DataType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid data type value: " + value);
    }
}