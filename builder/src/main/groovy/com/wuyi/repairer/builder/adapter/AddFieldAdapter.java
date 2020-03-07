package com.wuyi.repairer.builder.adapter;

import com.wuyi.repairer.builder.Config;
import com.wuyi.repairer.builder.proto.Field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * This adapter add a set of {@link Field} to a class.
 * todo support expression initialization
 */
public class AddFieldAdapter extends ClassVisitor {
    private final Set<Field> fields;
    private Set<Field> copyFields;

    public AddFieldAdapter(ClassVisitor cv, Set<Field> fields) {
        super(Config.ASM_VERSION, cv);
        this.fields = fields;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        copyFields = new HashSet<>();
        if (fields != null) {
            copyFields.addAll(fields);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        // remove the filed which is presented
        Field dummyField = new Field(name, null, 0, null);
        copyFields.remove(dummyField);
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visitEnd() {
        for (Field field : copyFields) {
            // does't support generic yet
            FieldVisitor fv = cv.visitField(field.access, field.name, field.type.getDescriptor(), null, field.value);
            if (fv != null) {
                fv.visitEnd();
            }
        }
        super.visitEnd();
    }
}
