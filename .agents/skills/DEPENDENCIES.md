# Skill Dependencies

This document lists all dependencies required to use the ComposeWebView Agent Skills.

## System Requirements

### Operating System
- **macOS**: Fully supported
- **Linux**: Fully supported
- **Windows**: Requires WSL (Windows Subsystem for Linux) or Git Bash for symlink support

### Shell
- Bash (sh-compatible shell)
- Available by default on macOS and Linux
- On Windows: Use WSL or Git Bash

## Development Skill Dependencies

### Required
- **JDK 17+**: For Kotlin/Gradle compilation
  ```bash
  # Verify installation
  java -version
  ```

- **Gradle**: Via wrapper (included in project)
  ```bash
  ./gradlew --version
  ```

### Platform-Specific

#### Android Development
- Android SDK (automatically downloaded by Gradle)
- `ANDROID_HOME` environment variable (optional, auto-configured)

#### iOS Development (macOS only)
- Xcode 14.0+
- Xcode Command Line Tools
  ```bash
  xcode-select --install
  ```

#### Desktop Development
- No additional requirements (uses JVM)

#### Web Development
- No additional requirements (Kotlin/JS)

## Documentation Skill Dependencies

### Required
- **Python 3.x**
  ```bash
  # Verify installation
  python3 --version
  ```

- **MkDocs with Material theme**
  ```bash
  # Install
  pip install mkdocs-material

  # Verify
  mkdocs --version
  ```

### Optional
- **Dokka** (for automated API documentation)
  - Already configured in Gradle

## Code Review Skill Dependencies

### Required
- **Bash**: For script execution
- **grep, find, awk**: Standard Unix tools (pre-installed on macOS/Linux)
- **Gradle**: Via wrapper (for Spotless, linting)

### No Additional Dependencies
All code review functionality uses tools already required by the Development Skill.

## Git Configuration (for symlinks)

Ensure Git handles symlinks correctly:

```bash
# Check current setting
git config core.symlinks

# Enable symlinks (if needed)
git config core.symlinks true
```

**Note**: On Windows, Git symlink support requires:
- Developer mode enabled, OR
- Git for Windows with symlink support enabled, OR
- Running Git Bash as administrator

## Installation Verification

Run this command to verify all essential dependencies:

```bash
# Check Java
java -version && echo "✅ Java OK" || echo "❌ Java missing"

# Check Gradle
./gradlew --version && echo "✅ Gradle OK" || echo "❌ Gradle missing"

# Check Python (for docs)
python3 --version && echo "✅ Python OK" || echo "❌ Python missing"

# Check MkDocs (for docs)
mkdocs --version && echo "✅ MkDocs OK" || echo "❌ MkDocs missing"

# Check Git symlinks
git config core.symlinks && echo "✅ Git symlinks enabled" || echo "⚠️ Git symlinks not enabled"
```

## Platform-Specific Setup

### macOS
All tools should work out of the box after installing:
1. Xcode (for iOS development)
2. Homebrew (optional, for package management)
3. Python via Homebrew: `brew install python`

### Linux (Ubuntu/Debian)
```bash
# Install Java
sudo apt update
sudo apt install openjdk-17-jdk

# Install Python and pip
sudo apt install python3 python3-pip

# Install MkDocs
pip3 install mkdocs-material
```

### Windows (WSL)
1. Install WSL 2: Follow [Microsoft's WSL installation guide](https://docs.microsoft.com/en-us/windows/wsl/install)
2. Inside WSL, follow Linux instructions above
3. Clone the repository inside WSL (not Windows filesystem)

## Troubleshooting

### Symlink Issues on Windows
**Problem**: `.claude/skills` symlink doesn't work

**Solutions**:
1. Use WSL instead of native Windows
2. Enable Developer Mode in Windows Settings
3. Run Git Bash as Administrator
4. Alternatively, copy `.agent/skills/` to `.claude/skills/` (not recommended - creates duplication)

### Script Permission Denied
**Problem**: `bash: permission denied` when running scripts

**Solution**:
```bash
# Make all scripts executable
chmod +x .agent/skills/development/scripts/*.sh
chmod +x .agent/skills/documentation/scripts/*.sh
chmod +x .agent/skills/code-review/scripts/*.sh
```

### MkDocs Module Not Found
**Problem**: `ModuleNotFoundError: No module named 'mkdocs'`

**Solution**:
```bash
# Install with pip3 (not pip)
pip3 install mkdocs-material

# Verify Python version
python3 --version  # Should be 3.7+
```

### Gradle Build Fails
**Problem**: Gradle build errors

**Solutions**:
1. Verify Java version: `java -version` (should be 17+)
2. Clean Gradle cache: `./gradlew clean`
3. Invalidate Gradle caches: `rm -rf .gradle`

## Optional Tools

These tools enhance the development experience but are not required:

- **Docker**: For containerized builds (advanced)
- **ktlint**: For additional Kotlin linting (included via Spotless)
- **detekt**: For static code analysis (optional)

## Environment Variables

### Required
None. The project uses sensible defaults.

### Optional
- `ANDROID_HOME`: Android SDK location (auto-detected)
- `JAVA_HOME`: Java installation path (auto-detected)

## Updating Dependencies

To update skill dependencies:

1. **Development**: Update Gradle dependencies in `build.gradle.kts`
2. **Documentation**: Update MkDocs: `pip install --upgrade mkdocs-material`
3. **Code Review**: No manual updates needed (uses project tools)

---

Last updated: 2025-12-28
