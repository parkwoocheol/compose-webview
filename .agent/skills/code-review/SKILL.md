---
name: composewebview-code-review
description: Performs code quality checks and reviews for ComposeWebView. Validates expect/actual implementations, KDoc coverage, Spotless formatting, and multiplatform patterns. Use when reviewing PRs, checking code quality, or validating new features.
---

# ComposeWebView Code Review & Quality

This skill automates code quality checks and assists with pull request reviews for the ComposeWebView multiplatform library.

## Quick Review

Run complete review workflow:

```bash
bash .agent/skills/code-review/scripts/review_checklist.sh
```

This executes all quality checks: formatting, expect/actual validation, KDoc coverage, and tests.

## Review Categories

### 1. Code Formatting

**Check formatting** (non-destructive):
```bash
./gradlew spotlessCheck
```

**Auto-fix formatting**:
```bash
./gradlew spotlessApply
```

Spotless enforces ktlint rules. **REQUIRED** before all commits.

### 2. Multiplatform Completeness

**Check expect/actual pairs**:
```bash
bash .agent/skills/code-review/scripts/check_expect_actual.sh
```

Verifies:
- Every `expect` declaration has corresponding `actual` implementations
- All platforms (Android, iOS, Desktop, Web) are covered
- No orphaned `actual` declarations

### 3. Documentation Coverage

**Verify KDoc**:
```bash
bash .agent/skills/code-review/scripts/verify_kdoc.sh
```

Checks:
- All `public` APIs have KDoc comments
- KDoc includes `@param` and `@return` where applicable
- No placeholder documentation (e.g., "TODO")

### 4. Testing Coverage

Ensure tests exist for:
- Common functionality in `commonTest`
- Platform-specific features in platform test sources
- Critical paths (WebView loading, JS bridge, state management)

**Run tests**:
```bash
bash .agent/skills/development/scripts/test_all.sh
```

### 5. Architecture Compliance

Verify adherence to project patterns:
- State management via `WebViewState` (see `.agent/knowledge/architecture.md`)
- Controller separation via `WebViewController`
- Proper platform abstraction (expect/actual)

See [reference/common_patterns.md](reference/common_patterns.md) for patterns.

## Review Checklists

### Feature Implementation

Copy and work through: [checklists/feature_checklist.md](checklists/feature_checklist.md)

Quick checklist:
```markdown
- [ ] Defined in commonMain with expect
- [ ] Implemented in all 4 platforms (actual)
- [ ] Public APIs have KDoc with @param/@return
- [ ] Tests added in commonTest and platform tests
- [ ] Spotless formatting applied
- [ ] Updated relevant documentation
- [ ] Follows existing patterns (State/Controller)
- [ ] Platform constraints considered
```

### Pull Request Review

Copy and work through: [checklists/pr_checklist.md](checklists/pr_checklist.md)

Essential checks:
- Code quality (formatting, naming, structure)
- Multiplatform completeness
- Test coverage
- Documentation updates
- Breaking change assessment

### Multiplatform Validation

For complex platform work: [checklists/multiplatform_checklist.md](checklists/multiplatform_checklist.md)

Platform-specific considerations:
- Android: WebView API usage, permissions
- iOS: WKWebView constraints, message handlers
- Desktop: CEF initialization, threading
- Web: IFrame limitations, postMessage bridge

## Automated Checks

### Complete Review Script

```bash
bash .agent/skills/code-review/scripts/review_checklist.sh
```

**Checks performed**:
1. ✅ Code formatting (Spotless)
2. ✅ Expect/actual completeness
3. ⚠️  KDoc coverage (warning only)
4. ✅ All tests passing

**Exit codes**:
- `0` - All checks passed
- `1` - Critical issues found (formatting, tests, expect/actual)

### Individual Checks

**Expect/Actual**:
```bash
bash .agent/skills/code-review/scripts/check_expect_actual.sh
```

**KDoc Coverage**:
```bash
bash .agent/skills/code-review/scripts/verify_kdoc.sh
```

