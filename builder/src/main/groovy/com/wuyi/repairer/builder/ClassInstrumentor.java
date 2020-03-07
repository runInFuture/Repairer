package com.wuyi.repairer.builder;

import com.wuyi.repairer.annotation.Change;
import com.wuyi.repairer.builder.adapter.AddFieldAdapter;
import com.wuyi.repairer.builder.proto.Const;
import com.wuyi.repairer.builder.proto.Field;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

/**
 * This class present to do all the work needed to instrument a class.
 * Specifically, add a {@link Const#changeFieldName} field to each class,
 * and delegate all method call received to it.
 */
public class ClassInstrumentor {
    public ClassInstrumentor() {

    }

    /**
     * add a {@link Const#changeFieldName} field to the class given.
     * @param clazzStream the input stream of class data
     * @return the instrumented class data
     */
    private byte[] instrument(InputStream clazzStream) {
        ClassWriter cw = new ClassWriter(0);
        Set<Field> fields = new HashSet<>();
        fields.add(newChangeField());
        AddFieldAdapter addFieldAdapter = new AddFieldAdapter(cw, fields);
        try {
            ClassReader cr = new ClassReader(clazzStream);
            cr.accept(addFieldAdapter, 0 /* skip nothing */);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cw.toByteArray();
    }

    private Field newChangeField() {
        return new Field(Const.changeFieldName, null, ACC_PUBLIC, Type.getType(Change.class));
    }
}
