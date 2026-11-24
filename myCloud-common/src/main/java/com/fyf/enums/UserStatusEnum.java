package com.fyf.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    NORMAL(1,"正常"),
    LOCKED(2,"被锁定");

    private Integer code;
    private String desc;
    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public static UserStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatusEnum userStatusEnum : UserStatusEnum.values()) {
            if (userStatusEnum.getCode().equals(code)) {
                return userStatusEnum;
            }
        }
        return NORMAL;
    }
}
