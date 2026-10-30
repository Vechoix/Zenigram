package tw.nekomimi.nekogram.helpers;

import android.content.Context;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.Utilities;

import java.io.File;

public class ZenigramCacheHelper {

    public interface CacheCallback {
        void onDone(long clearedBytes);
    }

    public static void clearCacheAsync(CacheCallback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            long clearedBytes = 0;
            try {
                Context ctx = ApplicationLoader.applicationContext;
                if (ctx != null) {
                    clearedBytes += cleanDirectoryContents(ctx.getCacheDir());
                    clearedBytes += cleanDirectoryContents(ctx.getExternalCacheDir());
                    clearedBytes += cleanDirectoryContents(ctx.getCodeCacheDir());
                    File mediaCache = FileLoader.checkDirectory(FileLoader.MEDIA_DIR_CACHE);
                    if (mediaCache != null) {
                        clearedBytes += cleanDirectoryContents(mediaCache);
                    }
                    // Make sure root cache directories remain present
                    File cacheDir = ctx.getCacheDir();
                    if (cacheDir != null && !cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    if (mediaCache != null && !mediaCache.exists()) {
                        mediaCache.mkdirs();
                    }
                }
                ImageLoader.getInstance().clearMemory();
            } catch (Throwable e) {
                FileLog.e(e);
            }

            final long finalCleared = clearedBytes;
            AndroidUtilities.runOnUIThread(() -> {
                if (callback != null) {
                    callback.onDone(finalCleared);
                }
            });
        });
    }

    private static long cleanDirectoryContents(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return 0;
        }
        long deletedBytes = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deletedBytes += cleanDirectoryContents(file);
                    try {
                        file.delete();
                    } catch (Throwable ignored) {}
                } else {
                    deletedBytes += file.length();
                    try {
                        file.delete();
                    } catch (Throwable ignored) {}
                }
            }
        }
        return deletedBytes;
    }
}
