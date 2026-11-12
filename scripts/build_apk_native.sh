#!/bin/bash
set -e

TOOLS_DIR="/app/applet/.tools"
mkdir -p "$TOOLS_DIR"

# 1. Download & Extract JDK 17 if not present
JDK_DIR="$TOOLS_DIR/jdk-17"
if [ ! -f "$JDK_DIR/bin/java" ]; then
    echo "=== Step 1/5: Downloading Adoptium Temurin OpenJDK 17 ==="
    mkdir -p "$JDK_DIR"
    curl -L -o /tmp/jdk17.tar.gz "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk"
    tar -xzf /tmp/jdk17.tar.gz -C "$JDK_DIR" --strip-components=1
    rm -f /tmp/jdk17.tar.gz
    echo "JDK 17 installed successfully!"
fi

export JAVA_HOME="$JDK_DIR"
export PATH="$JAVA_HOME/bin:$PATH"
java -version

# 2. Download & Extract Android Commandline Tools if not present
SDK_DIR="$TOOLS_DIR/android-sdk"
CMDLINE_DIR="$SDK_DIR/cmdline-tools/latest"
if [ ! -f "$CMDLINE_DIR/bin/sdkmanager" ]; then
    echo "=== Step 2/5: Downloading Android Command-Line Tools ==="
    mkdir -p "$SDK_DIR/cmdline-tools"
    curl -L -o /tmp/cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    unzip -q -o /tmp/cmdline-tools.zip -d "$SDK_DIR/cmdline-tools"
    rm -rf "$CMDLINE_DIR"
    mv "$SDK_DIR/cmdline-tools/cmdline-tools" "$CMDLINE_DIR"
    rm -f /tmp/cmdline-tools.zip
    echo "Android Command-Line Tools installed!"
fi

export ANDROID_HOME="$SDK_DIR"
export PATH="$CMDLINE_DIR/bin:$ANDROID_HOME/platform-tools:$PATH"

# 3. Accept licenses and install platform 35 & build-tools 35.0.0
echo "=== Step 3/5: Setting up Android SDK 35 & Build Tools ==="
mkdir -p "$SDK_DIR/licenses"
echo -e "\n24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$SDK_DIR/licenses/android-sdk-license"
echo -e "\n84831b9409646a918e30573bab4c9c91346d8abd" > "$SDK_DIR/licenses/android-sdk-preview-license"
yes | sdkmanager --licenses > /dev/null 2>&1 || true

if [ ! -d "$SDK_DIR/platforms/android-35" ] || [ ! -d "$SDK_DIR/build-tools/35.0.0" ]; then
    echo "Installing platforms;android-35 and build-tools;35.0.0..."
    sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" > /dev/null
fi

echo "sdk.dir=$SDK_DIR" > /app/applet/local.properties

# 4. Run Gradle Build
echo "=== Step 4/5: Running ./gradlew assembleDebug ==="
chmod +x /app/applet/gradlew
cd /app/applet
./gradlew assembleDebug --stacktrace

# 5. Check and copy output APK
echo "=== Step 5/5: Verifying Output APK ==="
APK_SOURCE="/app/applet/app/build/outputs/apk/debug/app-debug.apk"
APK_TARGET="/app/applet/app/build/outputs/apk/debug/PUZZLE100-debug.apk"

if [ -f "$APK_SOURCE" ]; then
    cp "$APK_SOURCE" "$APK_TARGET"
    mkdir -p /app/applet/public
    cp "$APK_SOURCE" /app/applet/public/PUZZLE100-debug.apk
    echo "SUCCESS: APK created at $APK_TARGET"
    ls -lh "$APK_TARGET"
else
    echo "ERROR: $APK_SOURCE not found after build!"
    exit 1
fi
