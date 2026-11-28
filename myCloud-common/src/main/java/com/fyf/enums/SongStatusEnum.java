package com.fyf.enums;

import lombok.Getter;

@Getter
public enum SongStatusEnum {
    NORMAL(1, "正常"),
    DELETE(2, "已删除");

    private final Integer code;
    private final String message;

    SongStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SongStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SongStatusEnum statusEnum : SongStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
