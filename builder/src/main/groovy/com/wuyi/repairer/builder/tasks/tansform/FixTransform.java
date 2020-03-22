package com.wuyi.repairer.builder.tasks.tansform;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.LoggerWrapper;
import com.android.build.gradle.internal.pipeline.ExtendedContentType;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.android.utils.ILogger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.wuyi.repairer.builder.Logger;
import com.wuyi.repairer.builder.instrument.IncrementalChangeVisitor;
import com.wuyi.repairer.builder.instrument.IncrementalVisitor;
import com.wuyi.repairer.builder.proto.Action;
import com.wuyi.repairer.builder.util.FileUtil;
import com.wuyi.repairer.builder.util.Traversal;

import org.gradle.api.logging.Logging;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Generate the main
 */
public class FixTransform extends Transform {
    protected static final ILogger LOGGER =
            new LoggerWrapper(Logging.getLogger(InstantRunTransform.class));
    private final ImmutableList.Builder<String> generatedClasses3Names = ImmutableList.builder();

    @Override
    public String getName() {
        return "repairerFix";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @NonNull
    @Override
    public Set<QualifiedContent.ContentType> getOutputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {

        TransformOutputProvider outputProvider = invocation.getOutputProvider();
        File classesTwoOutput =
                outputProvider.getContentLocation(
                        "classes", TransformManager.CONTENT_CLASS, getScopes(), Format.DIRECTORY);

        final File classesThreeOutput =
                outputProvider.getContentLocation(
                        "enhanced_classes",
                        ImmutableSet.of(ExtendedContentType.CLASSES_ENHANCED),
                        getScopes(),
                        Format.DIRECTORY);

        // first get all referenced input to construct a class loader capable of loading those
        // classes. This is useful for ASM as it needs to load classes
        List<URL> referencedInputUrls = getAllClassesLocations(
                invocation.getInputs(), invocation.getReferencedInputs());

        // This class loader could be optimized a bit, first we could create a parent class loader
        // with the android.jar only that could be stored in the GlobalScope for reuse. This
        // class loader could also be store in the VariantScope for potential reuse if some
        // other transform need to load project's classes.
        final URLClassLoader urlClassLoader = new InstantRunTransform.NonDelegatingUrlClassloader(referencedInputUrls);

        ClassLoader currentThreadClassLoader =
                Thread.currentThread().getContextClassLoader();
        // temporal replace the thread class load, for doWork method inner used
        Thread.currentThread().setContextClassLoader(urlClassLoader);

        for (TransformInput input : invocation.getInputs()) {
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File inputDir = directoryInput.getFile();
                // non incremental mode, we need to traverse the TransformInput#getFiles()
                // folder
                FileUtils.cleanOutputDir(classesTwoOutput);
                Traversal.traversal(inputDir, Traversal.CLASS_FILTER, new Action<File>() {
                    @Override
                    public void call(File classFile) {
                        Logger.getInstance().log("process: " + classFile.getPath());
                        try {
                            transformToClasses3Format(inputDir, classFile, classesThreeOutput);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        wrapUpOutputs(classesTwoOutput, classesThreeOutput);
    }

    /**
     * Transform a single file into a {@link ExtendedContentType#CLASSES_ENHANCED} format
     *
     * @param inputDir the input directory containing the input file.
     * @param inputFile the input file within the input directory to transform.
     * @param outputDir the output directory where to place the transformed file.
     * @throws IOException if the transformation failed.
     */
    @Nullable
    protected Void transformToClasses3Format(File inputDir, File inputFile, File outputDir)
            throws IOException {

        File outputFile =
                IncrementalVisitor.instrumentClass(
                        21 /* ? */,
                        inputDir,
                        inputFile,
                        outputDir,
                        IncrementalChangeVisitor.VISITOR_BUILDER,
                        LOGGER);

        // if the visitor returned null, that means the class cannot be hot swapped or more likely
        // that it was disabled for InstantRun, we don't add it to our collection of generated
        // classes and it will not be part of the Patch class that apply changes.
        if (outputFile == null) {
            LOGGER.info("Class %s cannot be hot swapped.", inputFile);
            // fixme
            return null;
        }
        generatedClasses3Names.add(
                inputFile.getAbsolutePath().substring(
                        inputDir.getAbsolutePath().length() + 1,
                        inputFile.getAbsolutePath().length() - ".class".length())
                        .replace(File.separatorChar, '.'));
        return null;
    }

    protected void wrapUpOutputs(File classes2Folder, File classes3Folder)
            throws IOException {
        // generate the patch file and add it to the list of files to process next.
        ImmutableList<String> generatedClassNames = generatedClasses3Names.build();
        if (!generatedClassNames.isEmpty()) {
            writePatchFileContents(
                    generatedClassNames,
                    classes3Folder,
                    9527 /* ? */);
        }
    }

    /**
     * Use asm to generate a concrete subclass of the AppPathLoaderImpl class.
     * It only implements one method :
     *      String[] getPatchedClasses();
     *
     * The method is supposed to return the list of classes that were patched in this iteration.
     * This will be used by the InstantRun runtime to load all patched classes and register them
     * as overrides on the original classes.2 class files.
     *
     * @param patchFileContents list of patched class names.
     * @param outputDir output directory where to generate the .class file in.
     */
    private static void writePatchFileContents(
            @NonNull ImmutableList<String> patchFileContents, @NonNull File outputDir, long buildId) {

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                IncrementalVisitor.APP_PATCHES_LOADER_IMPL, null,
                IncrementalVisitor.ABSTRACT_PATCHES_LOADER_IMPL, null);

        // Add the build ID to force the patch file to be repackaged.
        cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
                "BUILD_ID", "J", null, buildId);

        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    IncrementalVisitor.ABSTRACT_PATCHES_LOADER_IMPL,
                    "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                    "getPatchedClasses", "()[Ljava/lang/String;", null, null);
            mv.visitCode();
            mv.visitIntInsn(Opcodes.BIPUSH, patchFileContents.size());
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
            for (int index=0; index < patchFileContents.size(); index++) {
                mv.visitInsn(Opcodes.DUP);
                mv.visitIntInsn(Opcodes.BIPUSH, index);
                mv.visitLdcInsn(patchFileContents.get(index));
                mv.visitInsn(Opcodes.AASTORE);
            }
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(4, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

        byte[] classBytes = cw.toByteArray();
        File outputFile = new File(outputDir, IncrementalVisitor.APP_PATCHES_LOADER_IMPL + ".class");
        try {
            Files.createParentDirs(outputFile);
            Files.write(classBytes, outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculate a list of {@link URL} that represent all the directories containing classes
     * either directly belonging to this project or referencing it.
     *
     * @param inputs the project's inputs
     * @param referencedInputs the project's referenced inputs
     * @return a {@link List} or {@link URL} for all the locations.
     * @throws MalformedURLException if once the locatio
     */
    @NonNull
    private List<URL> getAllClassesLocations(
            @NonNull Collection<TransformInput> inputs,
            @NonNull Collection<TransformInput> referencedInputs) throws MalformedURLException {

        List<URL> referencedInputUrls = new ArrayList<>();

        // add the bootstrap classpath for jars like android.jar
        for (File file : getInstantRunBootClasspath()) {
            referencedInputUrls.add(file.toURI().toURL());
        }

        // now add the project dependencies.
        for (TransformInput referencedInput : referencedInputs) {
            addAllClassLocations(referencedInput, referencedInputUrls);
        }

        // and finally add input folders.
        for (TransformInput input : inputs) {
            addAllClassLocations(input, referencedInputUrls);
        }
        return referencedInputUrls;
    }

    private static void addAllClassLocations(TransformInput transformInput, List<URL> into)
            throws MalformedURLException {

        for (DirectoryInput directoryInput : transformInput.getDirectoryInputs()) {
            into.add(directoryInput.getFile().toURI().toURL());
        }
        for (JarInput jarInput : transformInput.getJarInputs()) {
            into.add(jarInput.getFile().toURI().toURL());
        }
    }

    private ImmutableList<File> getInstantRunBootClasspath() {
        return ImmutableList.<File>builder()
                .add(getAndroidJar())
                .build();
    }

    private File getAndroidJar() {
        return FileUtil.getAndroidJar();
    }
}
