# ComposeWebView Agent Skills

This directory contains Agent Skills following the [agentskills.io](https://agentskills.io) open format standard for the ComposeWebView library.

## What are Agent Skills?

Agent Skills are modular capabilities that extend AI agents' functionality. Each Skill packages instructions, scripts, and resources that agents use automatically when relevant to your task.

## Available Skills

### 🏗️ Development (`development/`)
**Purpose**: Automates building, testing, and formatting across all platforms (Android, iOS, Desktop, Web).

**Use when**: You need to build, test, format code, or check multiplatform implementation status.

**Key Features**:
- Build all platforms with one command
- Run comprehensive test suites
- Apply Spotless formatting
- Verify expect/actual implementation completeness

**Quick Start**:
```bash
# Build all platforms
bash .agent/skills/development/scripts/build_all.sh

# Run all tests
bash .agent/skills/development/scripts/test_all.sh

# Check formatting
bash .agent/skills/development/scripts/format_check.sh
```

---

### 📚 Documentation (`documentation/`)
**Purpose**: Manages MkDocs documentation site and API references.

**Use when**: Creating, updating, building documentation, or adding new guides.

**Key Features**:
- Serve documentation locally
- Build and validate docs
- Generate API documentation
- Documentation templates

**Quick Start**:
```bash
# Serve docs locally at http://127.0.0.1:8000
bash .agent/skills/documentation/scripts/mkdocs_serve.sh

# Build documentation
bash .agent/skills/documentation/scripts/mkdocs_build.sh
```

---

### ✅ Code Review (`code-review/`)
**Purpose**: Performs quality checks, validates multiplatform patterns, and assists PR reviews.

**Use when**: Reviewing PRs, checking code quality, or validating new features.

**Key Features**:
- Automated review checklist
- expect/actual validation
- KDoc coverage verification
- Multiplatform pattern checks

**Quick Start**:
```bash
# Run complete review workflow
bash .agent/skills/code-review/scripts/review_checklist.sh

# Check expect/actual pairs
bash .agent/skills/code-review/scripts/check_expect_actual.sh
```

---

## How Skills Work

Skills are automatically discovered by compatible AI assistants including:
- **Claude Code** (via `.claude/skills/` symlink)
- **Cursor**
- **VS Code Copilot**
- **Other agents** supporting agentskills standard

### Skill Structure

Each skill follows this structure:
```
skill-name/
├── SKILL.md              # Main skill definition with YAML frontmatter
├── scripts/              # Executable scripts
├── reference/            # Reference documentation
└── templates/            # Code and document templates
```

### Progressive Loading

Skills use progressive loading for efficiency:
1. **Metadata** (YAML frontmatter) - Always loaded
2. **Instructions** (SKILL.md) - Loaded when skill activates
3. **Resources** (scripts, references) - Loaded as needed

## Architecture

This project uses a unified `.agent/` directory structure:

```
.agent/
├── knowledge/    # Static documentation and canonical guidance
├── tools/        # Reserved for future use
└── skills/       # Executable workflows
```

**Claude Code Compatibility**: `.claude/skills/` is a symlink to `.agent/skills/`

## Dependencies

See [DEPENDENCIES.md](DEPENDENCIES.md) for required tools and installation instructions.

## Adding New Skills

To create a new skill:

1. Create directory under `.agent/skills/[skill-name]/`
2. Add `SKILL.md` with proper YAML frontmatter:
   ```yaml
   ---
   name: skill-name
   description: Clear description of what this skill does and when to use it
   ---
   ```
3. Organize scripts in `scripts/` subdirectory
4. Use progressive loading for complex content
5. Test across AI platforms

## Related Resources

- **Knowledge Base**: `.agent/knowledge/` - Project documentation
- **MkDocs**: `docs/` - User-facing documentation

## License

This skills infrastructure is part of the ComposeWebView project.
Licensed under the same terms as the main project.

---

*Skills infrastructure follows the open [agentskills](https://agentskills.io) standard for cross-platform AI agent compatibility.*
