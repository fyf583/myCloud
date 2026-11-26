package com.fyf.enums;

import lombok.Getter;

@Getter
public enum MessageStatusEnum {
    NORMAL(1,"正常"),
    DELETE(2,"已删除");

    private final Integer code;
    private final String message;
    MessageStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public static MessageStatusEnum getByCode(Integer code){
        for (MessageStatusEnum statusEnum : MessageStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
