package tw.nekomimi.nekogram.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;

import tw.nekomimi.nekogram.NekoConfig;

public class ZenigramUpdateHelper {

    public static final String UPDATER_JSON_URL = "https://raw.githubusercontent.com/Vechoix/Zenigram/main/updater.json";
    public static final String GITHUB_REPO_URL = "https://github.com/Vechoix/Zenigram";
    public static final String GITHUB_RELEASES_API = "https://api.github.com/repos/Vechoix/Zenigram/releases/latest";

    public static final String CURRENT_VERSION_NAME = org.telegram.messenger.BuildConfig.VERSION_NAME;
    public static final int CURRENT_VERSION_CODE = 7;

    private static volatile ZenigramUpdateHelper instance;
    private volatile boolean isChecking = false;
    private volatile boolean isDownloading = false;
    private volatile boolean cancelDownloadRequested = false;
    private final ArrayList<CheckCallback> pendingCallbacks = new ArrayList<>();
    private final Object checkLock = new Object();

    public static class UpdateInfo {
        public int versionCode = 1;
        public String versionName = "";
        public String downloadUrl = "";
        public String changelog = "";
        public long fileSize = 0;
        public boolean hasUpdate = false;
    }

    public interface CheckCallback {
        void onResult(UpdateInfo info, boolean hasUpdate, String error);
    }

    public interface DownloadCallback {
        void onProgress(float progress, long downloadedBytes, long totalBytes);
        void onSuccess(File file);
        void onError(String error);
    }

    public static ZenigramUpdateHelper getInstance() {
        if (instance == null) {
            synchronized (ZenigramUpdateHelper.class) {
                if (instance == null) {
                    instance = new ZenigramUpdateHelper();
                }
            }
        }
        return instance;
    }

