package com.fyf.aspect;

import com.fyf.annotation.AutoFill;
import org.aspectj.lang.ProceedingJoinPoint;
import com.fyf.enums.OperateType;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Aspect
@Component
public class AutoFillAspect {
    @Pointcut("execution(* com.fyf.mapper..*.insert*(..)) || execution(* com.fyf.mapper..*.update*(..))")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return joinPoint.proceed();
        }
        Object entity = args[0];
        // 只有业务实体才需要填充：JDK 自带类型（Long / String 等）直接放行，
        // 否则反射访问 java.lang 字段会被 JDK 模块系统拒绝（InaccessibleObjectException）
        if (entity == null || entity.getClass().getName().startsWith("java.")) {
            return joinPoint.proceed();
        }
        String methodName = joinPoint.getSignature().getName();
        LocalDateTime now = LocalDateTime.now();

        OperateType currentType;
        if (methodName.startsWith("insert")) {
            currentType = OperateType.INSERT;
        }  else {
            currentType = OperateType.UPDATE;
        }

        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            AutoFill autoFill = field.getAnnotation(AutoFill.class);
            if (autoFill == null) {
                continue;
            }
            OperateType annoType = autoFill.value();

            // 按操作类型匹配填充
            switch (currentType) {
                case INSERT:
                    // 新增：填充INSERT标记字段
                    if (annoType == OperateType.INSERT ) {
                        field.set(entity, now);
                    }
                    break;
                case UPDATE:
                    // 更新：只填充UPDATE标记字段
                    if (annoType == OperateType.UPDATE) {
                        field.set(entity, now);
                    }
                    break;
            }
        }
        return joinPoint.proceed();
    }
}
