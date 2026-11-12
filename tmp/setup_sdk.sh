#!/bin/bash
set -e
echo "Setting up Android SDK..."
mkdir -p /opt/android-sdk/cmdline-tools
cd /tmp
if [ ! -f commandlinetools-linux.zip ]; then
    echo "Downloading commandline-tools..."
    curl -o commandlinetools-linux.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
fi
echo "Extracting cmdline-tools..."
unzip -q -o commandlinetools-linux.zip -d /opt/android-sdk/cmdline-tools/
rm -rf /opt/android-sdk/cmdline-tools/latest
mv /opt/android-sdk/cmdline-tools/cmdline-tools /opt/android-sdk/cmdline-tools/latest

export ANDROID_HOME=/opt/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

echo "Accepting licenses..."
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null 2>&1 || true

echo "Installing platforms;android-35 and build-tools;35.0.0..."
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" > /dev/null

echo "sdk.dir=/opt/android-sdk" > /app/applet/local.properties
echo "Android SDK setup complete!"
