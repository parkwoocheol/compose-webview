#!/bin/bash
# Runs all tests for ComposeWebView and reports results
set -e

echo "🧪 Running ComposeWebView Test Suite..."
echo "=============================================="
echo ""

# Track total time
START_TIME=$(date +%s)

# Run all tests using the comprehensive test task
echo "Running all platform tests..."
echo ""

if ./gradlew :compose-webview:allTests; then
    echo ""
    echo "=============================================="
    echo "✅ All tests passed!"

    # Calculate duration
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))

    echo "⏱️  Total time: ${DURATION}s"
    echo "=============================================="
    exit 0
else
    echo ""
    echo "=============================================="
    echo "❌ Some tests failed"
    echo "=============================================="
    echo ""
    echo "To run platform-specific tests:"
    echo "  Android:  ./gradlew :compose-webview:testDebugUnitTest"
    echo "  iOS:      ./gradlew :compose-webview:iosSimulatorArm64Test"
    echo "  Desktop:  ./gradlew :compose-webview:desktopTest"
    echo "  Web:      ./gradlew :compose-webview:jsTest"
    echo ""
    exit 1
fi
