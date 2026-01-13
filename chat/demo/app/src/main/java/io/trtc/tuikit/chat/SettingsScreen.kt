package io.trtc.tuikit.chat

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tencent.mmkv.MMKV
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionItem
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.ActionSheet
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Avatar
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.AvatarSize
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.FullScreenDialog
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Switch
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.basecomponent.theme.ThemeMode
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.login.AllowType
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.atomicxcore.api.login.UserProfile
import io.trtc.tuikit.chat.login.LoginActivity
import io.trtc.tuikit.chat.viewmodels.SettingsViewModel
import io.trtc.tuikit.chat.viewmodels.TranslateLanguageOption
import io.trtc.tuikit.chat.viewmodels.displayName

@Composable
fun SettingsScreen() {
    val colors = LocalTheme.current.colors
    val activity = LocalActivity.current
    var showSelfDetailDialog by remember { mutableStateOf(false) }
    val settingsViewModel: SettingsViewModel = viewModel()
    val userInfo by settingsViewModel.loginUserInfo.collectAsState()
    val enableReadReceipt by settingsViewModel.enableReadReceipt.collectAsState()
    val translateTargetLanguage by settingsViewModel.translateTargetLanguage.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.bgColorOperate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable(indication = null, interactionSource = null) {
                    showSelfDetailDialog = true
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                url = userInfo?.avatarURL,
                name = userInfo?.displayName ?: "",
                size = AvatarSize.XL,
                onClick = {
                    showSelfDetailDialog = true
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userInfo?.displayName ?: "",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.24).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.textColorPrimary
                )

                Text(
                    text = "ID：${userInfo?.userID}",
                    fontSize = 12.sp,
                    color = colors.textColorSecondary
                )

                Text(
                    text = "${stringResource(R.string.compose_demo_self_detail_status)}：${userInfo?.selfSignature}",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.textColorSecondary
                )

            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        var showFriendAddOpt by remember { mutableStateOf(false) }
        var showThemeSelector by remember { mutableStateOf(false) }
        var showLanguageSelector by remember { mutableStateOf(false) }
        var showTranslateLanguageSelector by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding()
        ) {

            SettingsItem(
                title = stringResource(R.string.compose_demo_theme),
                value = getThemeString(LocalTheme.current.currentMode),
                showDivider = true,
                onClick = {
                    showThemeSelector = true
                }
            )

            SettingsItem(
                title = stringResource(R.string.compose_demo_language),
                value = stringResource(R.string.compose_demo_current_language),
                showDivider = false,
                onClick = {
                    showLanguageSelector = true
                }
            )
        }

        Column(
            modifier = Modifier.padding()
        ) {
            SettingsItem(
                title = stringResource(R.string.compose_demo_add_rule),
                value = getFriendAddOptString(userInfo?.allowType),
                showDivider = true,
                onClick = {
                    showFriendAddOpt = true
                }
            )

            ReadReceiptToggleItem(
                enabled = enableReadReceipt,
                onToggle = { newValue ->
                    settingsViewModel.updateReadReceiptEnabled(newValue)
                }
            )

            SettingsItem(
                title = stringResource(R.string.compose_demo_translate_target_language),
                value = settingsViewModel.getTranslateLanguageDisplayName(translateTargetLanguage),
                showDivider = false,
                onClick = {
                    showTranslateLanguageSelector = true
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    LoginStore.shared.logout(object : CompletionHandler {
                        override fun onSuccess() {
                            MMKV.defaultMMKV().encode("LoginUser", "")
                            activity?.startActivity(Intent(activity, LoginActivity::class.java))
                            activity?.finish()
                        }

                        override fun onFailure(code: Int, desc: String) {
                        }
                    })
                }
                .background(color = colors.bgColorInput)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.compose_demo_logout),
                fontSize = 16.sp,
                color = colors.textColorError
            )
        }

        ActionSheet(
            showFriendAddOpt, options = listOf(
                ActionItem(
                    text = stringResource(R.string.compose_demo_allow_type_allow_any),
                    value = AllowType.ALLOW_ANY
                ),
                ActionItem(
                    text = stringResource(R.string.compose_demo_allow_type_deny_any),
                    value = AllowType.DENY_ANY
                ),
                ActionItem(
                    text = stringResource(R.string.compose_demo_allow_type_need_confirm),
                    value = AllowType.NEED_CONFIRM
                ),
            ), onDismiss = { showFriendAddOpt = false }) {
            val userProfile = UserProfile().apply {
                allowType = it.value as? AllowType ?: AllowType.NEED_CONFIRM
            }
            LoginStore.shared.setSelfInfo(userProfile, object : CompletionHandler {
                override fun onSuccess() {

                }

                override fun onFailure(code: Int, desc: String) {
                }
            })
        }

        val themeState = LocalTheme.current
        ActionSheet(
            showThemeSelector, options = listOf(
                ActionItem(
                    text = stringResource(R.string.compose_demo_theme_custom),
                    value = "#Custom"
                ),
                ActionItem(text = getThemeString(ThemeMode.SYSTEM), value = ThemeMode.SYSTEM),
                ActionItem(text = getThemeString(ThemeMode.LIGHT), value = ThemeMode.LIGHT),
                ActionItem(text = getThemeString(ThemeMode.DARK), value = ThemeMode.DARK),
            ), onDismiss = { showThemeSelector = false }) {
            if (it.value == "#Custom") {
                val color = (0..0xFFFFFF).random()
                themeState.setPrimaryColor("#${color.toString(16).padStart(6, '0')}")
            } else {
                themeState.setThemeMode(it.value as ThemeMode)
            }
        }

        ActionSheet(
            showLanguageSelector, options = listOf(
                ActionItem(text = stringResource(R.string.compose_demo_zh_hans), value = "zh"),
                ActionItem(text = stringResource(R.string.compose_demo_zh_hant), value = "zh-hk"),
                ActionItem(text = stringResource(R.string.compose_demo_en), value = "en"),
                ActionItem(text = stringResource(R.string.compose_demo_ar), value = "ar"),
            ), onDismiss = { showLanguageSelector = false }) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(it.value.toString()))
            val viewModelStore = (activity as AppCompatActivity).viewModelStore
            viewModelStore.clear()
            activity.recreate()
        }

        ActionSheet(
            showTranslateLanguageSelector,
            options = settingsViewModel.translateLanguageOptions.map { option ->
                ActionItem(text = option.name, value = option.code)
            },
            onDismiss = { showTranslateLanguageSelector = false }
        ) {
            settingsViewModel.updateTranslateTargetLanguage(it.value.toString())
        }
    }

    if (showSelfDetailDialog) {
        FullScreenDialog(onDismissRequest = { showSelfDetailDialog = false }) {
            SelfDetailScreen(onDismiss = { showSelfDetailDialog = false })
        }

    }
}

