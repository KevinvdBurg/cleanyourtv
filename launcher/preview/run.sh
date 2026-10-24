#!/usr/bin/env bash
# Draws every app's ambient shapes on the Mac, without a TV, and checks them in place.
# Compiles AppShapes and AmbientScene from the app as they are, so the preview cannot drift from the APK.
# Output: launcher/build/shape-preview/sheet-day.png, sheet-night.png and one PNG per app.
set -euo pipefail
cd "$(dirname "$0")/.."
JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
classes=build/preview-classes
rm -rf "$classes"
"$JAVA_HOME/bin/javac" -d "$classes" \
    app/src/main/java/com/gathin/tvhome/AppShapes.java \
    app/src/main/java/com/gathin/tvhome/AmbientScene.java \
    preview/ScenePreview.java
"$JAVA_HOME/bin/java" -Djava.awt.headless=true -cp "$classes" com.gathin.tvhome.ScenePreview "${1:-build/shape-preview}"
