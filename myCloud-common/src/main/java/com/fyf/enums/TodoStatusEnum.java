package com.fyf.enums;

import lombok.Getter;

@Getter
public enum TodoStatusEnum {
    UNDONE(0, "未完成"),
    DONE(1, "已完成"),
    DELETE(2, "已删除");

    private final Integer code;
    private final String message;

    TodoStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static TodoStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TodoStatusEnum statusEnum : TodoStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    /** 是否为合法的“可见状态”（0 未完成 / 1 已完成） */
    public static boolean isVisible(Integer code) {
        return UNDONE.getCode().equals(code) || DONE.getCode().equals(code);
    }
}
