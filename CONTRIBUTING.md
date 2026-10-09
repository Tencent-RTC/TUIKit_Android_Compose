# Contributing to TUIKit_Android_Compose

We welcome contributions to TUIKit_Android_Compose! This document provides guidelines for contributing to the project.

## Code of Conduct

By participating in this project, you agree to abide by our Code of Conduct. Please treat all community members with respect and create a welcoming environment for everyone.

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17
- Android SDK Platforms for API levels 34 and 35 installed (the demo app runs on Android 6.0, API level 23, or higher)
- Git for version control

The Gradle Wrapper (Gradle 8.9) is included in `chat/demo`, so you don't need to install Gradle separately.

### Setting Up Development Environment

1. **Fork the Repository**
   ```bash
   # Fork the repo on GitHub, then clone your fork
   git clone https://github.com/<your-username>/TUIKit_Android_Compose.git
   cd TUIKit_Android_Compose/chat/demo
   ```

2. **Open in Android Studio**
   ```bash
   # Open the project in Android Studio
   # File -> Open -> Select the chat/demo directory
   ```

3. **Sync Project**
   - Android Studio will automatically sync Gradle dependencies
   - Wait for the sync to complete

## Development Guidelines

### Kotlin/Java Coding Standards

We strictly follow the [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide) and our project-specific coding standards. Please ensure your code adheres to these guidelines:

#### Mandatory Requirements

1. **File Naming**
   - Kotlin source files end with `.kt`
   - Java source files end with `.java`
   - File names describe the main content
   - Activity files: `ActivityName.kt`
   - Fragment files: `FragmentName.kt`
   - Composable files are named after their main composable, e.g. `MessageList.kt`
   - No spaces in file names

2. **Line Length**
   - Maximum 120 characters per line
   - Exceptions: comments, URLs, import statements

3. **Indentation and Spacing**
   - Use 4 spaces for indentation
   - Opening brace `{` on the same line
   - Closing brace `}` on a new line
   - One blank line between methods and class declarations

4. **Naming Conventions**
   - Use `camelCase` for variables, functions, and properties
   - Use `PascalCase` for classes, interfaces, and `@Composable` functions that emit UI
   - Use `UPPER_SNAKE_CASE` for constants
   - Use descriptive names that clearly indicate purpose

### Architecture Principles

1. **Component-Based Architecture**
   - Each feature must be designed as an independent component
   - Components must be self-contained and independently testable
   - Clear responsibility boundaries for each component

2. **Test-Driven Development (TDD)**
   - Write tests before implementation
   - Follow Red-Green-Refactor cycle
   - Maintain minimum 80% test coverage

3. **Real-Time Communication Quality**
   - End-to-end integration tests for audio/video features
   - Network quality monitoring and error handling
   - Support for multiple audio routes and device adaptation

## Contribution Process

### 1. Issue Reporting

Before creating a new issue, please:
- Search existing issues to avoid duplicates
- Use the appropriate issue template
- Provide detailed reproduction steps for bugs
- Include relevant system information (Android version, device model, etc.)

### 2. Feature Requests

For new features:
- Describe the use case and expected behavior
- Explain why this feature would be valuable
- Consider backward compatibility implications

### 3. Pull Request Process

1. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Your Changes**
   - Follow coding standards
   - Add tests for new functionality
   - Update documentation as needed

3. **Test Your Changes**
   ```bash
   cd chat/demo
   # Build the demo app
   ./gradlew :app:assembleDebug
   # Run unit tests
   ./gradlew test
   # Run instrumented tests
   ./gradlew connectedAndroidTest
   # Test on physical devices when possible
   ```

4. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "feat: add amazing new feature"
   ```

   Use conventional commit messages:
   - `feat:` for new features
   - `fix:` for bug fixes
   - `docs:` for documentation changes
   - `test:` for test additions/modifications
   - `refactor:` for code refactoring
   - `style:` for formatting changes

5. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   ```

   Then create a Pull Request on GitHub with:
   - Clear title and description
   - Reference to related issues
   - Screenshots/videos for UI changes
   - Test results and coverage information

### 4. Code Review Process

All submissions require code review. We use GitHub's review system for this purpose. Reviews focus on:

- **Code Quality**: Adherence to coding standards
- **Architecture**: Proper component design and separation of concerns
- **Testing**: Adequate test coverage and quality
- **Performance**: Efficient implementation, especially for real-time features
- **Documentation**: Clear comments and updated documentation

## Documentation

### Code Documentation

- Use KDoc for Kotlin documentation
- Use JavaDoc for Java documentation
- Document public APIs thoroughly
- Include usage examples for complex functionality
- Keep documentation up-to-date with code changes

### README Updates

When adding new features:
- Update feature lists in both `README.md` and `README.cn.md`
- Add usage examples
- Update architecture diagrams if needed

## Release Process

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist

Before releasing:
- [ ] All tests pass
- [ ] Documentation is updated
- [ ] CHANGELOG.md is updated
- [ ] Version numbers are bumped appropriately
- [ ] Release notes are prepared

## Getting Help

If you need help:

1. **Issues**: Search existing [GitHub issues](https://github.com/Tencent-RTC/TUIKit_Android_Compose/issues)
2. **Community**: Join our developer community discussions

## Recognition

Contributors will be recognized in our:
- Release notes for significant contributions
- Project documentation

Thank you for contributing to TUIKit_Android_Compose!
