#!/bin/bash
# Builds MkDocs documentation with strict mode

echo "🏗️  Building ComposeWebView Documentation..."
echo "=============================================="
echo ""

# Check if mkdocs is installed
if ! command -v mkdocs &> /dev/null; then
    echo "❌ MkDocs not installed"
    echo ""
    echo "Install with:"
    echo "  pip install mkdocs-material"
    echo ""
    exit 1
fi

# Check if mkdocs.yml exists
if [ ! -f "mkdocs.yml" ]; then
    echo "❌ mkdocs.yml not found"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Clean previous build
echo "🧹 Cleaning previous build..."
rm -rf site/

# Build with strict mode
echo "🔨 Building documentation (strict mode)..."
echo ""

if mkdocs build --strict; then
    echo ""
    echo "=============================================="
    echo "✅ Documentation built successfully!"
    echo ""
    echo "Output: site/"
    echo ""
    echo "To serve locally:"
    echo "  mkdocs serve"
    echo ""
    echo "To deploy (GitHub Pages):"
    echo "  mkdocs gh-deploy"
    echo "=============================================="
    exit 0
else
    echo ""
    echo "=============================================="
    echo "❌ Build failed"
    echo ""
    echo "Common issues:"
    echo "  - Markdown syntax errors"
    echo "  - Broken internal links"
    echo "  - Missing files referenced in navigation"
    echo ""
    echo "Fix errors and try again"
    echo "=============================================="
    exit 1
fi
