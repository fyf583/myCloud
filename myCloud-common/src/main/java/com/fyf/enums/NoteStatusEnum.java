package com.fyf.enums;

import lombok.Getter;

@Getter
public enum NoteStatusEnum {
    NORMAL(1, "正常"),
    DELETE(2, "已删除");

    private final Integer code;
    private final String message;

    NoteStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static NoteStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NoteStatusEnum statusEnum : NoteStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
