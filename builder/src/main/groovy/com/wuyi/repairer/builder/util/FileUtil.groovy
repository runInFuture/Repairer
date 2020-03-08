package com.wuyi.repairer.builder.util

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
}