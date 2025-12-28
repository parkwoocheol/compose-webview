#!/bin/bash
# Runs comprehensive code review checks

echo "🔍 ComposeWebView Code Review"
echo "=============================="
echo ""

ERRORS=0

# 1. Code Formatting
echo "1️⃣  Checking code formatting..."
if ./gradlew spotlessCheck > /dev/null 2>&1; then
    echo "   ✅ Formatting OK"
else
    echo "   ❌ Formatting issues found"
    echo "      Fix with: ./gradlew spotlessApply"
    ERRORS=$((ERRORS + 1))
fi

# 2. Expect/Actual completeness
echo ""
echo "2️⃣  Checking expect/actual implementations..."
if bash .agent/skills/code-review/scripts/check_expect_actual.sh > /dev/null 2>&1; then
    echo "   ✅ Expect/actual pairs complete"
else
    echo "   ❌ Missing platform implementations"
    echo "      Run: bash .agent/skills/code-review/scripts/check_expect_actual.sh"
    ERRORS=$((ERRORS + 1))
fi

# 3. KDoc coverage (warning only)
echo ""
echo "3️⃣  Checking KDoc coverage..."
if bash .agent/skills/code-review/scripts/verify_kdoc.sh > /dev/null 2>&1; then
    echo "   ✅ KDoc coverage adequate"
else
    echo "   ⚠️  Some public APIs lack KDoc (warning)"
    echo "      Run: bash .agent/skills/code-review/scripts/verify_kdoc.sh"
    # Don't increment ERRORS - this is a warning
fi

# 4. Tests
echo ""
echo "4️⃣  Running tests..."
if ./gradlew :compose-webview:allTests > /dev/null 2>&1; then
    echo "   ✅ All tests passing"
else
    echo "   ❌ Test failures detected"
    echo "      Run: ./gradlew :compose-webview:allTests"
    ERRORS=$((ERRORS + 1))
fi

# Summary
echo ""
echo "=============================="
if [ $ERRORS -eq 0 ]; then
    echo "✅ Review passed! Ready for merge."
    echo ""
    echo "Checks performed:"
    echo "  ✅ Code formatting (Spotless)"
    echo "  ✅ Expect/actual completeness"
    echo "  ✅ KDoc coverage"
    echo "  ✅ All tests passing"
    echo ""
    exit 0
else
    echo "❌ Found $ERRORS critical issue(s)"
    echo ""
    echo "Please fix the issues above before merging."
    echo ""
    echo "Quick fixes:"
    echo "  - Formatting: ./gradlew spotlessApply"
    echo "  - Missing actuals: Implement in all platform source sets"
    echo "  - Tests: Fix failing tests"
    echo ""
    exit 1
fi
