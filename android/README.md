# Vali Pilot — Android app

A minimal **Kotlin** app: one fullscreen, **landscape**, immersive `WebView` that
hosts the HUD page (`app/src/main/assets/index.html`). The page plays the **live
MediaMTX WebRTC feed over WHEP** and drives every control; the Kotlin side only
configures the WebView and exposes the JS → native bridge where MQTT will land.

> **Two ways to get an APK:**
>
> 1. **Prebuilt, ready to install now:** [`../dist/vali-pilot-debug.apk`](../dist/vali-pilot-debug.apk)
>    — a debug-signed APK already built in this repo. It was produced by a
>    framework-only **manual** build (`manual-build/`) because the build sandbox
>    can't reach Google's Maven/SDK servers (AGP + androidx are Google-only). It's
>    functionally identical to the Kotlin app: same WebView config, same
>    `assets/index.html`, same WHEP feed and command bridge. **minSdk 23**
>    (installs on Android 6.0+). Jump to *Install on your phone*.
> 2. **The canonical Gradle/Kotlin build** (recommended on your own machine, where
>    Google's servers are reachable): the `app/` module below. **minSdk 26**,
>    `compileSdk 35`. Build steps under *Build the debug APK*.
>
> Both load the exact same HUD page, so the live feed and controls behave the
> same. The live feed was **not** play-tested in the build sandbox (no device /
> no MediaMTX server there) — verify on-device per *Verify the feed actually
> plays*.

---

## What's configured (per the brief)

| Requirement | Where |
|---|---|
| Fullscreen WebView of the HUD page | `MainActivity.kt` → loads `file:///android_asset/index.html` |
| Live feed via WHEP `http://<ip>:8889/cam/whep` | JS WHEP client in `assets/index.html` (`WhepClient` + `connectFeed`) |
| `javaScriptEnabled` + DOM storage | `MainActivity.kt` WebView settings |
| `mediaPlaybackRequiresUserGesture = false` (autoplay) | `MainActivity.kt` |
| Hardware acceleration | manifest `android:hardwareAccelerated="true"` + `LAYER_TYPE_HARDWARE` |
| Cleartext / HTTP to LAN + mixed content | `usesCleartextTraffic`, `network_security_config.xml`, `MIXED_CONTENT_ALWAYS_ALLOW` |
| Locked to landscape | manifest `android:screenOrientation="landscape"` |
| Immersive (status + nav bars hidden) | `enterImmersive()` in `MainActivity.kt` |
| Robot address changeable without rebuild | in-app **gear ▸ Robot Address** (persists in DOM storage); accepts a **hostname or IP** (not validated as numeric); first-run default = `DEFAULT_ROBOT_HOST` in `MainActivity.kt` and `CONFIG.DEFAULT_HOST` (`aarush-virtualbox.local`) in `index.html` |
| Control buttons = clean logging stubs, MQTT-ready | `Robot.*` layer in `index.html` → `window.ValiNative.command()` → `RobotBridge` in `MainActivity.kt` (`TODO(mqtt)`) |

### minSdk — 24 (Gradle build)
The canonical Gradle project targets **minSdk 24** (Android 7.0):
- Modern Android (15+) refuses to install anything targeting below API 24, so 24
  is the practical floor for a sideloadable modern APK.
- System WebView / WebRTC behaves consistently from here up.
- Adaptive launcher icons apply on 26+; a plain vector icon in `res/mipmap/`
  covers 24–25.

`targetSdk`/`compileSdk` = 35. AGP 8.7.2, Kotlin 2.0.21, Gradle 8.9 (wrapper pinned).

**Build it in the cloud:** the `.github/workflows/build.yml` workflow builds this
exact Gradle project on GitHub Actions (whose runners have the full Android SDK +
Google access) and uploads `app-debug.apk` as a downloadable artifact. See
*Build on GitHub Actions* below.

> The old **`dist/` APK** (minSdk 23, built from the framework-only
> `manual-build/` twin) is kept only as a historical fallback — it will **not**
> install on Android 15+. Use the GitHub Actions APK instead.

---

## Set the robot address
Two options (no rebuild needed for the first one):
1. **In-app:** launch the app → tap the **⋯ gear** (top-right) → type the VM IP →
   **SET**. Persisted across launches.
2. **Bake the default:** edit `DEFAULT_ROBOT_IP` in
   `app/src/main/java/com/vali/pilot/MainActivity.kt` *and* `CONFIG.DEFAULT_HOST`
   in `app/src/main/assets/index.html`.

The feed URL is assembled as `http://<ip>:8889/cam/whep`. Port (`8889`) and path
(`cam`) are `CONFIG` constants at the top of `index.html` — change them there if
your MediaMTX setup differs. You can also type `ip:port` in the in-app field.

---

## Build on GitHub Actions (no local toolchain needed)

`.github/workflows/build.yml` builds the debug APK on every push and uploads it.
To get the APK:
1. Push this repo to GitHub (see the root instructions).
2. On GitHub open the **Actions** tab → click the latest **Build debug APK** run.
3. When it's green, scroll to **Artifacts** at the bottom → download
   **`vali-pilot-debug-apk`** (a zip containing `app-debug.apk`).
4. Unzip → install `app-debug.apk` on your phone (see *Install on your phone*).

## Build the debug APK locally

**Android Studio:** open the `android/` folder → let it sync → **Build ▸ Build
Bundle(s) / APK(s) ▸ Build APK(s)**.

**Command line** (from `android/`):
```bash
./gradlew assembleDebug
```
Output APK:
```
app/build/outputs/apk/debug/app-debug.apk
```
This is automatically **debug-signed** (Android's debug keystore), so it's
sideloadable as-is.

### Or: the manual (no-Gradle) build
`manual-build/build.sh` builds the framework-only twin into
`manual-build/build/vali-pilot-debug.apk` using only `aapt2`, an API-23
`android.jar`, `javac`, `dx`, `zipalign`, `apksigner`. This is how
`../dist/vali-pilot-debug.apk` was produced. You normally don't need this — it
exists so an APK could be built where Google's servers are blocked.

---

## Install on your phone

> The prebuilt file is **`dist/vali-pilot-debug.apk`** (repo root `dist/`). If you
> build with Gradle instead, swap in `app/build/outputs/apk/debug/app-debug.apk`.

### Method A — adb over USB
1. On the phone: **Settings ▸ About phone ▸** tap **Build number** 7× to unlock
   **Developer options**.
2. **Settings ▸ System ▸ Developer options ▸** enable **USB debugging**.
3. Plug the phone into the computer via USB; on the phone tap **Allow** on the
   "Allow USB debugging?" prompt.
4. Install platform-tools (gives you `adb`) if you don't have it, then:
   ```bash
   adb devices                       # should list your phone
   adb install -r dist/vali-pilot-debug.apk
   ```
   (`-r` reinstalls over an existing copy.)

### Method B — just copy the file across
1. Copy `dist/vali-pilot-debug.apk` to the phone (USB transfer, Google Drive,
   email it to yourself, etc.).
2. On the phone, allow **install from unknown sources**: **Settings ▸ Apps ▸
   Special app access ▸ Install unknown apps ▸** pick the app you'll open the APK
   from (Files / Chrome / Drive) **▸ Allow from this source**.
   (Older Android: **Settings ▸ Security ▸ Unknown sources**.)
3. Open the APK in a file manager and tap **Install** (you may see "Play Protect"
   warn about an unknown app — choose **Install anyway**).

---

## What you need on the phone for it to run
- **Android 6.0+** for the prebuilt APK (minSdk 23) — any modern phone is fine.
- A reasonably current **Android System WebView / Chrome** (it provides the
  WebRTC engine that plays the feed). It's preinstalled and auto-updated on
  essentially all phones; if playback misbehaves, update "Android System WebView"
  and "Chrome" from the Play Store.
- The phone on the **same Wi-Fi/LAN** as the robot/VM.

### ⚠️ About the `.local` hostname
`aarush-virtualbox.local` relies on **mDNS** resolution. Desktop browsers resolve
`.local` reliably; **Android's support is inconsistent** across versions/OEMs, so
the hostname may or may not resolve on a given phone. If the feed won't connect
with the hostname, open the in-app **gear ▸ Robot Address**, type the VM's **raw
IP** (e.g. `192.168.1.42`) and tap **SET** — the field accepts a hostname *or* an
IP, and the value persists. (Tip: a static DHCP lease for the VM keeps that IP
stable.)

---

## Verify the feed actually plays
1. On the VM, confirm MediaMTX is publishing and WHEP works in a desktop browser:
   open `http://<VM-host-or-IP>:8889/cam` — you should see the stream.
2. Put the phone on the **same LAN** as the VM.
3. Launch the app. If the hostname doesn't connect, set the **raw IP** in the gear
   menu and hit **SET**. On success the connection chip reads **Linked**, the CSS
   fallback scene is replaced by live video, and the latency/FPS readouts switch
   to **real** WebRTC stats (from `getStats()`).
4. If it stays on the fallback scene: check `adb logcat -s ValiPilot chromium`
   for the WHEP warning the page logs, and re-confirm step 1 + the address.
