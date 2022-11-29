package me.weishu.dirtypipecheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCenter.start(getApplication(), "1be20f3b-2af7-4d2a-888e-3889f2b6fcf3",
                Analytics.class, Crashes.class);

        setContentView(R.layout.main);

        Button button = findViewById(R.id.btn);

        button.setOnClickListener(v -> {
            File tmpfile = new File(getCacheDir(), ".dirty_pipe_check");
            if (tmpfile.exists()) {
                tmpfile.delete();
            }
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tmpfile)) {
                fos.write("1111111111111111111111111111111111111111111111111111111".getBytes());
            } catch (Exception e) {
                Log.e(TAG, "err: ", e);
            }

            Check.check(tmpfile.getAbsolutePath());

            String content = "";
            try {
                content = new String(Files.readAllBytes(tmpfile.toPath()));
            } catch (IOException ignored) {
            }

            Log.w(TAG, "content: " + content);
            boolean vulnerable = content.contains("test");

            if (vulnerable) {
                Analytics.trackEvent("vulnerable", new HashMap<String, String>() {{
                    put("product", Build.PRODUCT);
                    put("model", Build.MODEL);
                    put("fingerprint", Build.FINGERPRINT);
                    put("os", System.getProperty("os.version"));
                }});
            } else {
                Analytics.trackEvent("invulnerable", new HashMap<String, String>() {{
                    put("product", Build.PRODUCT);
                    put("model", Build.MODEL);
                    put("fingerprint", Build.FINGERPRINT);
                    put("os", System.getProperty("os.version"));
                }});
            }

            button.setBackgroundColor(vulnerable ? Color.GREEN : Color.RED);

            if (vulnerable) {
                new AlertDialog.Builder(this)
                        .setTitle(android.R.string.dialog_alert_title)
                        .setMessage(R.string.vulnerable_tips)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.memu_getroot);
        item.setOnMenuItemClickListener(item1 -> {

            new AlertDialog.Builder(this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.get_root_confirm)
                    .setPositiveButton(R.string.i_know_it,
                            (dialog, which) -> {
                                Check.getRoot(MainActivity.this, getWindow());

                                new AlertDialog.Builder(this)
                                        .setTitle(android.R.string.dialog_alert_title)
                                        .setMessage(R.string.get_root_tips)
                                        .setPositiveButton(android.R.string.ok, (dialog1, which1) -> {
                                            new AlertDialog.Builder(this).setTitle(android.R.string.dialog_alert_title)
                                                    .setMessage(R.string.get_root_tips2)
                                                    .setCancelable(false)
                                                    .setPositiveButton(android.R.string.ok, (dialog2, which2) -> {
                                                        dialog2.dismiss();
                                                        dialog1.dismiss();
                                                        dialog.dismiss();
                                                    }).show();
                                        })
                                        .setCancelable(false)
                                        .show();

                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        });

        MenuItem about = menu.findItem(R.id.menu_about);
        about.setOnMenuItemClickListener(item1 -> {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.about)
                    .setMessage(R.string.author)
                    .show();
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }
}