package com.wuyi.repairer.builder;

import com.wuyi.repairer.builder.instrument.AddFieldAdapter;
import com.wuyi.repairer.builder.proto.Field;
import com.wuyi.repairer.proto.Const;
import com.wuyi.repairer.runtime.IncrementalChange;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
     * @param classFile the .class file
     * @return the instrumented class data
     */
    public byte[] instrument(File classFile) {
        ClassWriter cw = new ClassWriter(0);
        Set<Field> fields = new HashSet<>();
        fields.add(newChangeField());
        AddFieldAdapter addFieldAdapter = new AddFieldAdapter(cw, fields);
        try {
            ClassReader cr = new ClassReader(new FileInputStream(classFile));
            cr.accept(addFieldAdapter, 0 /* skip nothing */);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cw.toByteArray();
    }

    private Field newChangeField() {
        return new Field(Const.changeFieldName, null, ACC_PUBLIC, Type.getType(IncrementalChange.class));
    }
}
