# Changelog

All notable changes to TUIKit_Android_Compose will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Audio/video calling through TUICallKit (`call/tuicallkit-kt`), including a Calls tab with recent calls in the demo
- Message quoting and replying
- Text-to-speech for messages, with voice message settings and voice cloning in the demo
- Customer service conversation and a custom link message example in the demo
- English and Simplified Chinese README documentation
- Contributing guidelines and development workflow
- Proper root .gitignore for Android Kotlin/Java projects

### Changed
- Restructured the repository to match TUIKit_Android:
  - Chat UI components moved from the Compose `atomic-x` module (`io.trtc.tuikit.atomicx.*`) to `chat/uikit` (`io.trtc.tuikit.chat.uikit.components.*`)
  - Chat pages moved from `io.trtc.tuikit.chat.pages` to `io.trtc.tuikit.chat.uikit.pages`
  - `atomic_x` is now the base module shared with TUIKit_Android

### Removed
- The Compose `atomic-x` module. Its components are now part of `chat/uikit`

## [3.6.0] - 2026-01-13

### Added
- @mentions in the message input
- Message forwarding, one by one or merged, and merged message details
- Message read receipts, reactions, and multi-selection
- Display of voice-to-text results

## [3.5.0] - 2025-11-19

### Added
- Configuration options for conversation actions (delete, mute, pin, mark as unread, and clear history)
- Configuration options for the message list and message input, including custom message actions

### Changed
- Redesigned voice message recording, including the recording panel and gestures

## [3.4.0] - 2025-09-29

### Added
- Initial release of TUIKit_Android_Compose
- Compose `atomic-x` module with the conversation list, message list, message input, contact list, chat settings, media pickers, image viewer, and video recorder and player
- Demo application

---

## Release Notes Format

### Types of Changes
- `Added` for new features
- `Changed` for changes in existing functionality
- `Deprecated` for soon-to-be removed features
- `Removed` for now removed features
- `Fixed` for any bug fixes
- `Security` for vulnerability fixes

### Version Format
- Follow [Semantic Versioning](https://semver.org/)
- MAJOR.MINOR.PATCH format
- Include release date in YYYY-MM-DD format
