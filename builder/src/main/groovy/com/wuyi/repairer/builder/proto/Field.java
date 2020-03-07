package com.wuyi.repairer.builder.proto;

import com.wuyi.repairer.builder.Utils;

import org.objectweb.asm.Type;

/**
 * Represent a field. Identified by {@link #name} only.
 */
public class Field {
    public final String name;
    public final Object value;
    public final Type type;
    public final int access;

    public Field(String name, Object value, int access, Type type) {
        this.name = name;
        this.value = value;
        this.access = access;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Field
                && Utils.strEquals(((Field) obj).name, name);
    }
}
