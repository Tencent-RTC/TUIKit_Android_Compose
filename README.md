# TUIKit_Android_Compose

English | [简体中文](README.cn.md)

## Overview

TUIKit_Android_Compose is a UI component library built with Jetpack Compose on top of Tencent Cloud's `AtomicXCore` SDK. `AtomicXCore` integrates the core capabilities of Tencent Cloud Instant Messaging (IM), Real-Time Communication (TRTC), and Audio/Video Calling (TUICallEngine), providing a state-driven API design.

TUIKit_Android_Compose provides a set of pre-built user interfaces (UI) on top of the core capabilities offered by `AtomicXCore`, enabling you to quickly integrate instant messaging and audio/video calling into your Android applications without worrying about complex backend logic and state management.

TUIKit_Android_Compose is the Jetpack Compose edition of [TUIKit_Android](https://github.com/Tencent-RTC/TUIKit_Android) and currently covers the Chat and Call scenarios.

> **Note:**
> In respect for the copyright of the emoji design, this project does not include the cutouts of large emoji elements. Please replace them with your own designed or copyrighted emoji packs before the official launch for commercial use. The default small yellow face emoji pack is copyrighted by Tencent Cloud and can be authorized for a fee. If you wish to obtain authorization, please [Contact Us](https://trtc.io/contact).
>
> <img src="https://qcloudimg.tencent-cloud.cn/image/document/6438e8feb7bba909511e0d798dfaf91d.png" width="300px" />

## Features

TUIKit_Android_Compose provides complete UI implementations for the following core business scenarios based on `AtomicXCore`:

* **Instant Messaging (Chat):**

    * **Conversation Management:** Support fetching and managing conversation lists.
    * **Message Sending/Receiving:** Support C2C (one-to-one) and Group chat scenarios, with multiple message types including text, images, voice, video, files, etc.
    * **Message Interaction:** Support quoting, forwarding, recalling, read receipts, reactions, and @mentions.
    * **AI Capabilities:** Support voice-to-text, translation, and text-to-speech for messages.
    * **Search:** Support searching messages, contacts, and groups.
    * **Contact Management:** Support friend, friend request, and blacklist management.
    * **Group Management:** Support group profile, group member, and group settings management.

* **Audio/Video Calling:**

    * **Basic Calling:** Support 1v1 and multi-party audio/video calls.
    * **Call Management:** Support answering, rejecting, and hanging up calls.
    * **Device Management:** Support camera and microphone control during calls.
    * **Call History:** Support querying and deleting call records.

## Quick Start

### 1. Environment Setup

* Android Studio Ladybug or newer
* JDK 17
* An Android device or emulator running Android 6.0 (API level 23) or higher.

The Gradle Wrapper (Gradle 8.9) is included. The project uses Android Gradle Plugin 8.6.1 and Kotlin 2.0.21.

### 2. Clone Repository

```bash
git clone https://github.com/Tencent-RTC/TUIKit_Android_Compose.git
```

### 3. Configure SDKAppID and SecretKey

1. Log in to the [Chat Console](https://console.trtc.io/), create an application (or select an existing one), and note down its **SDKAppID** and **SecretKey**.
2. Open `chat/demo/app/src/main/java/io/trtc/tuikit/chat/signature/GenerateTestUserSig.java` and fill in both values:

   ```java
   public static final int SDKAPPID = 1400000001;              // your SDKAppID
   private static final String SECRETKEY = "your_secret_key";   // your SecretKey
   ```

   Both values are intentionally left empty in this repository, so the demo will not compile until you fill them in.

> The demo calculates `UserSig` on the client only to keep the first run simple. A SecretKey shipped inside an APK can be extracted by decompiling it, so this method is only suitable for running the demo and debugging features. In production, `UserSig` must be issued by your own server. For details, see [How do I calculate UserSig on the server?](https://trtc.io/document/34385?product=chat&menulabel=serverapis)

### 4. Run Project

1. Open the `chat/demo` directory in Android Studio and wait for the Gradle sync to complete.
2. Select the `app` run configuration and a device, then click Run.

To build from the command line:

```bash
cd chat/demo
./gradlew :app:assembleDebug
```

See the [Chat Demo README](chat/demo/README.md) for more about the demo.

## Architecture

The architecture design of `TUIKit_Android_Compose` follows layered principles:

1. **TUIKit_Android_Compose (UI Layer):**

    * Provides pre-built, reusable UI components. The Chat components (`chat/uikit`) are built with Jetpack Compose.
    * Audio/video calling is provided by TUICallKit (`call/tuicallkit-kt`). It is implemented with Android Views, shared with TUIKit_Android, and integrated into the Compose demo.
    * `atomic_x` is the base module shared with TUIKit_Android, providing theme tokens, permission requests, a floating window, and common utilities.
    * Subscribes to `Store` in `AtomicXCore` to get state and update UI, and calls `Store` methods to respond to user operations.

2. **AtomicXCore (Core Layer):**

    * **Stores:** (such as `ConversationListStore`, `MessageListStore`, `ContactStore`, `GroupStore`) responsible for managing business logic and state.
    * **Engine Wrapper:** Encapsulates the underlying `IMSDK`, `TUICallEngine`, and `RTCRoomEngine`, providing unified APIs.

3. **Tencent Cloud SDK (Engine Layer):**

    * `IMSDK`: Provides instant messaging capabilities.
    * `TUICallEngine` & `RTCRoomEngine`: Provide underlying real-time audio/video capabilities.

## License

This project is licensed under the [MIT License](LICENSE).

---

## Project Structure

```
TUIKit_Android_Compose/
├── atomic_x/              # Base module shared with TUIKit_Android
├── call/
│   └── tuicallkit-kt/     # TUICallKit: audio/video calling UI
└── chat/
    ├── uikit/             # Chat UI components built with Jetpack Compose
    └── demo/              # Demo application (standalone Gradle project)
        ├── build.gradle.kts
        ├── settings.gradle.kts
        └── app/
```

`chat/demo/settings.gradle.kts` includes `chat/uikit`, `atomic_x`, and `call/tuicallkit-kt` as local modules. All other dependencies, such as IMSDK, AtomicXCore, RTCRoomEngine, and LiteAVSDK, are resolved from Maven.

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on how to submit pull requests, report issues, and contribute to the project.

### Development Workflow

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes following our [CONTRIBUTING](./CONTRIBUTING.md)
4. Add tests for your changes
5. Ensure all tests pass and the demo builds
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

## Support

- **Issues:** [GitHub Issues](https://github.com/Tencent-RTC/TUIKit_Android_Compose/issues)
- **Community:** [Tencent Cloud Developer Community](https://cloud.tencent.com/developer)

## Changelog

See [CHANGELOG.md](./CHANGELOG.md) for a detailed history of changes to this project.

## Acknowledgments

- Built with [Tencent Cloud Chat](https://cloud.tencent.com/product/im) and [Tencent Cloud TRTC](https://cloud.tencent.com/product/trtc)
- UI framework powered by [Jetpack Compose](https://developer.android.com/compose)
