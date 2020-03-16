/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wuyi.repairer.builder.instrument;

import com.android.annotations.NonNull;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;

/**
 * A specialized redirection that handles redirecting the part that redirects the
 * argument construction for the super()/this() call in a constructor.
 * <p>
 * Note that the generated bytecode does not have a direct translation to code, but as an
 * example, for a constructor of the form:
 * <pre>{@code
 *   <init>((int x) {
 *     int a = 2;
 *     super(int b = 3, x = 1, expr2() ? 3 : a++)
 *     doSomething(x + a)
 *   }
 * }</pre>
 * <p>
 * it becomes:
 * <pre>{@code
 *   <init>(int x) {
 *     int a = 2; // Prelude remains unchanged.
 *     Change change = $change; // Move to a variable to avoid multithreading issues.
 *     if (change != null) {
 *       Object[] arguments = new Object[3];
 *       arguments[0] = NULL; // The "this" reference. NULL in init$args.
 *       arguments[1] = x;
 *       arguments[2] = new Object[] { a }; // The local variables up to this point;
 *       Object[] result = change.access$dispatch("init$args", arguments);
 *       this(result, null);
 *       // result[0] contains the new arguments with the new locals.
 *       change.access$dispatch("init$body", result[0]);
 *       return;
 *     }
 *     super(int b = 3, x = 1, expr2() ? 3 : a++)
 *     doSomething(x + a)
 *  }
 * }</pre>
 *
 * @see ConstructorBuilder for the generation of init$args and init$body.
 */
public class ConstructorRedirection extends Redirection {

    // The signature of the dynamically dispatching 'this' constructor. The final parameters is
    // to disambiguate from other constructors that might preexist on the class.
    static final String DISPATCHING_THIS_SIGNATURE =
            "([Ljava/lang/Object;"
                    + IncrementalVisitor.INSTANT_RELOAD_EXCEPTION.getDescriptor() + ")V";

    private final Constructor constructor;

    /**
     * @param constructor the constructor to redirect.
     * @param types the types of the arguments on the super()/this() call.
     */
    ConstructorRedirection(LabelNode label,
            Constructor constructor,
            @NonNull List<Type> types) {
        super(label, types, Type.VOID_TYPE);
        this.constructor = constructor;
    }

    @Override
    protected void doRedirect(GeneratorAdapter mv, int change) {
        mv.loadLocal(change);
        mv.push("init$args." + constructor.args.desc);

        Type arrayType = Type.getType("[Ljava/lang/Object;");
        // init$args args (including this) + locals
        mv.push(types.size() + 1);
        mv.newArray(Type.getType(Object.class));

        int array = mv.newLocal(arrayType);
        mv.dup();
        mv.storeLocal(array);

        // "this" is not ready yet, use null instead.
        mv.dup();
        mv.push(0);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.arrayStore(Type.getType(Object.class));

        // Set the arguments in positions 1..(n-1);
        ByteCodeUtils.loadVariableArray(mv, ByteCodeUtils.toLocalVariables(types), 1); // Skip the this value

        // Add the locals array at the last position.
        mv.dup();
        // The index of the last position of the array.
        mv.push(types.size());
        // Create the array with all the local variables declared up to this point.
        ByteCodeUtils.newVariableArray(mv, constructor.variables.subList(0, constructor.localsAtLoadThis));
        mv.arrayStore(Type.getType(Object.class));

        mv.invokeInterface(IncrementalVisitor.CHANGE_TYPE, Method.getMethod("Object access$dispatch(String, Object[])"));
        mv.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
        //// At this point, init$args has been called and the result Object is on the stack.
        //// The value of that Object is Object[] with exactly n + 2 elements.
        //// The first element is the resulting local variables
        //// The second element is a string with the qualified name of the constructor to call.
        //// The remaining elements are the constructor arguments.

        // Keep a reference to the new locals array
        mv.dup();
        mv.push(0);
        mv.arrayLoad(Type.getType("[Ljava/lang/Object;"));
        mv.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
        mv.storeLocal(array);

        // Call super constructor
        // Put this behind the returned array
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.swap();
        // Push a null for the marker parameter.
        mv.visitInsn(Opcodes.ACONST_NULL);
        // Invoke the constructor
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, constructor.owner, "<init>", DISPATCHING_THIS_SIGNATURE, false);

        // Dispatch to init$body
        mv.loadLocal(change);
        mv.push("init$body." + constructor.body.desc);
        mv.loadLocal(array);

        // Now "this" can be set
        mv.dup();
        mv.push(0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.arrayStore(Type.getType(Object.class));

        mv.invokeInterface(IncrementalVisitor.CHANGE_TYPE, Method.getMethod("Object access$dispatch(String, Object[])"));
        mv.pop();
    }
}