@Composable
fun SettingsItem(
    title: String,
    value: String,
    showDivider: Boolean,
    showArrow: Boolean = true,
    onClick: () -> Unit = {}
) {
    val colors = LocalTheme.current.colors
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                maxLines = 1,
                color = colors.textColorSecondary,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp)
                    .weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                if (value.isNotEmpty()) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = value,
                        fontSize = 16.sp,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        overflow = TextOverflow.Ellipsis,
                        color = colors.textColorPrimary
                    )
                }
            }
            if (showArrow) {
                Icon(
                    painter = painterResource(id = R.drawable.app_navigation_right_icon),
                    contentDescription = "Arrow",
                    tint = colors.textColorTertiary,
                    modifier = Modifier
                        .width(7.dp)
                        .height(12.dp)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = colors.strokeColorSecondary,
                thickness = 1.dp
            )
        }
    }
}

@Composable
fun getFriendAddOptString(allowType: AllowType?): String {
    return when (allowType) {
        AllowType.ALLOW_ANY -> stringResource(R.string.compose_demo_allow_type_allow_any)
        AllowType.DENY_ANY -> stringResource(R.string.compose_demo_allow_type_deny_any)
        AllowType.NEED_CONFIRM -> stringResource(R.string.compose_demo_allow_type_need_confirm)
        else -> stringResource(R.string.compose_demo_allow_type_need_confirm)
    }
}

@Composable
fun getThemeString(themeScheme: ThemeMode): String {
    return when (themeScheme) {
        ThemeMode.SYSTEM -> stringResource(R.string.compose_demo_theme_system)
        ThemeMode.DARK -> stringResource(R.string.compose_demo_theme_dark)
        ThemeMode.LIGHT -> stringResource(R.string.compose_demo_theme_light)
    }
}

@Composable
fun ReadReceiptToggleItem(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val colors = LocalTheme.current.colors
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.compose_demo_message_read_receipt),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    maxLines = 1,
                    color = colors.textColorSecondary,
                    modifier = Modifier.weight(1f)
                )

                Switch(checked = enabled, onCheckedChange = onToggle)

            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getReadReceiptDescription(enabled),
                fontSize = 12.sp,
                color = colors.textColorSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun getReadReceiptDescription(enabled: Boolean): String {
    return if (enabled) {
        stringResource(R.string.compose_demo_message_read_receipt_enabled_desc)
    } else {
        stringResource(R.string.compose_demo_message_read_receipt_disabled_desc)
    }
}
