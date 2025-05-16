package cn.iocoder.yudao.framework.common.enums;

import lombok.Getter;

@Getter
public enum DataTypeEnums {
    MAX(1),
    AVG(0),
    MIN(-1);

    private final int value;

    DataTypeEnums(int value) {
        this.value = value;
    }

    public static DataTypeEnums fromValue(int value) {
        for (DataTypeEnums type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid data type value: " + value);
    }
}
