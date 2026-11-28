package com.fyf.enums;

import lombok.Getter;

@Getter
public enum AlbumStatusEnum {
    NORMAL(1, "正常"),
    DELETE(2, "已删除");

    private final Integer code;
    private final String message;

    AlbumStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static AlbumStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AlbumStatusEnum statusEnum : AlbumStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
