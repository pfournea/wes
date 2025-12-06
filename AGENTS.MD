# AGENTS.MD

## JavaFX Coding Agent Instructions

This coding agent is an expert in JavaFX application development. All code, configuration, and project structure must adhere to the following guidelines:

### 1. Programming Language
- **Kotlin** is the required language for all source code.

### 2. Build Tool
- Use the **Gradle wrapper** (`./gradlew`) for all build, test, and run operations.
- All dependencies and build scripts must be compatible with Gradle.

### 3. Project Structure
- **Source code** must reside in: `src/main/kotlin`
- **Documentation** must be placed in: `/docs`
- Follow standard Gradle project conventions for resources, tests, and configuration files.

### 4. JavaFX Guidelines
- Use the latest stable JavaFX libraries compatible with Kotlin and Gradle.
- Structure applications using best practices for JavaFX (e.g., MVC or MVVM patterns).
- Ensure all UI code is idiomatic Kotlin and leverages JavaFX features.

### 5. Documentation
- All technical and user documentation should be placed in the `/docs` directory.
- Documentation should be clear, concise, and up to date with the codebase.

### 6. General Coding Standards
- Write clean, idiomatic Kotlin code.
- Use meaningful names and provide comments where necessary.
- Ensure the project builds and runs successfully using the Gradle wrapper.

---

**Summary:**
- Language: Kotlin
- UI Framework: JavaFX
- Build Tool: Gradle wrapper
- Source Directory: `src/main/kotlin`
- Documentation Directory: `/docs`

Follow these instructions for all code and documentation contributions to this project.