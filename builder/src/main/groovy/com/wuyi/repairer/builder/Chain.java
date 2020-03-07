package com.wuyi.repairer.builder;


import org.objectweb.asm.ClassVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Helper class to deal with chain relation of {@link ClassVisitor}
 */
public class Chain {
    /**
     * Simply initialize and chain all the given ClassVisitor by order
     * @param clazzes  ClassVisitors needed to be chained
     * @return the first ClassVisitor int the chain
     */
    public static ClassVisitor chain(Class<? extends ClassVisitor>... clazzes) {
        ClassVisitor cv = null;
        for (int index = clazzes.length; index >= 0; index--) {
            Class<? extends ClassVisitor> clazz = clazzes[index];
            try {
                Constructor<? extends ClassVisitor> constructor = clazz.getConstructor(Integer.class, ClassVisitor.class);
                cv = constructor.newInstance(Config.ASM_VERSION, cv);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }
        return cv;
    }
}
