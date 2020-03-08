package com.wuyi.repairer.builder.proto;

public interface Func<T, R> {
    R call(T t);
}
