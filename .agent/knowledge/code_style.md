# Code Style & Conventions

## Naming Rules

- **Class/Interface**: PascalCase
- **Function/Variable**: camelCase
- **Composable**: PascalCase (even for functions). Filename should match the component name.
- **Platform Implementation**:
  - `commonMain`: `expect class/fun Name`
  - `platformMain`: `actual typealias Name = PlatformClass` or `actual class Name`.
  - Use `internal` visibility for internal implementations (e.g., `ComposeWebViewImpl`).

## Formatting (Crucial)

- You MUST run **Spotless** before committing.

  ```bash
  ./gradlew :spotlessApply
  ```

- **Indentation**: 4 spaces.
- **Import Optimization**: Handled by Spotless, but manually remove unused imports if needed.

## Documentation (KDoc)

- KDoc is **mandatory** for publicly exposed APIs (`public` keyword).
- Specify `@param`, `@return` to support IDE tooltips.
