package me.weishu.dirtypipecheck;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
            File tmpfile = new File(getCacheDir(), ".ditry_pipe_check");
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
        });
    }
}