# AGENTS.MD - Coding Agent Instructions

## Build/Test Commands
- Build: `./gradlew build`
- Run app: `./gradlew run`
- Run all tests: `./gradlew test`
- Run single test: `./gradlew test --tests "ClassName"` (e.g., `--tests "PhotoServiceTest"`)
- Clean: `./gradlew clean`

## Language & Framework
- Kotlin 2.2.21 with JVM target 21
- JavaFX 21.0.2 for UI
- JUnit 5 for testing

## Code Style

### Imports & Formatting
- Group imports by: domain, javafx, util, java stdlib
- No wildcard imports
- Organize: package > imports > blank line > KDoc > class

### Naming & Structure
- Classes: PascalCase (e.g., `PhotoService`, `CategoryCard`)
- Functions: camelCase with descriptive verb+noun (e.g., `getPhotoById`, `handleDragDetected`)
- Properties: camelCase for mutable, UPPER_SNAKE_CASE for constants in `object`
- Packages: lowercase, organized as `domain/model`, `domain/service`, `ui/component`, `ui/handler`, `util`

### Types & Nullability
- Use explicit types for public APIs; infer for local variables
- Prefer immutability: `val` over `var`, `List` over `MutableList` in return types
- Use `?` for nullable types, handle with `?.`, `?:`, or explicit null checks
- Return copies from services (e.g., `photos.toList()`) to protect internal state

### Documentation & Testing
- KDoc on all public classes and functions with `@param`, `@return` where applicable
- Test structure: `@Nested` inner classes with `@DisplayName`, backtick test names
- Test pattern: arrange-act-assert with descriptive assertions

### Domain-Driven Design
- Pure domain models in `domain/model` (no UI dependencies)
- Business logic in `domain/service`
- UI concerns strictly in `ui` package
- Use dependency injection via constructor parameters
