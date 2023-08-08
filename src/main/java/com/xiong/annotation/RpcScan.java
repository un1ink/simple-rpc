package com.xiong.annotation;

import org.springframework.context.annotation.Import;
import com.xiong.spring.CustomScannerRegistrar;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {

    String[] basePackage();
}