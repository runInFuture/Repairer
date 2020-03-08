package com.wuyi.repairer.builder.tasks.tansform;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.variant.VariantInfo;
import com.wuyi.repairer.builder.ClassInstrumentor;
import com.wuyi.repairer.builder.Logger;
import com.wuyi.repairer.builder.proto.Action;
import com.wuyi.repairer.builder.util.FileUtil;
import com.wuyi.repairer.builder.util.Traversal;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This transform add the field to all class in application.
 * The actual work is done by {@link com.wuyi.repairer.builder.ClassInstrumentor}
 */
public class InstrumentTransform extends Transform {
    private Logger logger = Logger.getInstance();
    private ClassInstrumentor instrumentor = new ClassInstrumentor();

    @Override
    public String getName() {
        return "repairerInject";
    }

    @Override
    public boolean applyToVariant(@SuppressWarnings("UnstableApiUsage") VariantInfo variant) {
        // only work for release variant
        return variant.getBuildTypeName().contains("release");
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        Set<QualifiedContent.ContentType> set = new HashSet<>();
        set.add(QualifiedContent.DefaultContentType.CLASSES);
        return set;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        Set<QualifiedContent.Scope> set = new HashSet<>();
        set.add(QualifiedContent.Scope.PROJECT);
        return set;
    }

    @Override
    public boolean isIncremental() {
        // todo what is 'incremental' exactly mean?
        return true;
    }

    @Override
    public void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {
        // the worker method
        logger.log(getName() + " invoke: " + "isIncremental: " + invocation.isIncremental());
        for (TransformInput input : invocation.getInputs()) {
            logger.log("process input: " + input.toString());
            for (DirectoryInput directory : input.getDirectoryInputs()) {
                logger.log("process directoryInput: " + directory.getName());
                for (Map.Entry<File, Status> entry : directory.getChangedFiles().entrySet()) {
                    // deal the add/change file only
                    if (entry.getValue() == Status.CHANGED || entry.getValue() == Status.ADDED) {
                        Traversal.traversal(entry.getKey(), Traversal.CLASS_FILTER, new Action<File>() {
                            @Override
                            public void call(File file) {
                                logger.log("process file: " + file.getName());
                                FileUtil.replace(file, instrumentor.instrument(file));
                            }
                        });
                    }
                }
            }

            for (JarInput jar : input.getJarInputs()) {
                logger.log("process jarInput: " + jar.getName());
            }
        }
    }
}
