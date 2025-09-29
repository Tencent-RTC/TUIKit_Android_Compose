package io.trtc.tuikit.atomicx.imageviewer.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import coil3.compose.AsyncImage
import io.trtc.tuikit.atomicx.imageviewer.utils.ImageUtils
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    data: Any,
    onTap: () -> Unit = {}
) {

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isAnimating by remember { mutableStateOf(false) }


    var imageSize by remember { mutableStateOf(Size.Zero) }


    val scaleAnimatable = remember { Animatable(1f) }
    val offsetXAnimatable = remember { Animatable(0f) }
    val offsetYAnimatable = remember { Animatable(0f) }


    val coroutineScope = rememberCoroutineScope()


    var lastFocalPoint by remember { mutableStateOf(Offset.Zero) }
    var touchStartPoint by remember { mutableStateOf(Offset.Zero) }
    var hasStartedPanning by remember { mutableStateOf(false) }
    var isPanningHorizontally by remember { mutableStateOf(false) }
    var isZoomed by remember { mutableStateOf(false) }


    LaunchedEffect(scale) {
        isZoomed = scale > 1.05f
    }


    val minScale = 1f
    val midScale = 1.75f
    val maxScale = 3.5f


    val animationDuration = 200


    val dragSensitivity = 3.0f
    val horizontalThreshold = 20f


    DisposableEffect(Unit) {
        onDispose {
            scale = 1f
            offset = Offset.Zero
            isAnimating = false
        }
    }


    fun resetGestureState() {
        hasStartedPanning = false
        isPanningHorizontally = false
        touchStartPoint = Offset.Zero
        lastFocalPoint = Offset.Zero
    }


    fun animateToScale(targetScale: Float, focalPoint: Offset? = null) {

        if (imageSize.width <= 0 || imageSize.height <= 0) return

        val currentScale = scale


        val newOffset = if (targetScale == minScale) {

            Offset.Zero
        } else if (focalPoint != null) {

            val centeredPointX = focalPoint.x - imageSize.width / 2
            val centeredPointY = focalPoint.y - imageSize.height / 2
            val scaleFactor = targetScale / currentScale

            if (targetScale > currentScale) {

                val newOffsetX = offset.x + centeredPointX * (1 - scaleFactor)
                val newOffsetY = offset.y + centeredPointY * (1 - scaleFactor)


                val scaledImageWidth = imageSize.width * targetScale
                val scaledImageHeight = imageSize.height * targetScale

                val horizontalSpace = (scaledImageWidth - imageSize.width).coerceAtLeast(0f) / 2
                val verticalSpace = (scaledImageHeight - imageSize.height).coerceAtLeast(0f) / 2

                Offset(
                    x = newOffsetX.coerceIn(-horizontalSpace, horizontalSpace),
                    y = newOffsetY.coerceIn(-verticalSpace, verticalSpace)
                )
            } else {

                offset
            }
        } else {
            offset
        }

        coroutineScope.launch {
            isAnimating = true

            try {

                scaleAnimatable.snapTo(scale)
                offsetXAnimatable.snapTo(offset.x)
                offsetYAnimatable.snapTo(offset.y)


                launch {
                    scaleAnimatable.animateTo(
                        targetValue = targetScale,
                        animationSpec = tween(durationMillis = animationDuration)
                    ) {
                        scale = this.value
                    }
                }


                launch {
                    offsetXAnimatable.animateTo(
                        targetValue = newOffset.x,
                        animationSpec = tween(durationMillis = animationDuration)
                    ) {
                        offset = offset.copy(x = this.value)
                    }
                }


                launch {
                    offsetYAnimatable.animateTo(
                        targetValue = newOffset.y,
                        animationSpec = tween(durationMillis = animationDuration)
                    ) {
                        offset = offset.copy(y = this.value)
                    }
                }
            } finally {

                isAnimating = false
                resetGestureState()
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = data,
            contentDescription = "",
            contentScale = ContentScale.Fit,
            imageLoader = ImageUtils.getImageLoader(),
            modifier = Modifier
                .fillMaxSize()

                .onGloballyPositioned { coordinates ->
                    imageSize = coordinates.size.toSize()
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )

                .pointerInput(Unit) {

                    detectTapGestures(
                        onTap = {
                            if (!isAnimating) {
                                onTap()
                            }
                            resetGestureState()
                        },
                        onDoubleTap = { tapPosition ->

                            val nextScale = when {
                                scale < midScale -> midScale
                                scale >= midScale && scale < maxScale -> maxScale
                                else -> minScale
                            }


                            animateToScale(nextScale, tapPosition)
                        }
                    )
                }

                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        var zoom = 1f
                        var pan = Offset.Zero
                        var pastTouchSlop = false

                        while (true) {
                            val event = awaitPointerEvent()


                            if (event.type == PointerEventType.Press) {
                                resetGestureState()
                                touchStartPoint = event.changes.first().position
                                lastFocalPoint = touchStartPoint
                                pastTouchSlop = false
                            } else if (event.type == PointerEventType.Move) {
                                if (isAnimating) continue

                                val changes = event.changes


                                if (changes.size == 1) {
                                    val change = changes.first()
                                    val position = change.position


                                    if (!hasStartedPanning) {
                                        val dx = position.x - touchStartPoint.x
                                        val dy = position.y - touchStartPoint.y


                                        if (abs(dx) > 5f || abs(dy) > 5f) {
                                            hasStartedPanning = true
                                            isPanningHorizontally = abs(dx) > abs(dy)
                                        }
                                    }


                                    if (isPanningHorizontally && !isZoomed) {
                                        continue
                                    }


                                    val dragX = (position.x - lastFocalPoint.x) * dragSensitivity
                                    val dragY = (position.y - lastFocalPoint.y) * dragSensitivity


                                    val scaledImageWidth = imageSize.width * scale
                                    val scaledImageHeight = imageSize.height * scale
                                    val horizontalSpace = (scaledImageWidth - imageSize.width).coerceAtLeast(0f) / 2
                                    val verticalSpace = (scaledImageHeight - imageSize.height).coerceAtLeast(0f) / 2


                                    offset = Offset(
                                        x = (offset.x + dragX).coerceIn(-horizontalSpace, horizontalSpace),
                                        y = (offset.y + dragY).coerceIn(-verticalSpace, verticalSpace)
                                    )


                                    lastFocalPoint = position


                                    change.consume()
                                } else if (changes.size == 2) {
                                    val zoomChange = calculateZoom(changes)
                                    val centroid = calculateCentroid(changes)


                                    val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)


                                    val scaledImageWidth = imageSize.width * newScale
                                    val scaledImageHeight = imageSize.height * newScale
                                    val horizontalSpace = (scaledImageWidth - imageSize.width).coerceAtLeast(0f) / 2
                                    val verticalSpace = (scaledImageHeight - imageSize.height).coerceAtLeast(0f) / 2


                                    scale = newScale


                                    lastFocalPoint = centroid


                                    changes.forEach { it.consume() }
                                }
                            } else if (event.type == PointerEventType.Release || event.type == PointerEventType.Exit) {
                                resetGestureState()
                            }
                        }
                    }
                }
        )
    }
}


private fun calculateZoom(changes: List<PointerInputChange>): Float {
    require(changes.size == 2)
    val newDistance = (changes[0].position - changes[1].position).getDistance()
    val oldDistance = (changes[0].previousPosition - changes[1].previousPosition).getDistance()
    return if (oldDistance != 0f) newDistance / oldDistance else 1f
}


private fun calculateCentroid(changes: List<PointerInputChange>): Offset {
    require(changes.size == 2)
    return (changes[0].position + changes[1].position) / 2f
}


private fun Offset.getDistance(): Float {
    return sqrt(x * x + y * y)
} 