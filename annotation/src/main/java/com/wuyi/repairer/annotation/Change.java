package com.wuyi.repairer.annotation;

// todo move this class to a proper package

/**
 * When a method is called, decide if call a fixed version method instead,
 * or just call the origin version directly.
 */
public interface Change {
    boolean dispatch(Call call);
}
