package org.ib.utils;

import org.ib.component.base.Publisher;
import org.ib.logger.Logger;

import java.io.File;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/19/13
 */
public class FileUtils {
    public static File createCacheDir(Publisher concernedComponent, String cacheDir) {
        return createCacheDir(concernedComponent, cacheDir == null ? null : new File(cacheDir));
    }

    public static File createCacheDir(Publisher concernedComponent, File cacheDir) {
        if (cacheDir == null) {
            cacheDir = new File("tmpCache");
        }
        File resultCache = cacheDir.getAbsoluteFile();

        if (!resultCache.exists()) {
            if (!resultCache.mkdir()) {
                Logger.log(concernedComponent, Logger.CRITICAL, "Cannot create audio cache in path = " + resultCache.getAbsolutePath());
                throw new IllegalArgumentException("Invalid cache path provided: " + resultCache.getAbsolutePath());
            }
        } else if (!resultCache.canWrite()) {
            Logger.log(concernedComponent, Logger.CRITICAL, "Cannot write in given path = " + resultCache.getAbsolutePath());
            throw new IllegalArgumentException("Invalid cache path provided: " + resultCache.getAbsolutePath());
        }
        Logger.log(concernedComponent, Logger.INFORM, "Using cache: " + resultCache);
        return resultCache;
    }

    public static boolean checkReadableFile(File filePath, boolean failOnInvalid) {
        if (filePath.exists() && filePath.canRead()) {
            return true;
        } else {
            if (failOnInvalid) {
                throw new IllegalArgumentException("Cannot read the given path = " + filePath.getAbsolutePath());
            } else {
                return false;
            }
        }
    }

    public static boolean checkWritableFile(File filePath, boolean failOnInvalid) {
        if (filePath.exists() && filePath.canWrite()) {
            return true;
        } else {
            if (failOnInvalid) {
                throw new IllegalArgumentException("Cannot write on given path = " + filePath.getAbsolutePath());
            } else {
                return false;
            }
        }
    }

    public static boolean checkExecutableFile(File filePath, boolean failOnInvalid) {
        if (filePath.exists() && filePath.canExecute()) {
            return true;
        } else {
            if (failOnInvalid) {
                throw new IllegalArgumentException("Cannot find executable on given path = " + filePath.getAbsolutePath());
            } else {
                return false;
            }
        }
    }
}
