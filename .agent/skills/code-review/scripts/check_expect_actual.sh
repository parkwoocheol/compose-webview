#!/bin/bash
# Verifies expect/actual implementation completeness

echo "📊 Checking Expect/Actual Implementation Completeness"
echo "===================================================="
echo ""

COMMON_DIR="compose-webview/src/commonMain"
PLATFORMS=("androidMain" "iosMain" "desktopMain" "jsMain")

# Check if common directory exists
if [ ! -d "$COMMON_DIR" ]; then
    echo "❌ Error: $COMMON_DIR not found"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Find all expect declarations
echo "🔍 Scanning for expect declarations..."
EXPECT_FILES=$(find "$COMMON_DIR" -name "*.kt" -type f -exec grep -l "^expect " {} \;)

if [ -z "$EXPECT_FILES" ]; then
    echo "✅ No expect declarations found (all implementations are platform-agnostic)"
    exit 0
fi

MISSING_COUNT=0
TOTAL_EXPECTS=0

# Check each file containing expect declarations
while IFS= read -r file; do
    echo ""
    echo "File: $file"
    echo "----------------------------------------"

    # Extract expect declarations (simplified - matches class and function names)
    EXPECTS=$(grep "^expect " "$file" | sed -E 's/.*\s(class|fun|val|var)\s+([A-Za-z0-9_]+).*/\2/')

    if [ -z "$EXPECTS" ]; then
        continue
    fi

    # Check each expect declaration
    while IFS= read -r expect_name; do
        if [ -z "$expect_name" ]; then
            continue
        fi

        TOTAL_EXPECTS=$((TOTAL_EXPECTS + 1))
        echo "  Checking: $expect_name"

        # Check each platform
        MISSING_PLATFORMS=()

        for PLATFORM in "${PLATFORMS[@]}"; do
            PLATFORM_DIR="compose-webview/src/$PLATFORM"

            if [ ! -d "$PLATFORM_DIR" ]; then
                MISSING_PLATFORMS+=("$PLATFORM (dir not found)")
                continue
            fi

            # Search for actual implementation
            if ! grep -r "actual.*$expect_name" "$PLATFORM_DIR" > /dev/null 2>&1; then
                MISSING_PLATFORMS+=("$PLATFORM")
            fi
        done

        # Report results
        if [ ${#MISSING_PLATFORMS[@]} -eq 0 ]; then
            echo "    ✅ All platforms implemented"
        else
            echo "    ❌ Missing in: ${MISSING_PLATFORMS[*]}"
            MISSING_COUNT=$((MISSING_COUNT + 1))
        fi
    done <<< "$EXPECTS"

done <<< "$EXPECT_FILES"

echo ""
echo "===================================================="
echo "Summary:"
echo "  Total expect declarations checked: $TOTAL_EXPECTS"
echo "  Missing implementations: $MISSING_COUNT"
echo ""

if [ $MISSING_COUNT -eq 0 ]; then
    echo "✅ All expect declarations have complete implementations"
    echo ""
    echo "Note: This is a simplified check based on naming."
    echo "Compile all platforms to verify actual implementation:"
    echo "  bash .agent/skills/development/scripts/build_all.sh"
    exit 0
else
    echo "❌ Found $MISSING_COUNT incomplete implementation(s)"
    echo ""
    echo "Action required:"
    echo "  1. Review the missing implementations listed above"
    echo "  2. Add 'actual' implementations in the missing platform source sets"
    echo "  3. Verify with: ./gradlew build"
    echo ""
    exit 1
fi
