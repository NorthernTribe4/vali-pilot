package com.vali.pilot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Vali Pilot — framework-only twin of the canonical Kotlin MainActivity.
 *
 * One fullscreen, landscape, immersive WebView that hosts the HUD page
 * (assets/index.html). The page plays the live MediaMTX WebRTC feed via WHEP
 * and drives every control; this Activity only configures the WebView and
 * bridges JS -> native (where MQTT publishing will be added later).
 *
 * This Java version exists ONLY so the build sandbox (which has no AGP / no
 * androidx / only the API-23 platform) can emit an installable APK. The Kotlin
 * project under android/app is the real source for a normal Android Studio /
 * Gradle build.
 */
public class MainActivity extends Activity {

    private static final String TAG = "ValiPilot";

    // ─────────────────────────────────────────────────────────────────────
    //  ROBOT ADDRESS — first-run default. A stable hostname (.local mDNS) or a
    //  raw LAN IP both work; the page does NOT validate it as numeric. Change
    //  it WITHOUT rebuilding via the in-app gear menu (▸ Robot Address); it
    //  persists in the WebView's DOM storage. Kept in sync with
    //  CONFIG.DEFAULT_HOST at the top of assets/index.html (the value the page
    //  actually uses). Feed URL is built as: http://<host>:8889/cam/whep
    // ─────────────────────────────────────────────────────────────────────
    public static final String DEFAULT_ROBOT_HOST = "aarush-virtualbox.local";

    private static final String HUD_URL = "file:///android_asset/index.html";

    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep the screen awake while piloting.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        setContentView(web);

        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);                       // page logic + WHEP client
        ws.setDomStorageEnabled(true);                       // persists the robot address
        ws.setMediaPlaybackRequiresUserGesture(false);       // autoplay the live <video>
        // file:// page pulls an http:// LAN stream -> "mixed content"; allow it.
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Hardware acceleration is on by default; pin the WebView to a HW layer
        // so video compositing stays smooth.
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // JS -> native bridge. Logs today; MQTT publish drops into command().
        web.addJavascriptInterface(new RobotBridge(), "ValiNative");

        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            // WHEP is receive-only so this normally won't fire, but granting
            // keeps playback robust if the stream ever negotiates media perms.
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(new Runnable() {
                    @Override public void run() { request.grant(request.getResources()); }
                });
            }
        });

        web.loadUrl(HUD_URL);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterImmersive();   // re-assert fullscreen on focus
    }

    private void enterImmersive() {
        getWindow().getDecorView().setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onDestroy() {
        if (web != null) web.destroy();
        super.onDestroy();
    }

    // ── Robot command bridge ────────────────────────────────────────────────
    //  The page calls window.ValiNative.command(topic, payloadJson) for every
    //  control action — drive vector, collect on/off, snapshot, record on/off.
    //  Topics are namespaced "vali/<action>"; payloads are JSON strings.
    //  RIGHT NOW: just logs (adb logcat -s ValiPilot).
    //  NEXT (MQTT): create one MQTT client in onCreate, then publish here:
    //      mqtt.publish(topic, new MqttMessage(payload.getBytes()));
    public class RobotBridge {
        @JavascriptInterface
        public void command(String topic, String payload) {
            Log.d(TAG, "command  " + topic + "  " + payload);
            // TODO(mqtt): mqtt.publish(topic, new MqttMessage(payload.getBytes()));
        }
    }
}
