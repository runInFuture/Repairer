package com.wuyi.repairer.builder.util;

import com.wuyi.repairer.builder.proto.Action;
import com.wuyi.repairer.proto.Const;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Traversal {
    /**
     * A helper method to traversal all file(EXCLUDE directory) match filter in the path.
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

    /**
     * A helper method to traversal all file(EXCLUDE directory) in a jar file.
     * @param jarFile     a .jar file
     * @param nameFilter  file filter.
     * @param onJarEntry  action to apply for each file.
     */
    public static void traversal(JarFile jarFile, Filter<String> nameFilter, OnJarEntry onJarEntry) {
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry entry = jarEntries.nextElement();
            if (!entry.isDirectory() && nameFilter.accept(entry.getName())) {
                try {
                    onJarEntry.onJarEntry(FileUtil.toByteArray(jarFile.getInputStream(entry)), entry.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface Filter<T> {
        boolean accept(T t);
    }

    public interface OnJarEntry {
        void onJarEntry(byte[] bytes, String name);
    }

    public final static Filter DOT_CLASS_FILTER = new Filter<String>() {
        @Override
        public boolean accept(String s) {
            return s.endsWith(Const.File.CLASS_SUFFIX);
        }
    };

    public final static FileFilter CLASS_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file != null
                    && file.exists()
                    && (file.isDirectory() || DOT_CLASS_FILTER.accept(file.getName()));
        }
    };

    public final static FileFilter NO_OP_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return true;
        }
    };
}
