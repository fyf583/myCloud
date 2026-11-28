package com.fyf.enums;

import lombok.Getter;

@Getter
public enum FileStatusEnum {
    NORMAL(1, "正常"),
    DELETE(2, "已删除");

    private final Integer code;
    private final String message;

    FileStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static FileStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FileStatusEnum statusEnum : FileStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
