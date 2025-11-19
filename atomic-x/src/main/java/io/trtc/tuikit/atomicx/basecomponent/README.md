### BaseComponent 使用说明

### 简介

- 基于 Jetpack Compose 的基础 UI 组件、主题系统、全局配置与工具集合。
- 统一提供颜色、字体、圆角、间距等设计变量，以及可直接使用的控件（对话框、按钮、头像、徽标、滑块、开关、轻提示等）。
- 通过 AppBuilder 读取 `assets/appConfig.json` 动态配置主题、消息列表行为、会话列表能力、输入框与搜索开关、头像形状等。

### 目录结构

- `basiccontrols`: 基础控件（ActionSheet、AlertDialog、Avatar、Badge、Button、FullScreenDialog、Label、Slider、Switch、Toast、AZOrderedList）。
- `config`: AppBuilder 与全局配置（`AppBuilderConfig`）。
- `theme`: 主题系统（`ColorScheme`、`Fonts`、`Radius`、`Spacings`、`ThemeState`）。
- `utils`: 系统栏外观与应用上下文（`SystemBarUtils`、`ContextProvider`）。

### 快速开始

```kotlin
@Composable
fun Example() {
  val theme = LocalTheme.current
  Text(
    text = "Hello",
    color = theme.colors.textColorPrimary,
    style = TextStyle(
      fontSize = theme.fonts.body4Regular.size,
      fontWeight = theme.fonts.body4Regular.weight
    )
  )
}
```

### 集成与初始化

- 确保应用使用 Jetpack Compose 与 Material3。
- 主题依赖通过 `LocalTheme` 提供，组件内部从 `LocalTheme.current` 读取颜色、字体、圆角、间距。
- `ContextProvider` 通过 `ContentProvider` 初始化应用级 `Context`，通常随库自动初始化，无需手动调用。
- `AppBuilder` 在 `AppBuilderConfig` 的初始化阶段自动读取 `assets/appConfig.json`（文件名固定为 `appConfig.json`）。

### AppBuilder 配置（assets/appConfig.json）

- theme
  - `mode`: `system` | `light` | `dark`
  - `primaryColor`: 自定义主题主色（格式 `#RRGGBB`）
- messageList
  - `alignment`: `left` | `right` | `two-sided`
  - `messageActionList`: `copy` | `recall` | `delete`
- conversationList
  - `enableCreateConversation`: 是否允许创建会话（true 或 false）
  - `conversationActionList`: `delete` | `mute` | `pin` | `markUnread` | `clearHistory`
- messageInput
  - `hideSendButton`: 是否隐藏发送按钮（true 或 false）
- search
  - `hideSearch`: 是否隐藏搜索入口（true 或 false）
- avatar
  - `shape`: `circular` | `square` | `rounded`

配置说明：

- 未配置项使用 `AppBuilderConfig` 默认值。
- `primaryColor` 合法时启用自定义配色，动态生成明暗主题的色板并替换相关关键色（链接色、头像底色、自气泡色、主按钮等）。

示例（完整可用的 appConfig.json）：

```json
{
  "theme": {
    "mode": "system",
    "primaryColor": "#1C66E5"
  },
  "messageList": {
    "alignment": "two-sided",
    "messageActionList": ["copy", "recall", "delete"]
  },
  "conversationList": {
    "enableCreateConversation": true,
    "conversationActionList": ["delete", "mute", "pin", "markUnread", "clearHistory"]
  },
  "messageInput": {
    "hideSendButton": false
  },
  "search": {
    "hideSearch": false
  },
  "avatar": {
    "shape": "circular"
  }
}
```

使用配置：

```kotlin
val enableCreate = AppBuilderConfig.enableCreateConversation
val hideSearch = AppBuilderConfig.hideSearch
```

### 主题系统（theme）

- `ThemeState`
  - 以 `MMKV` 持久化当前主题，支持 `SYSTEM/LIGHT/DARK` 模式与可选 `primaryColor`。
  - 通过 `LocalTheme.current` 提供：
    - `colors: ColorScheme`
    - `fonts: FontScheme`
    - `radius: RadiusScheme`
    - `spacings: SpacingScheme`
  - 设置方法：`setThemeMode(mode)`、`setPrimaryColor(hex)`、`clearPrimaryColor()`、`setTheme(ThemeConfig)`。
  - `ColorScheme` 提供文本、背景、描边、阴影、按钮、下拉、滚动条、浮层、复选框、Toast、Tag、Switch、Slider、Tab 等成体系的设计变量（明暗两套，或自定义主色动态生成）。

示例（在界面中切换主题与主色）：

```kotlin
LocalTheme.current.setThemeMode(ThemeMode.DARK)
LocalTheme.current.setPrimaryColor("#FF6A4C")
LocalTheme.current.clearPrimaryColor()
```

### 工具（utils）

- 系统栏外观
  - `SetActivitySystemBarAppearance()`：根据主题设置状态栏/导航栏亮暗图标。
  - `SetDialogSystemBarAppearance()`：在弹窗中同步系统栏外观。
  - `SetWindowSystemBarAppearance(window)`：通用入口。
