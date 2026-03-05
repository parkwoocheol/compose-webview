#!/bin/bash
# Verifies expect/actual implementation completeness
# Note: This is a fast static check (name-based), not a compiler-level proof.

set -u

echo "📊 Checking Expect/Actual Implementation Completeness"
echo "===================================================="
echo ""

COMMON_DIR="compose-webview/src/commonMain"
SRC_DIR="compose-webview/src"
declare -a PLATFORMS=()

parse_expect_line() {
    local line="$1"
    local stripped="$line"
    local name

    # Drop inline comments to reduce parsing noise.
    stripped="${stripped%%//*}"

    # Expect type declarations.
    name=$(echo "$stripped" | sed -En 's/^[[:space:]]*expect[[:space:]]+annotation[[:space:]]+class[[:space:]]+([A-Za-z_][A-Za-z0-9_]*).*/\1/p')
    if [ -n "$name" ]; then
        echo "type:$name"
        return 0
    fi

    name=$(echo "$stripped" | sed -En 's/^[[:space:]]*expect[[:space:]]+((open|abstract|sealed|data|value|inline|external|inner|enum)[[:space:]]+)*class[[:space:]]+([A-Za-z_][A-Za-z0-9_]*).*/\3/p')
    if [ -n "$name" ]; then
        echo "type:$name"
        return 0
    fi

    name=$(echo "$stripped" | sed -En 's/^[[:space:]]*expect[[:space:]]+((open|data|companion)[[:space:]]+)*object[[:space:]]+([A-Za-z_][A-Za-z0-9_]*).*/\3/p')
    if [ -n "$name" ]; then
        echo "type:$name"
        return 0
    fi

    name=$(echo "$stripped" | sed -En 's/^[[:space:]]*expect[[:space:]]+((sealed|fun)[[:space:]]+)*interface[[:space:]]+([A-Za-z_][A-Za-z0-9_]*).*/\3/p')
    if [ -n "$name" ]; then
        echo "type:$name"
        return 0
    fi

    name=$(echo "$stripped" | sed -En 's/^[[:space:]]*expect[[:space:]]+typealias[[:space:]]+([A-Za-z_][A-Za-z0-9_]*).*/\1/p')
    if [ -n "$name" ]; then
        echo "type:$name"
        return 0
    fi

    # Expect functions (regular + extension + generic).
    name=$(echo "$stripped" | sed -En 's/^[[:space:]]*expect[[:space:]]+.*fun[[:space:]]+(<[^>]+>[[:space:]]+)?([A-Za-z_][A-Za-z0-9_]*\.)?([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*\(.*/\3/p')
    if [ -n "$name" ]; then
        echo "fun:$name"
        return 0
    fi

    # Expect properties (regular + extension).
    name=$(echo "$stripped" | sed -En 's/^[[:space:]]*expect[[:space:]]+.*(val|var)[[:space:]]+([A-Za-z_][A-Za-z0-9_]*\.)?([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*:.*/\3/p')
    if [ -n "$name" ]; then
        echo "prop:$name"
        return 0
    fi

    return 1
}

has_actual_declaration() {
    local kind="$1"
    local name="$2"
    local platform_dir="$3"
    local pattern

    case "$kind" in
        type)
            pattern="^[[:space:]]*actual[[:space:]]+.*\\b(class|object|interface|typealias)\\b[[:space:]]+${name}\\b"
            ;;
        fun)
            pattern="^[[:space:]]*actual[[:space:]]+.*\\bfun\\b[^\\n]*\\b${name}[[:space:]]*\\("
            ;;
        prop)
            pattern="^[[:space:]]*actual[[:space:]]+.*\\b(val|var)\\b[^\\n]*\\b${name}[[:space:]]*:"
            ;;
        *)
            return 1
            ;;
    esac

    if command -v rg > /dev/null 2>&1; then
        rg -n --glob "*.kt" -e "$pattern" "$platform_dir" > /dev/null 2>&1
    else
        grep -rE "$pattern" "$platform_dir" --include="*.kt" > /dev/null 2>&1
    fi
}