**Formatting**:
```bash
bash .agent/skills/development/scripts/format_check.sh
```

## Common Issues & Solutions

See [reference/review_guidelines.md](reference/review_guidelines.md) for:
- Common multiplatform pitfalls
- Platform-specific gotchas (WKWebView, CEF)
- Performance considerations
- Breaking change checklist

### Quick Fixes

**Formatting issues**:
```bash
./gradlew spotlessApply
```

**Missing actual implementation**:
1. Identify the `expect` declaration
2. Add `actual` to all platform source sets
3. Verify: `bash .agent/skills/code-review/scripts/check_expect_actual.sh`

**Missing KDoc**:
```kotlin
/**
 * Brief description of what this does.
 *
 * @param param Description of parameter
 * @return Description of return value
 */
fun publicFunction(param: String): Result
```

## Review Workflow

### Before Submitting PR

1. **Format code**:
   ```bash
   ./gradlew spotlessApply
   ```

2. **Run full review**:
   ```bash
   bash .agent/skills/code-review/scripts/review_checklist.sh
   ```

3. **Fix any issues** reported

4. **Run tests**:
   ```bash
   bash .agent/skills/development/scripts/test_all.sh
   ```

5. **Commit and push**

### During Code Review

1. **Check PR against checklist**:
   - Use [pr_checklist.md](checklists/pr_checklist.md)

2. **Verify multiplatform completeness**:
   ```bash
   bash .agent/skills/code-review/scripts/check_expect_actual.sh
   ```

3. **Review architectural patterns**:
   - Refer to [common_patterns.md](reference/common_patterns.md)
   - Check `.agent/knowledge/architecture.md`

4. **Test locally**:
   ```bash
   git checkout pr-branch
   bash .agent/skills/development/scripts/test_all.sh
   ```

## Best Practices

### Code Quality

1. **Consistent naming**: Follow Kotlin conventions
   - Classes: `PascalCase`
   - Functions/properties: `camelCase`
   - Constants: `UPPER_SNAKE_CASE`

2. **Visibility modifiers**:
   - `public` - External API
   - `internal` - Internal implementations
   - `private` - Encapsulated logic

3. **Immutability**: Prefer `val` over `var`

4. **Null safety**: Avoid `!!`, use safe calls `?.` or `?:`

### Multiplatform

1. **Keep common code platform-agnostic**
2. **Use expect/actual for platform specifics**
3. **Document platform constraints**
4. **Test on all platforms**

### Documentation

1. **All public APIs** must have KDoc
2. **Include examples** in documentation
3. **Document platform differences**
4. **Keep docs up-to-date** with code

## CI/CD Integration

These checks can be integrated into GitHub Actions:

```yaml
- name: Code Review Checks
  run: bash .agent/skills/code-review/scripts/review_checklist.sh
```

## Scripts Reference

### review_checklist.sh
Comprehensive review running all checks. Returns non-zero exit code if critical issues found.

### check_expect_actual.sh
Validates that all expect declarations have actual implementations on all platforms.

### verify_kdoc.sh
Checks KDoc coverage for public APIs. Warning-level (doesn't fail build).

## Related Resources

- **Development**: [Development Skill](../development/SKILL.md)
- **Architecture**: `.agent/knowledge/architecture.md`
- **Code Style**: `.agent/knowledge/code_style.md`
- **Workflows**: `.agent/knowledge/commands.md`

## Troubleshooting

### Review Script Fails

**Check output** for specific failure:
- Formatting → Run `./gradlew spotlessApply`
- Tests → Fix failing tests
- Expect/actual → Implement missing actuals

### False Positives

**Expect/actual check** counts declarations - may show warnings for:
- Internal implementations
- Platform-specific extensions

Review build errors for actual issues.

### KDoc Warnings

Not all public APIs require extensive KDoc (e.g., simple getters). Use judgment, but prefer documentation.

---

*Use this skill to maintain high code quality and consistent patterns across the ComposeWebView codebase.*