- 应用上下文
  - `ContextProvider` 暴露 `appContext`，供主题/配置等模块初始化与存取。

示例（在 Activity 的 setContent 中）：

```kotlin
SetActivitySystemBarAppearance()
```

### 基础控件（basiccontrols）

## ActionSheet

- 目的：底部操作面板。
- 入口：`ActionSheet(isVisible, options, onDismiss, onActionSelected)`。
- 选项：`ActionItem(text, isDestructive, isEnabled, value)`。
- 行为：点击空白区域或取消按钮关闭；禁用项不可点击；危险项采用错误色。

示例：

```kotlin
var show by remember { mutableStateOf(true) }
val options = listOf(
  ActionItem(text = "置顶", value = "pin"),
  ActionItem(text = "删除", isDestructive = true, value = "delete")
)
ActionSheet(
  isVisible = show,
  options = options,
  onDismiss = { show = false },
  onActionSelected = { item -> }
)
```

## AlertDialog

- 单按钮：`AlertDialog(isVisible, message, confirmText, onDismiss, onConfirm)`。
- 双按钮：`AlertDialog(isVisible, title, message, cancelText, confirmText, onDismiss, onCancel, onConfirm)`。
- 行为：点击外部区域或返回键可关闭（遵循 `DialogProperties`）。

示例：

```kotlin
var show by remember { mutableStateOf(true) }
AlertDialog(
  isVisible = show,
  message = "已复制",
  onDismiss = { show = false }
)

AlertDialog(
  isVisible = show,
  title = "删除消息",
  message = "确认删除？",
  onDismiss = { show = false },
  onCancel = { show = false },
  onConfirm = { }
)
```

## Avatar

- 用途：头像显示，支持图片、文本、图标、默认图（用户/群组）。
- 形态：
  - `Avatar(content, size, shape, status, badge, onClick)`
  - `Avatar(url, name, size, onClick)`（便捷重载）
- 内容类型：`Image(url, fallbackName)` | `Text(name)` | `Icon(painter)` | `Default(isGroup)`。
- 尺寸：`XS(24)`、`S(32)`、`M(40)`、`L(48)`、`XL(64)`、`XXL(96)`（单位 dp，对应字号/圆角随尺寸变化）。
- 形状：参数 `shape` 优先；未指定时取 `AppBuilderConfig.avatarShape`（圆形/圆角/方形）。
- 状态：`Online`/`Offline` 在右下角显示状态点。
- 徽标：`Text/Count/Dot`，自动定位在右上角。

示例：

```kotlin
Avatar(url = avatarUrl, name = "Alice", size = AvatarSize.L)
Avatar(content = AvatarContent.Text("Bob"), size = AvatarSize.M, status = AvatarStatus.Online)
```

## AZOrderedList

- 用途：按字母（含中文拼音首字母）分组的可滚动通讯录/列表。
- 入口：AZOrderedList(modifier, items, topBar, bottomBar, onItemClick)。
- 数据：AZOrderedListItem<T>（key、displayName、avatarUrl、subtitle、value）。
- 行为：
  - 基于 displayName 首字符分组；中文取拼音首字母；数字/其他归为 #。
  - 分组内按名称升序；分组标题吸顶；右侧索引条支持点击/拖拽定位。
- 自定义：
  - topBar、bottomBar 注入头部/尾部内容。
  - onItemClick 返回被点击的 AZOrderedListItem。

## Badge

- 用途：数量或红点标记。
- 类型：`Text`（圆角文本）与 `Dot`（小圆点）。

示例：

```kotlin
Badge(text = "99+")
Badge(type = BadgeType.Dot)
```

## Button

- 入口：
  - 通用：`Button(buttonContent, enabled, type, colorType, size, onClick)`
  - 便捷：`FilledButton(text)`、`OutlinedButton(text)`、`IconButton(painter)`
- 内容：`TextOnly`、`IconOnly`、`IconWithText`（图标可位于前/后）。
- 类型：`Filled`、`Outlined`、`NoBorder`。
- 颜色风格：`Primary`、`Secondary`、`Danger`，覆盖默认/悬浮/按下/禁用四态色。
- 尺寸：`XS(24)`、`S(32)`、`M(40)`、`L(48)`，带最小宽度/水平内边距/字体与图标尺寸。

示例：

```kotlin
FilledButton(text = "发送") { }
OutlinedButton(text = "取消") { }
Button(
  buttonContent = ButtonContent.IconWithText("添加", painterResource(R.drawable.ic_add)),
  type = ButtonType.Filled,
  colorType = ButtonColorType.Primary,
  size = ButtonSize.M
) { }
```

## FullScreenDialog

- 用途：全屏弹窗容器（Compose `Dialog`）。
- 行为：带显示动画与背景暗化（动态 0.32f），自动同步系统栏外观，可透传 `DialogProperties`。

示例：

