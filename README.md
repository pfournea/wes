# WES Photo Categorizer

JavaFX desktop application for organizing photos into categories via drag-drop. Load a ZIP file, display photos in a grid, categorize them by dragging to category cards, and export organized photos to directories.

## Requirements

### Development
- JDK 21 (any distribution)
- Gradle 9.x (wrapper included)

### Packaging (jpackage/jpackageImage)

> **Important**: Native packaging (creating a standalone application or installer) requires a JDK that includes JavaFX jmods. Standard OpenJDK distributions do not include these.

To build the standalone application or installer, install one of the following:
- [BellSoft Liberica JDK 21 Full](https://bell-sw.com/pages/downloads/#jdk-21-lts) (recommended - ensure you select the **Full** package)
- [Azul Zulu JDK FX](https://www.azul.com/downloads/?package=jdk-fx)

Standard OpenJDK lacks the necessary JavaFX jmod files, which will cause the `jlink` stage of the packaging process to fail with errors like "module javafx.base not found".

## Build Commands

```bash
# Development
./gradlew build          # Build project
./gradlew run            # Run application
./gradlew test           # Run all tests (headless via Monocle)

# Distribution (current platform only)
./gradlew jpackageImage  # Create standalone app folder (requires JDK with JavaFX)
./gradlew jpackage       # Create platform installer (.deb/.msi/.dmg)
./gradlew runtimeZip     # Create portable zip with bundled JRE
```

See [AGENTS.md](AGENTS.md) for detailed technical documentation and project structure.