    public boolean isChecking() {
        return isChecking;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void cancelDownload() {
        cancelDownloadRequested = true;
    }

    public void checkUpdate(final boolean force, final CheckCallback callback) {
        synchronized (checkLock) {
            if (callback != null) {
                pendingCallbacks.add(callback);
            }
            if (isChecking) {
                return;
            }
            isChecking = true;
        }

        AndroidUtilities.runOnUIThread(() -> {
            synchronized (checkLock) {
                if (isChecking && !pendingCallbacks.isEmpty()) {
                    isChecking = false;
                    ArrayList<CheckCallback> toNotify = new ArrayList<>(pendingCallbacks);
                    pendingCallbacks.clear();
                    UpdateInfo timeoutInfo = new UpdateInfo();
                    for (CheckCallback cb : toNotify) {
                        try {
                            cb.onResult(timeoutInfo, false, "Превышено время ожидания");
                        } catch (Exception ignored) {}
                    }
                }
            }
        }, 10000);

        Utilities.globalQueue.postRunnable(() -> {
            UpdateInfo info = new UpdateInfo();
            String error = null;

            try {
                // 1. Try reading updater.json from GitHub raw
                HttpURLConnection conn = (HttpURLConnection) new URL(UPDATER_JSON_URL).openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent", "Zenigram-Android");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setInstanceFollowRedirects(true);
                int code = conn.getResponseCode();

                if (code == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    info.versionCode = json.optInt("versionCode", 1);
                    info.versionName = json.optString("versionName", "1.0.0");
                    info.downloadUrl = json.optString("downloadUrl", "");
                    info.changelog = json.optString("changelog", "");
                } else {
                    // 2. Fallback to GitHub Releases API if updater.json returned non-200
                    HttpURLConnection gitConn = (HttpURLConnection) new URL(GITHUB_RELEASES_API).openConnection();
                    gitConn.setConnectTimeout(8000);
                    gitConn.setReadTimeout(8000);
                    gitConn.setRequestProperty("User-Agent", "Zenigram-Android");
                    gitConn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                    if (gitConn.getResponseCode() == 200) {
                        BufferedReader gitReader = new BufferedReader(new InputStreamReader(gitConn.getInputStream(), "UTF-8"));
                        StringBuilder gitSb = new StringBuilder();
                        String gitLine;
                        while ((gitLine = gitReader.readLine()) != null) {
                            gitSb.append(gitLine).append("\n");
                        }
                        gitReader.close();

                        JSONObject gitJson = new JSONObject(gitSb.toString());
                        String tag = gitJson.optString("tag_name", "").replaceAll("^v", "");
                        info.versionName = tag;
                        info.changelog = gitJson.optString("body", "");

                        JSONArray assets = gitJson.optJSONArray("assets");
                        if (assets != null) {
                            for (int i = 0; i < assets.length(); i++) {
                                JSONObject asset = assets.getJSONObject(i);
                                String name = asset.optString("name", "");
                                if (name.endsWith(".apk")) {
                                    info.downloadUrl = asset.optString("browser_download_url", "");
                                    info.fileSize = asset.optLong("size", 0);
                                    if (name.contains("arm64")) {
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        error = "HTTP " + code;
                    }
                }

                // Determine if remote version is newer
                boolean isNew = false;
                if (info.versionCode > CURRENT_VERSION_CODE && info.versionCode < 1000) {
                    isNew = true;
                } else if (info.versionCode > BuildConfig.VERSION_CODE) {
                    isNew = true;
                } else if (isVersionGreater(info.versionName, CURRENT_VERSION_NAME)) {
                    isNew = true;
                }

                info.hasUpdate = isNew;

                if (info.versionName.length() > 0) {
                    NekoConfig.saveUpdateCache(isNew, info.versionName, info.versionCode, info.changelog, info.downloadUrl);
                }

            } catch (Exception e) {
                FileLog.e(e);
                error = e.getMessage() != null ? e.getMessage() : "Ошибка сети";
            } finally {
                final UpdateInfo finalInfo = info;
                final String finalError = error;
                AndroidUtilities.runOnUIThread(() -> {
                    ArrayList<CheckCallback> toNotify;
                    synchronized (checkLock) {
                        isChecking = false;
                        toNotify = new ArrayList<>(pendingCallbacks);
                        pendingCallbacks.clear();
                    }
                    for (CheckCallback cb : toNotify) {
                        try {
                            cb.onResult(finalInfo, finalInfo.hasUpdate, finalError);
                        } catch (Exception ignored) {}
                    }
                });
            }
        });
    }

    public void downloadApk(final String downloadUrl, final DownloadCallback callback) {
        if (isDownloading) {
            return;
        }
        isDownloading = true;
        cancelDownloadRequested = false;

        Utilities.globalQueue.postRunnable(() -> {
            File outFile = null;
            String error = null;

            try {
                String currentUrl = downloadUrl;
                HttpURLConnection conn = null;

                // Follow redirects manually to handle GitHub -> AWS S3 redirect
                for (int redirectCount = 0; redirectCount < 6; redirectCount++) {
                    conn = (HttpURLConnection) new URL(currentUrl).openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);
                    conn.setRequestProperty("User-Agent", "Zenigram-Android");
                    conn.setInstanceFollowRedirects(false);

                    int status = conn.getResponseCode();
                    if (status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_SEE_OTHER
                            || status == 307
                            || status == 308) {
                        String newUrl = conn.getHeaderField("Location");
                        if (newUrl != null) {
                            currentUrl = newUrl;
                            conn.disconnect();
                            continue;
                        }
                    }
                    break;
                }

                if (conn == null || conn.getResponseCode() != 200) {
                    int respCode = conn != null ? conn.getResponseCode() : -1;
                    throw new Exception("Не удалось загрузить APK (HTTP " + respCode + ")");
                }

                File cacheDir = ApplicationLoader.applicationContext.getExternalCacheDir();
                if (cacheDir == null) {
                    cacheDir = ApplicationLoader.applicationContext.getCacheDir();
                }
                outFile = new File(cacheDir, "Zenigram_Update.apk");
                if (outFile.exists()) {
                    outFile.delete();
                }

                long totalLength = conn.getContentLengthLong();
                InputStream in = conn.getInputStream();
                FileOutputStream out = new FileOutputStream(outFile);

                byte[] buffer = new byte[16384];
                int read;
                long downloaded = 0;
                long lastNotifyTime = 0;

                while ((read = in.read(buffer)) != -1) {
                    if (cancelDownloadRequested) {
                        in.close();
                        out.close();
                        if (outFile.exists()) {
                            outFile.delete();
                        }
                        isDownloading = false;
                        AndroidUtilities.runOnUIThread(() -> {
                            if (callback != null) {
                                callback.onError("Загрузка отменена");
                            }
                        });
                        return;
                    }

                    out.write(buffer, 0, read);
                    downloaded += read;

                    long now = System.currentTimeMillis();
                    if (now - lastNotifyTime > 80 || downloaded == totalLength) {
                        lastNotifyTime = now;
                        final float progress = totalLength > 0 ? (float) downloaded / (float) totalLength : 0f;
                        final long d = downloaded;
                        final long t = totalLength;
                        AndroidUtilities.runOnUIThread(() -> {
                            if (callback != null) {
                                callback.onProgress(progress, d, t);
                            }
                        });
                    }
                }

                out.flush();
                out.close();
                in.close();

                if (!outFile.exists() || outFile.length() < 1000000) {
                    throw new Exception("Файл обновления поврежден или загружен не полностью");
                }

            } catch (Exception e) {
                FileLog.e(e);
                error = e.getMessage() != null ? e.getMessage() : "Ошибка при загрузке";
                if (outFile != null && outFile.exists()) {
                    outFile.delete();
                }
                outFile = null;
            } finally {
                isDownloading = false;
                final File finalFile = outFile;
                final String finalError = error;
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        if (finalFile != null) {
                            callback.onSuccess(finalFile);
                        } else {
                            callback.onError(finalError);
                        }
                    }
                });
            }
        });
    }

    public static void installApk(Activity activity, File apkFile) {
        if (activity == null || apkFile == null || !apkFile.exists()) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!activity.getPackageManager().canRequestPackageInstalls()) {
                    Intent grantIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    grantIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                    grantIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(grantIntent);
                    return;
                }
            }

            Uri apkUri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", apkFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static boolean isVersionGreater(String remote, String current) {
        if (remote == null || current == null) return false;
        remote = remote.trim().replaceAll("^v", "");
        current = current.trim().replaceAll("^v", "");
        if (remote.equals(current)) return false;

        String[] rParts = remote.split("\\.");
        String[] cParts = current.split("\\.");
        int len = Math.max(rParts.length, cParts.length);

        for (int i = 0; i < len; i++) {
            int r = 0, c = 0;
            if (i < rParts.length) {
                try {
                    r = Integer.parseInt(rParts[i].replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {
                }
            }
            if (i < cParts.length) {
                try {
                    c = Integer.parseInt(cParts[i].replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {
                }
            }
            if (r > c) return true;
            if (r < c) return false;
        }
        return false;
    }
}
