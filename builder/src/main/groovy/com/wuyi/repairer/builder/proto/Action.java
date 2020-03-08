package com.wuyi.repairer.builder.proto;

public interface Action<T> {
    void call(T t);
}
