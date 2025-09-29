package io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.basecomponent.theme.Colors
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun IndexBar(
    letters: List<String>,
    modifier: Modifier = Modifier,
    currentLetter: String? = null,
    onLetterSelected: (String) -> Unit,
    onLetterPressed: (String) -> Unit = {},
    onLetterReleased: () -> Unit = {},
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
) {
    val colors = LocalTheme.current.colors
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isDragging by remember { mutableStateOf(false) }
    var draggedLetter by remember { mutableStateOf<String?>(null) }
    var columnHeight by remember { mutableStateOf(0f) }
    var columnTop by remember { mutableStateOf(0f) }

    fun getLetterFromPosition(absoluteY: Float): String? {
        if (columnHeight <= 0f || letters.isEmpty()) return null

        val relativeY = absoluteY - columnTop
        val progress = (relativeY / columnHeight).coerceIn(0f, 1f)
        val letterIndex = (progress * letters.size).roundToInt()
            .coerceIn(0, letters.lastIndex)
        return letters[letterIndex]
    }

    Column(
        modifier = modifier
            .wrapContentHeight()
            .width(24.dp)
            .padding(vertical = 8.dp)
            .onGloballyPositioned { coordinates ->
                columnHeight = coordinates.size.height.toFloat()
                columnTop = coordinates.localToWindow(Offset.Zero).y
            }
            .pointerInput(letters) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        onDragStart()
                        val letter = getLetterFromPosition(offset.y + columnTop)
                        letter?.let {
                            draggedLetter = it
                            onLetterSelected(it)
                            onLetterPressed(it)
                        }
                    },
                    onDrag = { change, _ ->
                        val absoluteY = change.position.y + columnTop
                        val letter = getLetterFromPosition(absoluteY)
                        letter?.let {
                            if (draggedLetter != it) {
                                draggedLetter = it
                                onLetterSelected(it)
                                onLetterPressed(it)
                            }
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        draggedLetter = null
                        onDragEnd()
                        onLetterReleased()
                    }
                )
            },
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        letters.forEach { letter ->
            val isDraggedLetter = isDragging && draggedLetter == letter
            val isCurrent = currentLetter == letter
            val isHighlighted = isDraggedLetter || isCurrent

            Box(
                modifier = Modifier
                    .size(if (isHighlighted) 20.dp else 16.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            isDraggedLetter -> colors.textColorLink
                            isCurrent -> colors.textColorLink
                            else -> Colors.Transparent
                        }
                    )
                    .clickable {
                        onLetterSelected(letter)
                        onLetterPressed(letter)
                        coroutineScope.launch {
                            delay(300)
                            onLetterReleased()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W400,
                    color = when {
                        isDraggedLetter -> colors.textColorButton
                        isCurrent -> colors.textColorButton
                        else -> colors.textColorLink
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
} 