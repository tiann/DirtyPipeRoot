package me.weishu.dirtypipecheck;

import android.app.Activity;
import android.graphics.Color;
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


            try {
                String content = new String(Files.readAllBytes(tmpfile.toPath()));
                Log.w(TAG, "content: " + content);
                boolean vunlerable = content.contains("test");

                Analytics.trackEvent("vunlerable", new HashMap<String, String>() {{
                    put("vunlerable", String.valueOf(vunlerable));
                }});

                button.setBackgroundColor(vunlerable ? Color.GREEN : Color.RED);
            } catch (IOException e) {
                Log.e(TAG, "err", e);
            }
        });
    }
}