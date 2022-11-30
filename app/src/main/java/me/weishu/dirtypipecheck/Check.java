package me.weishu.dirtypipecheck;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Random;

/**
 * @author weishu
 * @date 2022/11/26.
 */

public final class Check {

    private static final String TAG = "DirtyPipeCheck";

    static {
        System.loadLibrary("dirtypipecheck");
    }

    public static native void check(String path);

    public static void getRoot(Context context, Window window) {

        File baseDir = new File(context.getCacheDir(), "dirtypipe");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        extractAssets(context, baseDir);

        String nativeLibraryDir = context.getApplicationInfo().nativeLibraryDir;
        String path = nativeLibraryDir + "/libdirtypipe.so";

        String cmd = String.format("export BASE_DIR=%s;%s -f",baseDir.getAbsolutePath(), path);

        Log.w(TAG, "cmd: " + cmd);

        ShellUtils.CommandResult commandResult = ShellUtils.execCmd(cmd, false);
        Log.i(TAG, "result: " + commandResult);

        // trigger init property change!
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = randomFloat(0.01f, 0.5f);
        Log.i(TAG, "brightness: " + lp.screenBrightness);
        window.setAttributes(lp);

        SystemClock.sleep(100);

        lp.screenBrightness = randomFloat(0.6f, 1.0f);
        Log.i(TAG, "brightness: " + lp.screenBrightness);
        window.setAttributes(lp);
    }

    public static float randomFloat(float min, float max) {
        Random random = new Random();
        return random.nextFloat() * (max - min) + min;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void extractAssets(Context context, File baseDir) {
        AssetManager assets = context.getAssets();

        // copy startup-root
        File startupRoot = new File(baseDir, "startup-root");
        try (InputStream in = assets.open("startup-root")) {
            Files.copy(in, startupRoot.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        startupRoot.setExecutable(true, false);

        // copy env-patcher
        File envPatcher = new File(baseDir, "env-patcher");
        try (InputStream in = assets.open("env-patcher")) {
            Files.copy(in, envPatcher.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        envPatcher.setExecutable(true, false);

        // copy magisk dir
        File magiskDir = new File(baseDir, "magisk");
        magiskDir.mkdirs();

        try {
            String[] files = assets.list("magisk");

            for (String file : files) {
                File target = new File(magiskDir, file);
                if (!target.exists()) {
                    target.createNewFile();
                }
                InputStream stream = assets.open("magisk/" + file);
                Files.copy(stream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                stream.close();
                target.setExecutable(true, false);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
