# Chat Demo (Jetpack Compose)

A runnable sample app built on top of the `uikit` module (Jetpack Compose based)
of Tencent Cloud Chat.

## 1. Prerequisites

| Item | Requirement |
| --- | --- |
| Android Studio | Ladybug or newer |
| JDK | 17 (Kotlin/Java target is 1.8) |
| Gradle | 8.x (wrapper included) |
| Kotlin / AGP | 2.0.21 / 8.6.1 |
| minSdk / targetSdk | 23 / 35 |

## 2. Configure your own SDKAppID

1. Create an application in the [Chat console](https://console.cloud.tencent.com/im)
   and copy its **SDKAppID** and **SecretKey**.
2. Fill them into
   `app/src/main/java/io/trtc/tuikit/chat/signature/GenerateTestUserSig.java`:

   ```java
   public static final int SDKAPPID = 1400000001;              // your SDKAppID
   private static final String SECRETKEY = "your_secret_key";   // your SecretKey
   ```

3. Run the app, type any `userID` (for example `test_user_1`) and tap **Login**.
   Install on a second device with a different `userID` to chat between them.

> The demo signs `UserSig` locally only to keep the first run simple. A SecretKey
> shipped inside an APK can be extracted by decompiling it, so in production
> `UserSig` must be issued by your own server. See
> [Generating UserSig](https://cloud.tencent.com/document/product/269/32688).

## 3. Build

```bash
./gradlew :app:assembleDebug
```

## 4. What this demo shows

- Conversation list, contact list, chat screen, search, chat settings
- Self profile, theme / primary color / language switching, read receipts
- Voice message settings and voice clone entries
- Audio & video calling through `tuicallkit-kt`
- Customer service conversation and a custom "link" message type

## 5. Not included

Offline push (TIMPush) and phone-number login are specific to the Tencent Cloud
demo backend and vendor accounts, so they are not part of this sample. To add
offline push to your own app, follow
[Offline Push](https://cloud.tencent.com/document/product/269/74605) and supply
your own vendor credentials.
