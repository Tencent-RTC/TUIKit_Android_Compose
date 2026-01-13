package io.trtc.tuikit.atomicx.messagelist.ui.messagerenderers

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicx.messagelist.ui.LocalInteractionHandler
import io.trtc.tuikit.atomicx.messagelist.ui.MessageRenderer
import io.trtc.tuikit.atomicx.messagelist.ui.widgets.MessageReadReceiptIndicator
import io.trtc.tuikit.atomicx.messagelist.utils.FileUtils
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class FileMessageRenderer : MessageRenderer {
    @Composable
    override fun Render(message: MessageInfo) {
        val context = LocalContext.current
        val colors = LocalTheme.current.colors
        val messageInteraction = LocalInteractionHandler.current

        val statusString = remember(message.messageBody?.filePath, message.progress) {
            when {
                message.messageBody?.filePath.isNullOrEmpty() -> {
                    if (message.progress != 100 && message.progress != 0) {
                        "${message.progress}%"
                    } else {
                        context.getString(R.string.message_list_not_download)
                    }
                }

                else -> ""
            }
        }

        Box(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
            ConstraintLayout(
                modifier = Modifier
                    .padding(12.dp, end = 12.dp)
                    .pointerInput(message.messageBody?.filePath) {
                        detectTapGestures(
                            onTap = {
                                messageInteraction.onTap()
                            },
                            onLongPress = {
                                messageInteraction.onLongPress()
                            }
                        )
                    }
                    .widthIn(min = 180.dp, max = 280.dp),
            ) {
                val (fileRow, fileSize, fileStatus) = createRefs()

                Row(
                    modifier = Modifier
                        .constrainAs(fileRow) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .background(color = colors.bgColorOperate, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(22.dp, 22.dp),
                        painter = painterResource(id = R.drawable.message_list_file_type_icon_none),
                        contentDescription = "",
                        tint = Color.Unspecified
                    )

                    Text(
                        maxLines = 1,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500,
                        overflow = TextOverflow.Ellipsis,
                        text = message.messageBody?.fileName ?: "",
                        color = colors.textColorPrimary
                    )
                }

                Text(
                    modifier = Modifier.constrainAs(fileSize) {
                        top.linkTo(fileRow.bottom, margin = 12.dp)
                        start.linkTo(parent.start)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    text = FileUtils.formatFileSize(message.messageBody?.fileSize?.toLong()),
                    color = if (message.isSelf) colors.textColorAntiSecondary else colors.textColorSecondary
                )

                Row(modifier = Modifier.constrainAs(fileStatus) {
                    top.linkTo(fileRow.bottom, margin = 12.dp)
                    end.linkTo(parent.end)
                }) {
                    if (statusString.isNotEmpty()) {
                        Text(
                            modifier = Modifier,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W400,
                            text = statusString,
                            color = if (message.isSelf) colors.textColorAntiSecondary else colors.textColorSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    MessageReadReceiptIndicator(
                        message = message,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}