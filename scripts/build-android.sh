#!/bin/bash
set -e

# Load environment variables from .env file
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

# Resolve JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    if command -v java &> /dev/null; then
        JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
    else
        echo "Error: JAVA_HOME not set and java not found in PATH"
        exit 1
    fi
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# Resolve Gradle binary
if [ -n "$GRADLE_BIN" ] && [ -f "$GRADLE_BIN" ]; then
    GRADLE="$GRADLE_BIN"
elif [ -f "apps/android/gradlew" ]; then
    GRADLE="apps/android/gradlew"
else
    # Try to find Gradle in .tmp/gradle-dist
    GRADLE=$(find .tmp/gradle-dist -name "gradle" -type f 2>/dev/null | head -n 1)
    if [ -z "$GRADLE" ]; then
        echo "Error: Gradle not found. Please set GRADLE_BIN or install Gradle"
        exit 1
    fi
fi

echo "Using Gradle: $GRADLE"

# Set Android SDK paths
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
export ANDROID_HOME="$ANDROID_SDK_ROOT"

# Run Gradle build
cd apps/android
"$GRADLE" :app:assembleDebug

# Copy APK to builds directory
mkdir -p ../../builds/android/latest
cp app/build/outputs/apk/debug/*.apk ../../builds/android/latest/

echo "Build completed successfully"
