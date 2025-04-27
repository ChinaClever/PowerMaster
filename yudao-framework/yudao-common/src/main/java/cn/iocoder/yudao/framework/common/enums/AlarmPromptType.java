package cn.iocoder.yudao.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlarmPromptType {

    VOICE_ALARM(1, "voice_alarm"),

    EMAIL_ALARM(2, "mail_alarm"),

    SMS_ALARM(3, "sms_alarm"),

    MQ_ALARM(4, "mq_alarm"),

    ;

    private final Integer code;

    private final String type;

    public static AlarmPromptType valueOf(Integer code) {
        for (AlarmPromptType value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

}
