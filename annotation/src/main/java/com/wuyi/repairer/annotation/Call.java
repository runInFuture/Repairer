package com.wuyi.repairer.annotation;

/**
 * Represent to a method call.
 * todo is needed?
 */
public interface Call<T> {
    void parameters(Object... parameters);
    T invoke();
}
