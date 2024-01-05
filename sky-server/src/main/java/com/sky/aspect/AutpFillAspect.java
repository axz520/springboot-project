package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类
 * insert和update自动插入更新和创建相关信息
 */
@Aspect
@Component
@Slf4j
public class AutpFillAspect {
    /**
     * 切入点:mapper下所有包含AutoFill的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {
    }

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段填充");

        // 获取数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType value = autoFill.value();

        // 获取参数类型
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return;
        }
        Object entity = args[0];

        // 准备赋值参数
        LocalDateTime time = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();

        // 根据类型赋值
        if (value == OperationType.UPDATE) {
            // 获取
            try {
                //获得set方法对象----Method
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                // 通过反射调用目标对象的方法
                setCreateTime.invoke(entity, time);
                setUpdateTime.invoke(entity, time);
                setCreateUser.invoke(entity, id);
                setUpdateUser.invoke(entity, id);
            } catch (Exception ex) {
                log.error("公共字段自动填充失败：{}", ex.getMessage());
            }

        } else if (value == OperationType.INSERT) {
            try {
                //获得set方法对象----Method
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                // 通过反射调用目标对象的方法
                setCreateTime.invoke(entity, time);
                setCreateUser.invoke(entity, id);
            } catch (Exception ex) {
                log.error("公共字段自动填充失败：{}", ex.getMessage());
            }
        }
    }
}
