package com.wuyi.repairer.patch;

import android.os.Environment;
import android.util.Log;

import com.wuyi.repairer.runtime.AbstractPatchesLoaderImpl;
import com.wuyi.repairer.runtime.AndroidInstantRuntime;

import java.io.File;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * SHOULD be move to :lib
 */
public class Repairer {
    private static final String patchesLoaderImplName = "com.wuyi.repairer.runtime.AppPatchesLoaderImpl";
    public static final String patchPath = Environment.getExternalStorageDirectory() + "/fix.apk";

    public void apply(Patch patch) {
        try {
            Log.d("repairer", "start patch " + patch);
            if (!verifyPatch(patch)) {
                throw new IllegalArgumentException("patch is not valid!");
            }
            InstantRunClassLoader cl = new InstantRunClassLoader(patch.apkFile.getPath(), patch.optDir, getClass().getClassLoader());
            Class<?> implClazz = Class.forName(patchesLoaderImplName, true, cl);
            AbstractPatchesLoaderImpl impl = (AbstractPatchesLoaderImpl) implClazz.newInstance();
            impl.load();
            Log.d("repairer", "patch success!");
        } catch (Exception e) {
            Log.d("repairer", "patch fail with exception: " + e);
        }
    }

    private boolean verifyPatch(Patch patch) {
        return patch.apkFile != null && patch.apkFile.exists();
    }

    public static class Patch {
        public File apkFile = new File(patchPath);
        public File optDir;

        @Override
        public String toString() {
            return "patch{" +
                    "file: " + apkFile +
                    "}";
        }
    }

    public class InstantRunClassLoader extends BaseDexClassLoader {

        public InstantRunClassLoader(String dexPath, File optimizedDirectory, ClassLoader parent) {
            super(dexPath, optimizedDirectory, null, parent);
        }

        /**
         * find a native code library.
         *
         * @param libraryName the name of the library.
         * @return the String of a path name to the library or <code>null</code>.
         * @category ClassLoader
         * @see ClassLoader#findLibrary(String)
         */
        public String  findLibrary(final String libraryName) {

            try {
                return (String) AndroidInstantRuntime.invokeProtectedMethod(this.getParent(), new Object[]{libraryName},
                        new Class[]{String.class}, "findLibrary");
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return null;
        }

    }
}
