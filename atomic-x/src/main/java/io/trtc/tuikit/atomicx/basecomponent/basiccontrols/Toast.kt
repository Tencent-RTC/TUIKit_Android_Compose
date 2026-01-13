package io.trtc.tuikit.atomicx.basecomponent.basiccontrols

import android.app.Activity
import android.app.Dialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class ToastType {
    Text,
    Info,
    Help,
    Loading,
    Success,
    Warning,
    Error
}

object Toast {
    const val DURATION = 3000L
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var currentDialog: Dialog? = null
    private var currentToast: android.widget.Toast? = null
    private var hideJob: Job? = null

    fun info(context: Context, message: String) {
        show(
            context = context,
            message = message,
            type = ToastType.Info,
        )
    }

    fun help(context: Context, message: String) {
        show(
            context = context,
            message = message,
            type = ToastType.Help,
        )
    }

    fun success(context: Context, message: String) {
        show(
            context = context,
            message = message,
            type = ToastType.Success,
        )
    }

    fun warning(context: Context, message: String) {
        show(
            context = context,
            message = message,
            type = ToastType.Warning,
        )
    }

    fun error(context: Context, message: String) {
        show(
            context = context,
            message = message,
            type = ToastType.Error,
        )
    }

    fun loading(context: Context, message: String) {
        show(
            context = context,
            message = message,
            type = ToastType.Loading,
        )
    }

    fun simple(context: Context, message: String) {
        show(
            context = context,
            message = message,
            type = ToastType.Info,
        )
    }

    private fun show(
        context: Context,
        message: String,
        type: ToastType,
        duration: Long = DURATION,
        onDismiss: (() -> Unit)? = null
    ) {
        coroutineScope.launch {
            showInternal(context, message, type, duration, onDismiss)
        }
    }

    private suspend fun showInternal(
        context: Context,
        message: String,
        type: ToastType,
        duration: Long,
        onDismiss: (() -> Unit)?
    ) {
        hideInternal()

        val activity = context as? Activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            showWithDialog(activity, message, type, duration, onDismiss)
        } else {
            showWithToast(context, message, duration, onDismiss)
        }
    }

    private fun showWithToast(
        context: Context,
        message: String,
        duration: Long,
        onDismiss: (() -> Unit)?
    ) {
        val toastDuration = if (duration > 2000) {
            android.widget.Toast.LENGTH_LONG
        } else {
            android.widget.Toast.LENGTH_SHORT
        }
        currentToast = android.widget.Toast.makeText(context, message, toastDuration).also {
            it.show()
        }

        if (duration > 0) {
            hideJob = coroutineScope.launch {
                delay(duration)
                hideInternal()
                onDismiss?.invoke()
            }
        }
    }

    private suspend fun showWithDialog(
        activity: Activity,
        message: String,
        type: ToastType,
        duration: Long,
        onDismiss: (() -> Unit)?
    ) {
        currentDialog = Dialog(activity).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            window?.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )

            setCancelable(false)
            setCanceledOnTouchOutside(false)

            window?.setDimAmount(0f)
            window?.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )

            val composeView = ComposeView(activity).apply {
                setViewTreeLifecycleOwner(activity as LifecycleOwner?)
                setViewTreeSavedStateRegistryOwner(activity as SavedStateRegistryOwner?)

                setContent {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        ToastContent(
                            message = message,
                            type = type,
                        )
                    }
                }
            }

            setContentView(composeView)
        }

        currentDialog?.show()

        if (duration > 0) {
            hideJob = coroutineScope.launch {
                delay(duration)
                hide()
                onDismiss?.invoke()
            }
        }
    }

    fun hide() {
        coroutineScope.launch {
            hideInternal()
        }
    }

    private fun hideInternal() {
        try {
            hideJob?.cancel()
            hideJob = null

            currentDialog?.dismiss()
            currentDialog = null

            currentToast?.cancel()
            currentToast = null
        } catch (_: Exception) {
        }
    }
}

@Composable
private fun ToastContent(
    message: String,
    type: ToastType,
) {

    val horizontalPadding = 16.dp
    val verticalPadding = 9.dp
    val fontSize = 14.sp
    val iconGap = 8.dp
    val colors = LocalTheme.current.colors
    Box(modifier = Modifier.padding(12.dp)) {

        Card(
            modifier = Modifier
                .widthIn(max = 340.dp),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = colors.bgColorOperate),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(),
                    horizontalArrangement = Arrangement.spacedBy(iconGap)
                ) {
                    val lineHeightDp = with(LocalDensity.current) {
                        (fontSize.value * 1.57).sp.toDp()
                    }
                    val iconTopPadding = (lineHeightDp - 16.dp) / 2
                    if (type != ToastType.Text) {
                        ToastIcon(
                            modifier = Modifier
                                .align(Alignment.Top)
                                .padding(top = iconTopPadding),
                            type = type,
                            size = 16.dp
                        )
                    }
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = message,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Medium,
                        color = colors.textColorPrimary,
                        lineHeight = (fontSize.value * 1.57).sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ToastIcon(
    modifier: Modifier = Modifier,
    type: ToastType,
    size: Dp
) {
    val icon = when (type) {
        ToastType.Info -> painterResource(R.drawable.base_component_toast_info_icon)
        ToastType.Help -> painterResource(R.drawable.base_component_toast_help_icon)
        ToastType.Success -> painterResource(R.drawable.base_component_toast_success_icon)
        ToastType.Warning -> painterResource(R.drawable.base_component_toast_warning_icon)
        ToastType.Error -> painterResource(R.drawable.base_component_toast_error_icon)
        else -> null
    }
    when {
        icon != null -> {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = modifier.size(size),
                tint = Color.Unspecified
            )
        }

        type == ToastType.Loading -> {
            CircularProgressIndicator(
                modifier = modifier.size(size),
                strokeWidth = 2.dp,
                color = Color(0xFF1C66E5)
            )
        }

        else -> {}
    }
}

fun Context.hideToast() {
    Toast.hide()
}

fun Context.toastInfo(message: String) = Toast.info(this, message)
fun Context.toastHelp(message: String) = Toast.help(this, message)
fun Context.toastSuccess(message: String) = Toast.success(this, message)
fun Context.toastWarning(message: String) = Toast.warning(this, message)
fun Context.toastError(message: String) = Toast.error(this, message)
fun Context.toastLoading(message: String) = Toast.loading(this, message)
fun Context.toastSimple(message: String) = Toast.simple(this, message)