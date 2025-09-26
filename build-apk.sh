#!/bin/bash

echo "🚀 Setting up Android AI - Building APK"
echo "========================================"

# Create SDK directory
export ANDROID_HOME="$PWD/android-sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

# Create directories
mkdir -p $ANDROID_HOME/cmdline-tools

# Download Android Command Line Tools
echo "📥 Downloading Android Command Line Tools..."
cd $ANDROID_HOME/cmdline-tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O cmdline-tools.zip

# Extract tools
echo "📦 Extracting Command Line Tools..."
unzip -q cmdline-tools.zip
mv cmdline-tools latest

# Go back to project directory
cd ../../

# Accept licenses and install required packages
echo "📄 Accepting Android SDK licenses..."
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

echo "⬇️  Installing Android SDK components..."
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Create local.properties file
echo "⚙️  Creating local.properties..."
echo "sdk.dir=$ANDROID_HOME" > local.properties

# Build the APK
echo "🔨 Building Android AI APK..."
./gradlew assembleDebug --no-daemon

# Check if APK was created
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ Success! APK created at: app/build/outputs/apk/debug/app-debug.apk"
    echo "📱 Ready to install on Android devices!"
    
    # Copy APK to root directory for easy access
    cp -f app/build/outputs/apk/debug/app-debug.apk ./android-ai.apk
    echo "📋 APK copied to: android-ai.apk"
    
    # Show APK info
    ls -lh android-ai.apk

    # Auto-push APK to repo (requested push-everytime)
    if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        echo "🚀 Pushing android-ai.apk to origin/main..."
        git add -f android-ai.apk
        git commit -m "chore: update android-ai.apk (auto-push after build)" || true
        git push origin main || true
    fi
else
    echo "❌ Failed to build APK"
    exit 1
fi 