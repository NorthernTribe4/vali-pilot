package com.vali.pilot

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.RoundedCorner
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Locale

/**
 * Vali Pilot — a single fullscreen, landscape, immersive WebView that hosts the
 * HUD page (assets/index.html). The page plays the live MediaMTX WebRTC feed via
 * WHEP and drives all the controls; this Activity configures the WebView and
 * provides the JS -> native bridge where MQTT publishing will be added later.
 *
 * Display cutout handling: the window is drawn edge-to-edge ACROSS the cutout so
 * the camera feed stays truly full-bleed. We then measure the real per-edge safe
 * insets (cutout + system bars + rounded corners) and inject them into the page's
 * CSS --sa-* variables, so only the HUD controls inset away from the unsafe
 * regions — the video underneath still fills the whole screen.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ValiPilot"

        // ─────────────────────────────────────────────────────────────────────
        //  ROBOT ADDRESS
        //  A stable hostname (mDNS / .local) or a raw LAN IP — both work; the
        //  page does NOT validate it as numeric, so a hostname is fine.
        //  Change it WITHOUT rebuilding via the in-app gear menu
        //  (▸ Robot Address) — it persists in the WebView's DOM storage.
        //  The constant below is only the FIRST-RUN default and is kept in sync
        //  with CONFIG.DEFAULT_HOST at the top of index.html (which is the value
        //  the page actually uses on first launch).
        //  The page builds the feed URL as:  http://<host>:8889/cam/whep
        //  (port + path are CONFIG constants at the top of index.html).
        // ─────────────────────────────────────────────────────────────────────
        const val DEFAULT_ROBOT_HOST = "aarush-virtualbox.local"

        private const val HUD_URL = "file:///android_asset/index.html"
    }

    private lateinit var web: WebView

    /** Latest safe-area injection JS, re-applied after each page load. */
    private var insetCss: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw edge-to-edge; system bars are hidden in enterImmersive().
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Let the window extend INTO the display cutout on every edge so the feed
        // is full-bleed; we inset only the HUD via CSS using the measured insets.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                    else
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        web = WebView(this)
        web.setBackgroundColor(Color.BLACK)
        setContentView(web)

        web.settings.apply {
            javaScriptEnabled = true                 // page logic + WHEP client
            domStorageEnabled = true                 // persists the robot IP (localStorage)
            mediaPlaybackRequiresUserGesture = false // let the live <video> autoplay
            // The page is loaded from file:// but the feed is http:// on the LAN,
            // which the WebView treats as mixed content — allow it explicitly.
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // Hardware acceleration is on by default (and declared in the manifest);
        // pin the WebView to a hardware layer so video compositing stays smooth.
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // JS -> native bridge. See RobotBridge: today it logs, tomorrow it
        // publishes MQTT — no page changes required.
        web.addJavascriptInterface(RobotBridge(), "ValiNative")

        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Re-apply the latest insets once the DOM (and :root) exists.
                if (insetCss.isNotEmpty()) view.evaluateJavascript(insetCss, null)
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            // WHEP is receive-only so this normally won't fire, but granting keeps
            // playback robust if the stream ever negotiates a media permission.
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread { request.grant(request.resources) }
            }
        }

        // Measure the safe area and push it into the page's CSS --sa-* vars.
        // We return the insets UNCONSUMED so the WebView itself stays full-bleed.
        ViewCompat.setOnApplyWindowInsetsListener(web) { v, insets ->
            applySafeAreaToPage(v as WebView, insets)
            insets
        }

        web.loadUrl(HUD_URL)
    }

    /**
     * Per-edge safe inset (in CSS px) = max(display cutout, system bars, rounded
     * corner) for that edge, injected into the page's --sa-* CSS variables.
     */
    private fun applySafeAreaToPage(view: WebView, insets: WindowInsetsCompat) {
        val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        var left = maxOf(cutout.left, bars.left)
        var top = maxOf(cutout.top, bars.top)
        var right = maxOf(cutout.right, bars.right)
        var bottom = maxOf(cutout.bottom, bars.bottom)

        // Rounded corners (API 31+) clip controls parked in the corners; reserve
        // ~half the corner radius on each adjoining edge so nothing is cut off.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.rootWindowInsets?.let { raw ->
                fun r(pos: Int) = raw.getRoundedCorner(pos)?.radius ?: 0
                val tl = r(RoundedCorner.POSITION_TOP_LEFT)
                val tr = r(RoundedCorner.POSITION_TOP_RIGHT)
                val bl = r(RoundedCorner.POSITION_BOTTOM_LEFT)
                val br = r(RoundedCorner.POSITION_BOTTOM_RIGHT)
                left = maxOf(left, (maxOf(tl, bl) * 0.5f).toInt())
                right = maxOf(right, (maxOf(tr, br) * 0.5f).toInt())
                top = maxOf(top, (maxOf(tl, tr) * 0.5f).toInt())
                bottom = maxOf(bottom, (maxOf(bl, br) * 0.5f).toInt())
            }
        }

        val d = resources.displayMetrics.density
        fun css(v: Int) = String.format(Locale.US, "%.1f", v / d)
        insetCss =
            "document.documentElement.style.setProperty('--sa-l','${css(left)}px');" +
            "document.documentElement.style.setProperty('--sa-r','${css(right)}px');" +
            "document.documentElement.style.setProperty('--sa-t','${css(top)}px');" +
            "document.documentElement.style.setProperty('--sa-b','${css(bottom)}px');"
        view.evaluateJavascript(insetCss, null)
    }

    /** Re-assert immersive fullscreen whenever we regain focus. */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enterImmersive()
            // Bars hidden -> insets changed; recompute the safe area.
            ViewCompat.requestApplyInsets(web)
        }
    }

    private fun enterImmersive() {
        WindowCompat.getInsetsController(window, web).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }

    // ── Robot command bridge ────────────────────────────────────────────────
    //  The page calls window.ValiNative.command(topic, payloadJson) for every
    //  control action — drive vector, collect on/off, snapshot, record on/off.
    //  Topics are namespaced "vali/<action>" and payloads are JSON strings.
    //
    //  RIGHT NOW: just logs to Logcat (tag "ValiPilot").
    //  NEXT (MQTT): create one MQTT client (e.g. Eclipse Paho / HiveMQ) in
    //  onCreate, then in command() do:
    //      mqtt.publish(topic, MqttMessage(payload.toByteArray()))
    //  Nothing in the page or the button wiring needs to change.
    inner class RobotBridge {
        @JavascriptInterface
        fun command(topic: String, payload: String) {
            Log.d(TAG, "command  $topic  $payload")
            // TODO(mqtt): mqtt.publish(topic, MqttMessage(payload.toByteArray()))
        }
    }
}
