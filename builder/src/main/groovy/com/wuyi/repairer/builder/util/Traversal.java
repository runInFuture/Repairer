package com.wuyi.repairer.builder.util;

import com.wuyi.repairer.builder.proto.Action;
import com.wuyi.repairer.builder.proto.Const;

import java.io.File;
import java.io.FileFilter;

public class Traversal {
    /**
     * A helper method to traversal all file match filter in the path.
     * @param root    root path. file or directory.
     * @param filter  file filter.
     * @param action  action to apply for each file.
     */
    public static void traversal(File root, FileFilter filter, Action<File> action) {
        if (root == null || !root.exists() || !filter.accept(root)) return;
        if (root.isDirectory()) {
            final File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    traversal(file, filter, action);
                }
            }
        } else {
            action.call(root);
        }
    }

    public final static FileFilter CLASS_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file != null
                    && file.exists()
                    && (file.isDirectory() || file.getName().endsWith(Const.File.CLASS_SUFFIX));
        }
    };
}
