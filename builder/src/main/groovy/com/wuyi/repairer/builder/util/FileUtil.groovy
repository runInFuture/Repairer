package com.wuyi.repairer.builder.util

import com.wuyi.repairer.builder.Context
import org.gradle.api.Project
import org.jetbrains.annotations.NotNull

class FileUtil {
    /**
     * Simply replace the file content into the given bytes
     * @param file         the file which content need to be override
     * @param replacement  the new content
     */
    static void replace(@NotNull File file, byte[] replacement) {
        ensureExist(file)
        file.with {
            setBytes(replacement)
        }
    }

    static void ensureExist(File file) {
        if (!file.exists()) {
            file.mkdir()
        }
    }

    static File getAndroidJar() {
        Project project = Context.of().project
        return new File("${project.android.getSdkDirectory()}/platforms/${project.android.getCompileSdkVersion()}/android.jar")
    }

    static byte[] toByteArray(final InputStream input) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream()
        final byte[] buffer = new byte[8024]
        int n = 0
        long count = 0
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n)
            count += n
        }
        return output.toByteArray()
    }
}