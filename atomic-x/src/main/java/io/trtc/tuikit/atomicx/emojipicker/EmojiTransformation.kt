package io.trtc.tuikit.atomicx.emojipicker

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun rememberEmojiText(
    text: String,
    emojiSize: TextUnit = 28.sp
): Pair<AnnotatedString, Map<String, InlineTextContent>> {
    val context = LocalContext.current
    EmojiManager.initialize(context)
    val (annotatedString, inlineContent) = remember(text, emojiSize) {
        createOptimizedEmojiText(
            text = text,
            emojiSize = emojiSize
        )
    }
    return Pair(annotatedString, inlineContent)
}

private fun createOptimizedEmojiText(
    text: String,
    emojiSize: TextUnit = 28.sp
): Pair<AnnotatedString, Map<String, InlineTextContent>> {
    if (text.isEmpty()) {
        return Pair(AnnotatedString(text), emptyMap())
    }

    val emojiKeys = EmojiManager.littleEmojiKeyList

    return buildOptimizedEmojiTextData(
        text = text,
        emojiKeys = emojiKeys,
        emojiSize = emojiSize
    )
}

private fun buildOptimizedEmojiTextData(
    text: String,
    emojiKeys: List<String>,
    emojiSize: TextUnit
): Pair<AnnotatedString, Map<String, InlineTextContent>> {
    val inlineContentMap = mutableMapOf<String, InlineTextContent>()

    val emojiMatches = findAllEmojiMatches(text, emojiKeys)

    if (emojiMatches.isEmpty()) {
        return Pair(AnnotatedString(text), emptyMap())
    }

    val annotatedString = buildAnnotatedString {
        var currentPos = 0

        emojiMatches.forEach { (startIndex, emojiKey) ->
            if (currentPos < startIndex) {
                append(text.substring(currentPos, startIndex))
            }

            val emojiId = generateEmojiId(emojiKey)
            appendInlineContent(emojiId, emojiKey)

            if (!inlineContentMap.containsKey(emojiId)) {
                val emoji = EmojiManager.findEmojiByKey(emojiKey)
                if (emoji != null) {
                    inlineContentMap[emojiId] = createOptimizedInlineContent(emoji, emojiSize)
                }
            }

            currentPos = startIndex + emojiKey.length
        }

        if (currentPos < text.length) {
            append(text.substring(currentPos))
        }
    }

    return Pair(annotatedString, inlineContentMap)
}

private fun findAllEmojiMatches(text: String, emojiKeys: List<String>): List<Pair<Int, String>> {
    val matches = mutableListOf<Pair<Int, String>>()

    val sortedKeys = emojiKeys.sortedByDescending { it.length }

    var i = 0
    while (i < text.length) {
        var matched = false

        for (emojiKey in sortedKeys) {
            if (i + emojiKey.length <= text.length &&
                text.substring(i, i + emojiKey.length) == emojiKey
            ) {
                matches.add(i to emojiKey)
                i += emojiKey.length
                matched = true
                break
            }
        }

        if (!matched) {
            i++
        }
    }

    return matches.sortedBy { it.first }
}

private fun generateEmojiId(emojiKey: String): String {
    return "emoji_${emojiKey.hashCode()}"
}

private fun createOptimizedInlineContent(
    emoji: io.trtc.tuikit.atomicx.emojipicker.model.Emoji,
    emojiSize: TextUnit
): InlineTextContent {
    return InlineTextContent(
        placeholder = Placeholder(
            width = emojiSize,
            height = emojiSize,
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
        )
    ) {
        OptimizedEmojiImage(
            emoji = emoji,
            size = emojiSize.value.dp
        )
    }
}

@Composable
private fun OptimizedEmojiImage(
    emoji: io.trtc.tuikit.atomicx.emojipicker.model.Emoji,
    size: androidx.compose.ui.unit.Dp
) {
    val context = LocalContext.current

    val cachedDrawable = remember(emoji.key) {
        EmojiManager.getCachedEmojiDrawable(emoji.key)
    }

    if (cachedDrawable != null) {
        val painter = remember(cachedDrawable) {
            try {
                when (cachedDrawable) {
                    is android.graphics.drawable.BitmapDrawable -> {
                        val bitmap = cachedDrawable.bitmap
                        if (bitmap != null && !bitmap.isRecycled) {
                            BitmapPainter(bitmap.asImageBitmap())
                        } else {
                            null
                        }
                    }

                    else -> {
                        val bitmap = createBitmap(cachedDrawable.intrinsicWidth.takeIf { it > 0 } ?: 64,
                            cachedDrawable.intrinsicHeight.takeIf { it > 0 } ?: 64)
                        val canvas = android.graphics.Canvas(bitmap)
                        cachedDrawable.setBounds(0, 0, canvas.width, canvas.height)
                        cachedDrawable.draw(canvas)
                        BitmapPainter(bitmap.asImageBitmap())
                    }
                }
            } catch (e: Exception) {
                null
            }
        }

        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = emoji.emojiName,
                modifier = Modifier.size(size)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(emoji.emojiUrl)
                    .crossfade(false)
                    .memoryCacheKey(emoji.key)
                    .diskCacheKey(emoji.key)
                    .build(),
                contentDescription = emoji.emojiName,
                modifier = Modifier.size(size)
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(emoji.emojiUrl)
                .crossfade(false)
                .memoryCacheKey(emoji.key)
                .diskCacheKey(emoji.key)
                .listener(
                    onSuccess = { _, _ ->
                    }
                )
                .build(),
            contentDescription = emoji.emojiName,
            modifier = Modifier.size(size)
        )
    }
}


@Composable
fun rememberEmojiKeyToName(text: String): String {
    val context = LocalContext.current
    EmojiManager.initialize(context)
    return remember(text) {
        if (text.isEmpty()) return@remember text
        replaceEmojiKeysWithNames(text)
    }
}

private fun replaceEmojiKeysWithNames(text: String): String {
    if (text.isEmpty()) return text

    val emojiList = EmojiManager.littleEmojiList
    var result = text

    val sortedEmojis = emojiList.sortedByDescending { it.key.length }

    sortedEmojis.forEach { emoji ->
        if (result.contains(emoji.key)) {
            result = result.replace(emoji.key, emoji.emojiName)
        }
    }

    return result
}
