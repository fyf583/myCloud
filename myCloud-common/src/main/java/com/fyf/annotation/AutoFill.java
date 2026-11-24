package com.fyf.annotation;

import com.fyf.enums.OperateType;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    OperateType value();
}
