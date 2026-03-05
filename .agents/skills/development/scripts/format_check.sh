#!/bin/bash
# Checks code formatting using Spotless (for CI/pre-commit hooks)

echo "🎨 Checking code formatting with Spotless..."
echo "=============================================="
echo ""

if ./gradlew spotlessCheck; then
    echo ""
    echo "✅ Code is properly formatted"
    exit 0
else
    echo ""
    echo "❌ Code needs formatting"
    echo ""
    echo "To fix formatting issues, run:"
    echo "  ./gradlew spotlessApply"
    echo ""
    exit 1
fi
