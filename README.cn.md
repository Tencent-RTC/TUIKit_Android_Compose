# TUIKit_Android_Compose

[English](README.md) | 简体中文

## 概述

TUIKit_Android_Compose 是一款基于 Jetpack Compose 的 UI 组件库，它基于腾讯云 `AtomicXCore` SDK 构建。`AtomicXCore` 整合了腾讯云即时通信（IM）、实时音视频（TRTC）和音视频通话（TUICallEngine）的核心能力，提供了状态驱动的（State-driven）API 设计。

TUIKit_Android_Compose 在 `AtomicXCore` 提供的核心能力之上，为您提供了一套预制的用户界面（UI），使您无需关心复杂的后端逻辑和状态管理，即可快速为您的 Android 应用集成即时通信和音视频通话功能。

TUIKit_Android_Compose 是 [TUIKit_Android](https://github.com/Tencent-RTC/TUIKit_Android) 的 Jetpack Compose 版本，目前包含 Chat（即时通信）和 Call（音视频通话）两个场景。

> **注意：**
> 尊重表情设计版权，本项目不包含大表情元素切图，正式上线商用前请您替换为自己设计或拥有版权的其他表情包。默认的小黄脸表情包版权归腾讯云所有，可有偿授权使用，如需获取授权，请 [提交工单](https://console.cloud.tencent.com/workorder/category) 联系我们。
>
> <img src="https://qcloudimg.tencent-cloud.cn/image/document/6438e8feb7bba909511e0d798dfaf91d.png" width="300px" />

## 功能特性

TUIKit_Android_Compose 基于 `AtomicXCore` 提供了以下核心业务场景的完整 UI 实现：

* **即时通讯 (Chat):**

    * **会话管理:** 支持会话列表的拉取和管理。
    * **消息收发:** 支持 C2C（单聊）和 Group（群聊）场景，支持文本、图片、语音、视频、文件等多种消息类型。
    * **消息互动:** 支持引用回复、转发、撤回、已读回执、表情回应和 @ 提及。
    * **AI 能力:** 支持消息的语音转文字、翻译和文字转语音。
    * **搜索:** 支持搜索消息、联系人和群组。
    * **联系人管理:** 支持好友、好友申请和黑名单管理。
    * **群组管理:** 支持群资料、群成员和群设置管理。

* **音视频通话 (Calling):**

    * **基础通话:** 支持 1v1 及多人音视频通话。
    * **通话管理:** 支持接听、拒绝、挂断。
    * **设备管理:** 支持通话中的摄像头和麦克风控制。
    * **通话记录:** 支持查询和删除通话记录。

## 快速开始

### 1. 环境准备

* Android Studio Ladybug 或更高版本
* JDK 17
* 运行 Android 6.0（API level 23）或更高版本的 Android 设备或模拟器。

工程已内置 Gradle Wrapper（Gradle 8.9），使用 Android Gradle Plugin 8.6.1 和 Kotlin 2.0.21。

### 2. 克隆仓库

```bash
git clone https://github.com/Tencent-RTC/TUIKit_Android_Compose.git
```

### 3. 配置 SDKAppID 和 SecretKey

1. 登录 [即时通信 IM 控制台](https://console.cloud.tencent.com/im)，创建应用（或选择已有应用），记录其 **SDKAppID** 和 **SecretKey**。
2. 打开 `chat/demo/app/src/main/java/io/trtc/tuikit/chat/signature/GenerateTestUserSig.java`，填写这两个值：

   ```java
   public static final int SDKAPPID = 1400000001;              // your SDKAppID
   private static final String SECRETKEY = "your_secret_key";   // your SecretKey
   ```

   仓库中这两个值是有意留空的，填写之前 Demo 无法编译通过。

> 为了让首次运行更简单，Demo 在客户端本地计算 `UserSig`。打包在 APK 中的 SecretKey 可以通过反编译被提取，因此这种方式仅适合运行 Demo 和调试功能。正式环境中，`UserSig` 必须由您的业务服务器签发，详见 [生成 UserSig](https://cloud.tencent.com/document/product/269/32688)。

### 4. 运行项目

1. 使用 Android Studio 打开 `chat/demo` 目录，等待 Gradle 同步完成。
2. 选择 `app` 运行配置和目标设备，点击运行。

也可以通过命令行构建：

```bash
cd chat/demo
./gradlew :app:assembleDebug
```

Demo 的更多说明请参见 [Chat Demo README](chat/demo/README.md)。

## 架构

`TUIKit_Android_Compose` 的架构设计遵循分层原则：

1. **TUIKit_Android_Compose (UI 层):**

    * 提供预制的、可复用的 UI 组件。Chat 组件（`chat/uikit`）基于 Jetpack Compose 实现。
    * 音视频通话由 TUICallKit（`call/tuicallkit-kt`）提供。它基于 Android View 实现，与 TUIKit_Android 共用，并集成在 Compose Demo 中。
    * `atomic_x` 是与 TUIKit_Android 共用的基础模块，提供主题 Token、权限申请、悬浮窗和通用工具。
    * 订阅 `AtomicXCore` 中的 `Store` 来获取状态并更新 UI，调用 `Store` 方法来响应用户操作。

2. **AtomicXCore (核心层):**

    * **Stores:** （如 `ConversationListStore`、`MessageListStore`、`ContactStore`、`GroupStore`）负责管理业务逻辑和状态（State）。
    * **Engine 封装:** 封装了底层的 `IMSDK`、`TUICallEngine` 和 `RTCRoomEngine`，提供统一的 API。

3. **Tencent Cloud SDK (引擎层):**

    * `IMSDK`: 提供即时通讯能力。
    * `TUICallEngine` & `RTCRoomEngine`: 提供底层的实时音视频能力。

## 许可证

本项目遵循 [MIT 许可证](LICENSE)。

---

## 目录结构

```
TUIKit_Android_Compose/
├── atomic_x/              # 与 TUIKit_Android 共用的基础模块
├── call/
│   └── tuicallkit-kt/     # TUICallKit：音视频通话 UI
└── chat/
    ├── uikit/             # 基于 Jetpack Compose 的 Chat UI 组件
    └── demo/              # Demo 应用（独立的 Gradle 工程）
        ├── build.gradle.kts
        ├── settings.gradle.kts
        └── app/
```

`chat/demo/settings.gradle.kts` 以本地模块的方式引入 `chat/uikit`、`atomic_x` 和 `call/tuicallkit-kt`，IMSDK、AtomicXCore、RTCRoomEngine、LiteAVSDK 等其他依赖均从 Maven 获取。

## 参与贡献

欢迎参与贡献！提交 Pull Request、反馈问题等方式请参见 [贡献指南](CONTRIBUTING.md)。

### 开发流程

1. Fork 本仓库
2. 创建功能分支（`git checkout -b feature/amazing-feature`）
3. 按照 [CONTRIBUTING](./CONTRIBUTING.md) 中的规范进行修改
4. 为您的修改添加测试
5. 确保所有测试通过且 Demo 能编译成功
6. 提交修改（`git commit -m 'Add amazing feature'`）
7. 推送到分支（`git push origin feature/amazing-feature`）
8. 发起 Pull Request

## 支持

- **问题反馈:** [GitHub Issues](https://github.com/Tencent-RTC/TUIKit_Android_Compose/issues)
- **开发者社区:** [腾讯云开发者社区](https://cloud.tencent.com/developer)

## 更新日志

本项目的详细变更记录请参见 [CHANGELOG.md](./CHANGELOG.md)。

## 致谢

- 基于 [腾讯云即时通信 IM](https://cloud.tencent.com/product/im) 和 [腾讯云实时音视频 TRTC](https://cloud.tencent.com/product/trtc) 构建
- UI 框架基于 [Jetpack Compose](https://developer.android.com/compose)
