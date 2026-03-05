#!/bin/bash
# Verifies KDoc coverage for public APIs

echo "📝 Checking KDoc Coverage for Public APIs"
echo "=========================================="
echo ""

COMMON_DIR="compose-webview/src/commonMain"

# Check if common directory exists
if [ ! -d "$COMMON_DIR" ]; then
    echo "❌ Error: $COMMON_DIR not found"
    echo "Please run this script from the project root directory"
    exit 1
fi

echo "🔍 Scanning for public APIs..."

# Find Kotlin files
KOTLIN_FILES=$(find "$COMMON_DIR" -name "*.kt" -type f)

TOTAL_PUBLIC_APIS=0
MISSING_KDOC=0

# Temporary file to store APIs without KDoc
TEMP_FILE=$(mktemp)

# Check each Kotlin file
while IFS= read -r file; do
    # Find public declarations
    # This is a simplified check - matches public class/fun/val declarations

    # Get public declarations without KDoc
    # Look for lines starting with 'public' not preceded by /** comment
    while IFS= read -r line_num; do
        if [ -n "$line_num" ]; then
            LINE_CONTENT=$(sed -n "${line_num}p" "$file")

            # Check if previous line is KDoc (starts with /** or contains */)
            PREV_LINE_NUM=$((line_num - 1))
            if [ $PREV_LINE_NUM -gt 0 ]; then
                PREV_LINE=$(sed -n "${PREV_LINE_NUM}p" "$file")

                # Simple KDoc check
                if echo "$PREV_LINE" | grep -q '/\*\*\|^\s*\*'; then
                    # Has KDoc, skip
                    continue
                fi
            fi

            # Extract function/class name
            API_NAME=$(echo "$LINE_CONTENT" | sed -E 's/.*\s(class|fun|val|var|object|interface)\s+([A-Za-z0-9_]+).*/\2/')

            if [ -n "$API_NAME" ] && [ "$API_NAME" != "$LINE_CONTENT" ]; then
                TOTAL_PUBLIC_APIS=$((TOTAL_PUBLIC_APIS + 1))
                MISSING_KDOC=$((MISSING_KDOC + 1))

                # Store for reporting
                echo "$file:$line_num: $API_NAME" >> "$TEMP_FILE"
            fi
        fi
    done < <(grep -n "^public " "$file" | cut -d: -f1)

done <<< "$KOTLIN_FILES"

# Count public APIs with KDoc
PUBLIC_WITH_KDOC=$((TOTAL_PUBLIC_APIS - MISSING_KDOC))

# Calculate percentage if we have any public APIs
if [ $TOTAL_PUBLIC_APIS -gt 0 ]; then
    COVERAGE_PERCENT=$((PUBLIC_WITH_KDOC * 100 / TOTAL_PUBLIC_APIS))
else
    COVERAGE_PERCENT=100
fi

echo ""
echo "=========================================="
echo "KDoc Coverage Report:"
echo "  Total public APIs: $TOTAL_PUBLIC_APIS"
echo "  With KDoc: $PUBLIC_WITH_KDOC"
echo "  Missing KDoc: $MISSING_KDOC"
echo "  Coverage: ${COVERAGE_PERCENT}%"
echo ""

if [ $MISSING_KDOC -eq 0 ]; then
    echo "✅ All public APIs have KDoc comments"
    rm -f "$TEMP_FILE"
    exit 0
else
    echo "⚠️  Some public APIs lack KDoc:"
    echo ""

    # Show first 10 missing KDocs
    head -n 10 "$TEMP_FILE" | while IFS= read -r line; do
        echo "  - $line"
    done

    if [ $MISSING_KDOC -gt 10 ]; then
        echo "  ... and $((MISSING_KDOC - 10)) more"
    fi

    echo ""
    echo "Recommendation:"
    echo "  Add KDoc comments to public APIs following this format:"
    echo ""
    echo "  /**"
    echo "   * Brief description of what this does."
    echo "   *"
    echo "   * @param param Description"
    echo "   * @return Description"
    echo "   */"
    echo "  public fun example(param: String): Result"
    echo ""

    rm -f "$TEMP_FILE"

    # Return warning (not error)
    # Exit 1 to indicate warning, but this doesn't fail the build
    exit 1
fi
