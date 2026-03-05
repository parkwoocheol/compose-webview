#!/bin/bash
# Checks expect/actual implementation completeness across platforms

echo "📊 Platform Implementation Status"
echo "================================="
echo ""

COMMON_DIR="compose-webview/src/commonMain"
PLATFORMS=("androidMain" "iosMain" "desktopMain" "jsMain")

# Check if common directory exists
if [ ! -d "$COMMON_DIR" ]; then
    echo "❌ Error: $COMMON_DIR not found"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Count expect declarations
EXPECT_COUNT=$(find "$COMMON_DIR" -name "*.kt" -type f -exec grep -c "^expect " {} + 2>/dev/null | awk '{s+=$1} END {print s+0}')

echo "Total expect declarations: $EXPECT_COUNT"
echo ""

if [ "$EXPECT_COUNT" -eq 0 ]; then
    echo "✅ No expect declarations found (or all implementations are internal)"
    exit 0
fi

# Check each platform
echo "Platform implementation counts:"
echo "--------------------------------"

ALL_COMPLETE=true

for PLATFORM in "${PLATFORMS[@]}"; do
    PLATFORM_DIR="compose-webview/src/$PLATFORM"

    if [ -d "$PLATFORM_DIR" ]; then
        ACTUAL_COUNT=$(find "$PLATFORM_DIR" -name "*.kt" -type f -exec grep -c "^actual " {} + 2>/dev/null | awk '{s+=$1} END {print s+0}')

        # Determine status icon
        if [ "$ACTUAL_COUNT" -ge "$EXPECT_COUNT" ]; then
            STATUS="✅"
        else
            STATUS="⚠️ "
            ALL_COMPLETE=false
        fi

        echo "  $STATUS $PLATFORM: $ACTUAL_COUNT actuals"
    else
        echo "  ❌ $PLATFORM: directory not found"
        ALL_COMPLETE=false
    fi
done

echo ""
echo "================================="

if [ "$ALL_COMPLETE" = true ]; then
    echo "✅ All platforms have complete implementations"
    exit 0
else
    echo "⚠️  Some platforms may have incomplete implementations"
    echo ""
    echo "Note: This is a simple count check. Some expect/actual pairs"
    echo "may be correctly implemented even if counts differ (e.g., internal classes)."
    echo ""
    echo "Run platform-specific builds to verify:"
    echo "  bash .agents/skills/development/scripts/build_all.sh"
    exit 0
fi