# Check if common directory exists
if [ ! -d "$COMMON_DIR" ]; then
    echo "❌ Error: $COMMON_DIR not found"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Discover platform source sets dynamically.
for dir in "$SRC_DIR"/*Main; do
    [ -d "$dir" ] || continue
    source_set=$(basename "$dir")
    if [ "$source_set" != "commonMain" ]; then
        PLATFORMS+=("$source_set")
    fi
done

if [ ${#PLATFORMS[@]} -eq 0 ]; then
    echo "❌ No platform source sets found under $SRC_DIR/*Main"
    exit 1
fi

echo "Detected platform source sets: ${PLATFORMS[*]}"
echo ""

# Find all files with expect declarations.
echo "🔍 Scanning for expect declarations..."
EXPECT_FILES=$(find "$COMMON_DIR" -name "*.kt" -type f -exec grep -l "^[[:space:]]*expect[[:space:]]" {} \;)

if [ -z "$EXPECT_FILES" ]; then
    echo "✅ No expect declarations found (all implementations are platform-agnostic)"
    exit 0
fi

MISSING_COUNT=0
TOTAL_EXPECTS=0
SKIPPED_EXPECTS=0

# Check each file containing expect declarations
while IFS= read -r file; do
    echo ""
    echo "File: $file"
    echo "----------------------------------------"

    LINE_NO=0
    while IFS= read -r line; do
        LINE_NO=$((LINE_NO + 1))
        if [[ ! "$line" =~ ^[[:space:]]*expect[[:space:]] ]]; then
            continue
        fi

        parsed="$(parse_expect_line "$line" || true)"
        if [ -z "$parsed" ]; then
            SKIPPED_EXPECTS=$((SKIPPED_EXPECTS + 1))
            echo "  ⚠️  Skipped unsupported expect syntax at line $LINE_NO"
            continue
        fi

        kind="${parsed%%:*}"
        expect_name="${parsed#*:}"

        TOTAL_EXPECTS=$((TOTAL_EXPECTS + 1))
        echo "  Checking ($kind): $expect_name"

        MISSING_PLATFORMS=()

        for PLATFORM in "${PLATFORMS[@]}"; do
            PLATFORM_DIR="$SRC_DIR/$PLATFORM"
            if ! has_actual_declaration "$kind" "$expect_name" "$PLATFORM_DIR"; then
                MISSING_PLATFORMS+=("$PLATFORM")
            fi
        done

        if [ ${#MISSING_PLATFORMS[@]} -eq 0 ]; then
            echo "    ✅ All platforms implemented"
        else
            echo "    ❌ Missing in: ${MISSING_PLATFORMS[*]}"
            MISSING_COUNT=$((MISSING_COUNT + 1))
        fi
    done < "$file"

done <<< "$EXPECT_FILES"

echo ""
echo "===================================================="
echo "Summary:"
echo "  Total expect declarations checked: $TOTAL_EXPECTS"
echo "  Missing implementations: $MISSING_COUNT"
echo "  Skipped declarations: $SKIPPED_EXPECTS"
echo ""

if [ $MISSING_COUNT -eq 0 ] && [ $SKIPPED_EXPECTS -eq 0 ]; then
    echo "✅ All expect declarations have complete implementations"
    echo ""
    echo "Note: This is a static check based on declaration names."
    echo "Compile all platforms to verify actual implementation:"
    echo "  bash .agents/skills/development/scripts/build_all.sh"
    exit 0
else
    if [ $MISSING_COUNT -gt 0 ]; then
        echo "❌ Found $MISSING_COUNT incomplete implementation(s)"
    fi
    if [ $SKIPPED_EXPECTS -gt 0 ]; then
        echo "⚠️  Skipped $SKIPPED_EXPECTS declaration(s) with unsupported syntax"
    fi
    echo ""
    echo "Action required:"
    echo "  1. Review the missing/skipped declarations listed above"
    echo "  2. Add/fix 'actual' declarations in the missing platform source sets"
    echo "  3. Verify with: ./gradlew build"
    echo ""
    exit 1
fi
