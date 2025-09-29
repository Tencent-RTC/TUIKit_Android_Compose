一 如果您在使用录制的时候提示"由于您的工程配置，您当前功能将受限...",这是因为视频录制的某些功能需要用到LiteAVSDK_Professional，而当前缺失该依赖。

解决方法1：
您需在gradle中添加如下代码，
api "com.tencent.liteav:LiteAVSDK_Professional:latest.release"
如果您的工程当前已经依赖 com.tencent.liteav:LiteAVSDK_TRTC,则需要去掉该依赖，否则会编译失败。
LiteAVSDK_Professional完全包含了LiteAVSDK_TRTC的功能，去掉LiteAVSDK_TRTC依赖增加LiteAVSDK_Professional依赖不会让功能缺失。

解决方法2:
您可以在配置代码中屏蔽引起弹窗的相关功能
配置文件位置：assets/video_recorder_config/video_recorder_config.json，配置文件如下:
{
  "max_record_duration_ms": "60000",
  "min_record_duration_ms": "2000",
  "video_quality": "2",
  "primary_theme_color": "#147AFF",
  "is_default_front_camera":"false",
  "record_mode":"0",
  "support_record_torch": "true",
  "support_record_beauty": "true",
  "support_record_aspect": "true",
  "support_record_scroll_filter": "true"
}
比如您如果想屏蔽美颜功能，将support_record_beauty项设置为false即可。如果没有依赖LiteAVSDK_Professional你可能需要屏蔽support_record_beauty，support_record_aspect，support_record_scroll_filter这三个功能



二 如果您在使用录制的时候提示"由于您未开通多媒体插件使用权限，您当前功能将受限...",这是因为视频录制的某些功能需要注册开通

解决方法1：
注册开通视频录制的高级功能并了解更多详细功能，请访问官方文档：https://cloud.tencent.com/document/product/269/113290
解决方法2：
屏蔽部份功能，屏蔽方法如问题一的解决方法2，您需要屏蔽support_record_beauty，support_record_scroll_filter这两项功能

  
  
I. If you encounter the prompt "Due to your project configuration, some features are currently restricted..." while using the recording function, this is because certain video recording features require LiteAVSDK_Professional, which is currently missing as a dependency.

Solution 1:

You need to add the following code to your gradle:

api "com.tencent.liteav:LiteAVSDK_Professional:latest.release"

If your project already depends on com.tencent.liteav:LiteAVSDK_TRTC, you must remove this dependency; otherwise, the compilation will fail.

LiteAVSDK_Professional fully includes the functionality of LiteAVSDK_TRTC. Removing the LiteAVSDK_TRTC dependency and adding the LiteAVSDK_Professional dependency will not result in any loss of functionality.

Solution 2:

You can disable the relevant feature configurations that trigger the prompt in the configuration code.

Configuration file location: assets/video_recorder_config/video_recorder_config.json. The configuration file is as follows:
{
  "max_record_duration_ms": "60000",
  "min_record_duration_ms": "2000",
  "video_quality": "2",
  "primary_theme_color": "#147AFF",
  "is_default_front_camera": "false",
  "record_mode": "0",
  "support_record_torch": "true",
  "support_record_beauty": "true",
  "support_record_aspect": "true",
  "support_record_scroll_filter": "true"
}
For example, if you want to disable the beauty filter feature, set the support_record_beautyitem to false. If you have not added the LiteAVSDK_Professional dependency, you may need to disable the following three features: support_record_beauty, support_record_aspect, and support_record_scroll_filter.

II. If you encounter the prompt "Due to your lack of permissions for the multimedia plugin, some features are currently restricted..." while using the recording function, this is because certain video recording features require registration and activation.

Solution 1:

To register and activate the advanced features of video recording and learn more about the detailed functionalities, please refer to the official documentation:

https://cloud.tencent.com/document/product/269/113290

Solution 2:

Disable some of the features. The method to disable them is the same as Solution 2 for Issue I. You need to disable the following two features: support_record_beautyand support_record_scroll_filter.
