#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# Manual APK build for Vali Pilot — no Gradle / no AGP / no androidx required.
#
# Why this exists: the build sandbox cannot reach Google's Maven/SDK servers
# (AGP + androidx + SDK platforms are Google-only), so the canonical Gradle
# build can't run here. This script builds a functionally-identical, framework-
# only debug APK using just: aapt2, an API-23 android.jar, javac, dx, zipalign
# and apksigner.
#
# Tool sources (all reachable here):
#   aapt2, zipalign, apksigner, android.jar(23)  -> Ubuntu apt
#   dx (dexer)                                    -> Maven Central (dalvik-dx)
#   javac/keytool                                 -> JDK
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE"

ANDROID_JAR="/usr/lib/android-sdk/platforms/android-23/android.jar"
DX_JAR="${DX_JAR:-/tmp/dx.jar}"           # com.jakewharton.android.repackaged:dalvik-dx
OUT="$HERE/build"
APK_NAME="vali-pilot-debug.apk"
MIN_SDK=23
TARGET_SDK=23

rm -rf "$OUT"; mkdir -p "$OUT/gen" "$OUT/classes"

echo "==> [1/7] aapt2 compile resources"
aapt2 compile --dir res -o "$OUT/res.zip"

echo "==> [2/7] aapt2 link (resources + manifest + assets -> base.apk, R.java)"
aapt2 link \
  -o "$OUT/base.apk" \
  -I "$ANDROID_JAR" \
  --manifest AndroidManifest.xml \
  -A assets \
  --java "$OUT/gen" \
  --min-sdk-version "$MIN_SDK" \
  --target-sdk-version "$TARGET_SDK" \
  "$OUT/res.zip"

echo "==> [3/7] javac (compile Java against android.jar, bytecode 8)"
javac --release 8 \
  -classpath "$ANDROID_JAR" \
  -d "$OUT/classes" \
  "$OUT/gen/com/vali/pilot/R.java" \
  src/com/vali/pilot/MainActivity.java

echo "==> [4/7] dx (dex the .class files -> classes.dex)"
java -cp "$DX_JAR" com.android.dx.command.Main \
  --dex --min-sdk-version="$MIN_SDK" \
  --output="$OUT/classes.dex" "$OUT/classes"

echo "==> [5/7] add classes.dex into the apk"
cp "$OUT/base.apk" "$OUT/unaligned.apk"
( cd "$OUT" && zip -q unaligned.apk classes.dex )

echo "==> [6/7] zipalign"
zipalign -f -p 4 "$OUT/unaligned.apk" "$OUT/aligned.apk"

echo "==> [7/7] sign with a debug keystore"
KS="$HERE/debug.keystore"
if [ ! -f "$KS" ]; then
  keytool -genkeypair -v \
    -keystore "$KS" -storepass android -keypass android \
    -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null 2>&1
fi
apksigner sign \
  --ks "$KS" --ks-pass pass:android --key-pass pass:android \
  --min-sdk-version "$MIN_SDK" \
  --v1-signing-enabled true --v2-signing-enabled true \
  --out "$OUT/$APK_NAME" "$OUT/aligned.apk"

echo "==> verify signature"
apksigner verify --verbose "$OUT/$APK_NAME" | sed 's/^/    /'

echo
echo "BUILD OK -> $OUT/$APK_NAME"
