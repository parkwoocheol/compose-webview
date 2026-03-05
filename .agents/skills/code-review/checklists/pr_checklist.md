# Pull Request Review Checklist

Use this checklist when reviewing pull requests for ComposeWebView.

## Initial Assessment

- [ ] PR title is clear and descriptive
- [ ] PR description explains the "why" behind the change
- [ ] Linked to related issues/discussions
- [ ] Labels applied correctly (feature/bug/enhancement/etc.)
- [ ] Target branch is correct (usually `main`)
- [ ] PR size is reasonable (< 500 lines preferred, split if > 1000)

## Automated Checks

- [ ] All CI/CD checks passing
- [ ] Code formatted (Spotless check passed)
- [ ] Tests passing on all platforms
- [ ] No new compiler warnings

Run locally:
```bash
bash .agent/skills/code-review/scripts/review_checklist.sh
```

## Code Quality

### Structure & Organization

- [ ] Code is well-organized and logical
- [ ] Functions are focused and small
- [ ] No code duplication (DRY principle)
- [ ] Proper separation of concerns

### Naming & Conventions

- [ ] Naming is clear and follows Kotlin conventions
- [ ] No misleading names
- [ ] Constants instead of magic numbers/strings
- [ ] Consistent naming across codebase

### Error Handling

- [ ] Errors handled appropriately
- [ ] No swallowed exceptions
- [ ] Meaningful error messages
- [ ] Proper use of nullable types

## Multiplatform Compliance

### Implementation Completeness

- [ ] All platforms implemented (Android, iOS, Desktop, Web)
- [ ] Expect/actual pairs complete
- [ ] No platform-specific code in `commonMain`

Verify:
```bash
bash .agent/skills/code-review/scripts/check_expect_actual.sh
```

### Platform Consistency

- [ ] Behavior consistent across platforms (or differences documented)
- [ ] Platform constraints considered and documented
- [ ] No hidden platform-specific assumptions

### Platform-Specific Review

#### Android
- [ ] Proper use of WebView APIs
- [ ] Lifecycle handled correctly
- [ ] Permissions declared (if needed)
- [ ] Tested on API 24+

#### iOS
- [ ] Proper use of WKWebView APIs
- [ ] WKWebView constraints handled (e.g., zoom limitations)
- [ ] Message handlers configured correctly
- [ ] Tested on iOS 14.0+

#### Desktop
- [ ] CEF initialization handled properly (async)
- [ ] Threading considerations addressed
- [ ] Tested on JVM 11+

#### Web
- [ ] IFrame/postMessage implementation correct
- [ ] Browser compatibility considered
- [ ] Tested in modern browsers

## Architecture & Patterns

- [ ] Follows established patterns (see `common_patterns.md`)
- [ ] State management via `WebViewState`
- [ ] Actions via `WebViewController`
- [ ] Proper use of expect/actual
- [ ] Composable best practices followed

## Documentation

### Code Documentation

- [ ] Public APIs have KDoc
- [ ] KDoc includes @param/@return where needed
- [ ] Examples provided for complex APIs
- [ ] Platform differences documented

Verify:
```bash
bash .agent/skills/code-review/scripts/verify_kdoc.sh
```

### User Documentation

- [ ] User-facing docs updated (if needed)
- [ ] README updated (if public-facing change)
- [ ] Migration guide provided (if breaking change)
- [ ] CHANGELOG updated (or will be before release)

## Testing

### Test Coverage

- [ ] New functionality has tests
- [ ] Tests added in `commonTest` for common logic
- [ ] Platform-specific tests where needed
- [ ] Critical paths covered

### Test Quality

- [ ] Test names are descriptive
- [ ] Tests are isolated and independent
- [ ] Meaningful assertions
- [ ] No flaky or brittle tests

### Manual Testing

- [ ] Tested on multiple platforms
- [ ] Tested in sample app
- [ ] Edge cases verified
- [ ] Performance acceptable

## Security Review

- [ ] No security vulnerabilities introduced
- [ ] No hardcoded secrets
- [ ] Input validation present
- [ ] No unintended data exposure
- [ ] JavaScript execution is safe (if applicable)

## Performance

- [ ] No performance regression
- [ ] No blocking operations on main thread
- [ ] Resources properly managed (no leaks)
- [ ] Expensive operations optimized

## Breaking Changes

If this PR contains breaking changes:

- [ ] Breaking changes are justified
- [ ] Deprecation warnings added (not direct removal)
- [ ] Migration path documented
- [ ] BREAKING CHANGE in commit message
- [ ] Version bump planned

## Backward Compatibility

- [ ] Existing functionality still works
- [ ] No unexpected behavior changes
- [ ] APIs remain stable (or deprecated gracefully)

## Dependencies

- [ ] No unnecessary dependencies added
- [ ] Dependencies are up-to-date
- [ ] Licenses compatible
- [ ] Security-audited (for new dependencies)

## Build & CI

- [ ] Builds successfully on all platforms
- [ ] CI pipeline passes
- [ ] No new warnings introduced
- [ ] Sample app still works

Local verification:
```bash
./gradlew clean build
bash .agent/skills/development/scripts/build_all.sh
```

## Git Hygiene

- [ ] Commit messages are clear and descriptive
- [ ] Commits are atomic (each does one thing)
- [ ] No merge commits (rebased if needed)
- [ ] No unrelated changes included
- [ ] No commented-out code left behind

## Reviewer Actions

### Before Approving

- [ ] Code reviewed thoroughly
- [ ] All checklist items verified
- [ ] Tested locally (for significant changes)
- [ ] Concerns addressed or noted

### Feedback Categories

Use these labels for clarity:

**Critical** (must fix before merge):
- Bugs, security issues
- Breaking changes without justification
- Missing platform implementations
- Test failures

**Important** (should fix):
- Code quality issues
- Missing documentation
- Performance concerns
- Architecture violations

**Nice-to-have** (optional):
- Style suggestions
- Refactoring ideas
- Additional test coverage

### Approval Criteria

✅ **Approve** when:
- All critical and important issues resolved
- Code quality meets standards
- Tests pass
- Documentation adequate

❌ **Request changes** when:
- Critical issues remain
- Tests fail
- Security concerns
- Architecture violations

💬 **Comment** (no approval) when:
- Need clarification
- Suggest alternatives
- Nice-to-have improvements only

## Special Cases

### Hotfix

- [ ] Truly urgent (production issue)
- [ ] Minimal changes
- [ ] Extra scrutiny on correctness
- [ ] Will be properly tested after deploy

### Dependency Update

- [ ] Changelog reviewed
- [ ] Breaking changes noted
- [ ] Tests still pass
- [ ] No security vulnerabilities

### Documentation Only

- [ ] Accuracy verified
- [ ] Links work
- [ ] Examples tested
- [ ] Spelling/grammar checked

---

## Quick Command Reference

```bash
# Full review check
bash .agent/skills/code-review/scripts/review_checklist.sh

# Check expect/actual
bash .agent/skills/code-review/scripts/check_expect_actual.sh

# Verify KDoc
bash .agent/skills/code-review/scripts/verify_kdoc.sh

# Build all platforms
bash .agent/skills/development/scripts/build_all.sh

# Run all tests
bash .agent/skills/development/scripts/test_all.sh
```

---

*Use this checklist to ensure thorough and consistent PR reviews.*
