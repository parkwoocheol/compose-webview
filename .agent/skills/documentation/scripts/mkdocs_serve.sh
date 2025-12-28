#!/bin/bash
# Serves MkDocs documentation locally with error handling

echo "📚 Serving ComposeWebView Documentation..."
echo "=============================================="
echo ""

# Check if mkdocs is installed
if ! command -v mkdocs &> /dev/null; then
    echo "❌ MkDocs not installed"
    echo ""
    echo "Install with:"
    echo "  pip install mkdocs-material"
    echo ""
    echo "Or with pip3:"
    echo "  pip3 install mkdocs-material"
    echo ""
    exit 1
fi

# Check if mkdocs.yml exists
if [ ! -f "mkdocs.yml" ]; then
    echo "❌ mkdocs.yml not found"
    echo "Please run this script from the project root directory"
    exit 1
fi

echo "✅ MkDocs is installed"
echo "✅ Configuration found"
echo ""
echo "🌐 Starting server at http://127.0.0.1:8000"
echo ""
echo "Press Ctrl+C to stop the server"
echo "=============================================="
echo ""

# Serve documentation
mkdocs serve
