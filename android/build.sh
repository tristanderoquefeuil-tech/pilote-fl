#!/usr/bin/env bash
# Compile l'APK Pilote F&L sans Gradle, avec les outils du SDK Android déjà installés.
set -euo pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_HOME:-$LOCALAPPDATA/Android/Sdk}"
SDK="$(cygpath -u "$SDK" 2>/dev/null || echo "$SDK")"
BT="$SDK/build-tools/36.1.0"
JAR="$SDK/platforms/android-36/android.jar"
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
export PATH="$JAVA_HOME/bin:$PATH"
KS="$HOME/.android/debug.keystore"

node ../src/build-web.js
rm -rf build && mkdir -p build/classes build/gen

echo "1/6 Ressources"
"$BT/aapt2.exe" compile --dir res -o build/res.zip
"$BT/aapt2.exe" link -I "$JAR" --manifest AndroidManifest.xml -A assets --java build/gen -o build/base.apk build/res.zip

echo "2/6 Java"
javac --release 11 -nowarn -cp "$JAR" -d build/classes src/com/pilotefl/app/*.java build/gen/com/pilotefl/app/R.java

echo "3/6 Dex"
"$BT/d8.bat" --lib "$JAR" --min-api 24 --output build $(find build/classes -name '*.class')

echo "4/6 Assemblage"
(cd build && "$BT/aapt.exe" add base.apk classes.dex > /dev/null)

echo "5/6 Alignement"
"$BT/zipalign.exe" -f -p 4 build/base.apk build/aligned.apk

echo "6/6 Signature"
"$BT/apksigner.bat" sign --ks "$KS" --ks-pass pass:android --key-pass pass:android --out PiloteFL.apk build/aligned.apk
"$BT/apksigner.bat" verify PiloteFL.apk && echo "OK : $(pwd)/PiloteFL.apk"
