#!/bin/bash
# Builds ComposeWebView for all platforms sequentially with error handling
set -e

echo "🏗️  Building ComposeWebView for all platforms..."
echo "=============================================="
echo ""

# Function to print section headers
print_section() {
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "$1"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

# Track total time
START_TIME=$(date +%s)

# Android
print_section "📱 Building Android..."
if ./gradlew :compose-webview:assembleDebug; then
    echo "✅ Android build successful"
else
    echo "❌ Android build failed"
    exit 1
fi

# iOS
print_section "🍎 Building iOS..."
if ./gradlew :compose-webview:linkIosSimulatorArm64; then
    echo "✅ iOS build successful"
else
    echo "❌ iOS build failed"
    exit 1
fi

# Desktop (JVM)
print_section "🖥️  Building Desktop (JVM)..."
if ./gradlew :compose-webview:compileKotlinDesktop; then
    echo "✅ Desktop build successful"
else
    echo "❌ Desktop build failed"
    exit 1
fi

# Web (JS)
print_section "🌐 Building Web (JS)..."
if ./gradlew :compose-webview:compileKotlinJs; then
    echo "✅ Web build successful"
else
    echo "❌ Web build failed"
    exit 1
fi

# Summary
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo "=============================================="
echo "✅ All platforms built successfully!"
echo "⏱️  Total time: ${DURATION}s"
echo "=============================================="
