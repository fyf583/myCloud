package com.fyf.enums;

import lombok.Getter;

@Getter
public enum DiaryStatusEnum {
    NORMAL(1, "正常"),
    DELETE(2, "已删除");

    private final Integer code;
    private final String message;

    DiaryStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static DiaryStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DiaryStatusEnum statusEnum : DiaryStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
