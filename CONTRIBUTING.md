# Contributing to Watch Dogs 2 Map App

Thank you for your interest in contributing to the Watch Dogs 2 Map App! This document provides guidelines for contributing to the project.

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on what is best for the community
- Show empathy towards other community members

## How to Contribute

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce**
- **Expected behavior**
- **Actual behavior**
- **Screenshots** (if applicable)
- **Device/Emulator info** (Android version, device model)
- **App version**

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

- **Clear title and description**
- **Current behavior** vs **desired behavior**
- **Why this enhancement would be useful**
- **Possible implementation** (optional)

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Follow the coding style** used in the project
3. **Write clear commit messages**
4. **Test your changes** thoroughly
5. **Update documentation** if needed
6. **Submit a pull request**

## Development Guidelines

### Code Style

- **Kotlin**: Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Naming**: Use descriptive names for variables, functions, and classes
- **Comments**: Add comments for complex logic, not obvious code
- **Formatting**: Use Android Studio's auto-formatting (Ctrl+Alt+L)

### Theme Guidelines

Keep the Watch Dogs 2 cyberpunk aesthetic:
- **Primary Color**: Neon Cyan (#00FFF7)
- **Background**: Dark blacks and grays
- **Accents**: Neon pink (#FF006E) for special elements
- **Fonts**: Bold for important text, regular for body
- **Effects**: Use shadow radius for neon glow effects

### Commit Message Format

```
<type>: <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

Example:
```
feat: Add mission marker types

- Added new marker types for Watch Dogs 2 missions
- Implemented color coding for mission difficulty
- Updated map style to highlight mission areas

Closes #123
```

### Testing

- Test on multiple Android versions (minimum SDK 24)
- Test on different screen sizes
- Test with and without location permissions
- Test Spotify integration (if modified)
- Verify map interactions work correctly

### What to Contribute

We welcome contributions in these areas:

#### Features
- [ ] Additional Watch Dogs 2 themed markers and locations
- [ ] Mission system with objectives
- [ ] AR features for real-world hacking
- [ ] Achievement system
- [ ] Photo mode
- [ ] Custom playlist integration
- [ ] Social features (share locations)

#### Improvements
- [ ] Performance optimizations
- [ ] Better error handling
- [ ] Accessibility improvements
- [ ] Offline map support
- [ ] Better animation effects
- [ ] Sound effects

#### Documentation
- [ ] Code documentation
- [ ] User guides
- [ ] Video tutorials
- [ ] Translation to other languages

#### Design
- [ ] UI/UX improvements
- [ ] Custom icons
- [ ] Map style refinements
- [ ] Loading screens
- [ ] Splash screen animations

## Project Structure

```
app/src/main/
├── java/com/jeremylakeyjr/watchdogsmap/
│   ├── MainActivity.kt          # Main app logic
│   └── CustomInfoWindowAdapter.kt  # Info window styling
├── res/
│   ├── layout/                  # XML layouts
│   ├── drawable/                # Icons and graphics
│   ├── raw/                     # Map style JSON
│   └── values/                  # Colors, themes, strings
└── AndroidManifest.xml          # App configuration
```

## Resources

- [Android Developer Guide](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Google Maps Platform](https://developers.google.com/maps/documentation)
- [Spotify SDK Documentation](https://developer.spotify.com/documentation/android)
- [Material Design Guidelines](https://material.io/design)

## Questions?

Feel free to:
- Open an issue with the `question` label
- Start a discussion in GitHub Discussions
- Contact the maintainer

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to the Watch Dogs 2 Map App! Your efforts help make this project better for everyone. 🎮🗺️
