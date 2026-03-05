#!/bin/bash
# Validates documentation files and structure

echo "🔍 Validating Documentation..."
echo "=============================================="
echo ""

ERRORS=0

# Check if docs directory exists
if [ ! -d "docs" ]; then
    echo "❌ docs/ directory not found"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Check required files exist
echo "📋 Checking required files..."

REQUIRED_FILES=(
    "docs/index.md"
    "docs/getting-started.md"
    "mkdocs.yml"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✅ $file"
    else
        echo "  ❌ Missing: $file"
        ERRORS=$((ERRORS + 1))
    fi
done

echo ""

# Check for broken markdown syntax (basic check)
echo "🔗 Checking markdown files..."

MARKDOWN_FILES=$(find docs -name "*.md" -type f)
MD_COUNT=$(echo "$MARKDOWN_FILES" | wc -l | tr -d ' ')

echo "  Found $MD_COUNT markdown files"

# Check for common issues
while IFS= read -r file; do
    # Check for empty files
    if [ ! -s "$file" ]; then
        echo "  ⚠️  Empty file: $file"
    fi

    # Check for potential broken links (very basic check)
    if grep -q '\[.*\](.*\.md' "$file"; then
        # File contains markdown links - could validate these more thoroughly
        :
    fi
done <<< "$MARKDOWN_FILES"

echo ""

# Check mkdocs.yml syntax
echo "⚙️  Checking mkdocs.yml..."

if python3 -c "import yaml; yaml.safe_load(open('mkdocs.yml'))" 2>/dev/null; then
    echo "  ✅ Valid YAML syntax"
else
    echo "  ❌ Invalid YAML syntax in mkdocs.yml"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "=============================================="

if [ $ERRORS -eq 0 ]; then
    echo "✅ Validation passed!"
    echo ""
    echo "Next steps:"
    echo "  - Serve locally: bash .agent/skills/documentation/scripts/mkdocs_serve.sh"
    echo "  - Build: bash .agent/skills/documentation/scripts/mkdocs_build.sh"
    exit 0
else
    echo "❌ Found $ERRORS error(s)"
    echo ""
    echo "Fix the errors listed above and try again"
    exit 1
fi