```kotlin
var show by remember { mutableStateOf(true) }
if (show) {
  FullScreenDialog(onDismissRequest = { show = false }) {
    Box(Modifier.fillMaxSize())
  }
}
```

## Label

- 标题：`TitleLabel(text, size)`（Small/Medium/Large）。
- 副标题：`SubTitleLabel(text, size, icon, iconPosition)`。
- 列表项：`ItemLabel(text, size, icon, iconPosition)`。
- 危险态：`DangerLabel(text, size, icon, iconPosition)`。
- 自定义：`CustomLabel(text, font, textColor, backgroundColor, lineLimit, icon, iconPosition)`。

示例：

```kotlin
TitleLabel("标题")
SubTitleLabel("副标题", icon = R.drawable.ic_info)
ItemLabel("列表项")
DangerLabel("删除")
CustomLabel(
  text = "自定义",
  font = LocalTheme.current.fonts.caption1Bold,
  textColor = LocalTheme.current.colors.textColorPrimary
)
```

## Slider

- 入口：`Slider(value, orientation, enabled, valueRange, showTooltip, onValueChange)`。
- 方向：`Horizontal` | `Vertical`。
- 取值：默认范围 `0f..100f`，根据轨迹长度与拇指半径计算进度，点击/拖动更新值。
- 说明：`showTooltip` 预留；启用时会在拖动或悬停时显示位置，当前实现不渲染具体内容。

示例：

```kotlin
var value by remember { mutableStateOf(50f) }
Slider(value = value, onValueChange = { value = it })
Slider(value = value, orientation = SliderOrientation.Vertical, onValueChange = { value = it })
```

## Switch

- 入口：`Switch(checked, onCheckedChange, enabled, loading, size, type)`。
- 类型：`Basic`、`WithText`、`WithIcon`。
- 尺寸：`S(26×16)`、`M(32×20)`、`L(40×24)`；拇指带阴影动画位移。
- 行为：`loading` 时在拇指中显示圆形进度；禁用态降低透明度。

示例：

```kotlin
var checked by remember { mutableStateOf(false) }
Switch(checked = checked, onCheckedChange = { checked = it }, type = SwitchType.WithText)
```

## Toast

- 入口：`Toast`（对象）与 `Context` 扩展方法。
- 类型：`Text`、`Info`、`Help`、`Loading`、`Success`、`Warning`、`Error`。
- 方法：`info/help/success/warning/error/loading/simple(context, message)`；`hide()` 主动隐藏。
- 行为：同一时间仅显示一个，默认时长 3000ms，加载态同样遵循默认时长；通过 `Dialog+ComposeView` 覆盖显示，非可交互。

示例：

```kotlin
context.toastSuccess("发送成功")
context.toastError("发送失败")
context.toastLoading("加载中")
context.hideToast()
```

### 设计变量（主题能力）

- 颜色：`ColorScheme` 提供面向文本、背景、边框、阴影、列表、按钮、下拉、滚动条、浮层、复选框、Toast、Tag、Switch、Slider、Tab 的完整色值集合。
- 字体：`FontScheme` 预设标题/正文/说明等不同字号与字重组合，按用途分 `Bold/Medium/Regular` 与 `title/body/caption` 序列。
- 圆角：`RadiusScheme` 提供常用控件圆角尺寸（含超大圆角与完全圆角）。
- 间距：`SpacingScheme` 提供图标/文本/气泡/内容/标题/卡片等标准间距。

### 资源与依赖

- 资源：部分控件依赖 `R.string` 与 `R.drawable` 字符串与图标资源（如取消按钮文案、头像默认图、Toast 图标等）。
- 依赖：使用 Compose Material3、Coil（`AsyncImage`）、Gson、MMKV 等库能力。

### 注意事项

- 组件颜色等外观请统一从 `LocalTheme.current.colors` 读取，保证主题切换一致性。
- 自定义主色需满足格式校验（`#RRGGBB`），主题切换过程会清理缓存并持久化配置。
- `ContextProvider` 通过 `ContentProvider` 自动初始化，一般无需手动注册或调用。
- `appConfig.json` 缺省时采用默认配置；异常格式将忽略对应项并输出警告日志。

### 最佳实践

```kotlin
@Composable
fun ThemedText() {
  val theme = LocalTheme.current
  Text(
    text = "文本",
    color = theme.colors.textColorPrimary
  )
}

@Composable
fun SpacedColumn(content: @Composable () -> Unit) {
  val spacing = LocalTheme.current.spacings
  Column(verticalArrangement = Arrangement.spacedBy(spacing.normalSpacing)) {
    content()
  }
}

@Composable
fun FeatureEntry() {
  if (!AppBuilderConfig.hideSearch) {
    // 显示搜索入口
  }
}
```

### 常见问题

- 切换主题模式：

```kotlin
LocalTheme.current.setThemeMode(ThemeMode.DARK)
```

- 设置/清除主色：

```kotlin
LocalTheme.current.setPrimaryColor("#FF6A4C")
LocalTheme.current.clearPrimaryColor()
```

- 配置文件位置：`assets/appConfig.json`


