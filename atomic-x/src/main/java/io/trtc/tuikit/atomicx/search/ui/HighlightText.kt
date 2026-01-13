package io.trtc.tuikit.atomicx.search.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme

open class HighlightTextItem(
    val text: String,
    val keywords: String = "",
)

@Composable
fun HighlightTitle(
    text: String,
    keywords: String,
    modifier: Modifier = Modifier
) {

    HighlightedText(
        text = text,
        keywords = keywords,
        textColor = LocalTheme.current.colors.textColorPrimary,
        modifier = modifier,
    )
}

@Composable
fun HighlightSecondary(
    textList: List<HighlightTextItem>,
    modifier: Modifier = Modifier
) {
    HighlightedText(
        textList = textList,
        textColor = LocalTheme.current.colors.textColorSecondary,
        modifier = modifier,
        textSize = 12.sp,
        weight = FontWeight.W400
    )
}

@Composable
fun HighlightSecondary(
    text: String,
    keywords: String,
    modifier: Modifier = Modifier
) {
    HighlightedText(
        text = text,
        keywords = keywords,
        textColor = LocalTheme.current.colors.textColorSecondary,
        modifier = modifier,
        textSize = 12.sp,
        weight = FontWeight.W400
    )
}


@Composable
fun HighlightedText(
    textList: List<HighlightTextItem>,
    textColor: Color,
    modifier: Modifier = Modifier,
    textSize: TextUnit = 14.sp,
    maxLine: Int = 1,
    weight: FontWeight = FontWeight.W500,
) {
    val colors = LocalTheme.current.colors
    val normalStyle = SpanStyle(
        fontWeight = weight,
        fontSize = textSize,
        color = textColor
    )

    val annotatedString = buildAnnotatedString {
        textList.forEach { item ->
            val highlightStyle = normalStyle.copy(
                color = colors.textColorLink
            )

            if (item.keywords.isEmpty()) {
                withStyle(normalStyle) { append(item.text) }
            } else {
                val regex = Regex.escape(item.keywords).toRegex(RegexOption.IGNORE_CASE)
                val matches = regex.findAll(item.text)
                var lastIndex = 0

                matches.forEach { match ->
                    withStyle(normalStyle) {
                        append(item.text.substring(lastIndex, match.range.first))
                    }
                    withStyle(highlightStyle) {
                        append(item.text.substring(match.range))
                    }
                    lastIndex = match.range.last + 1
                }
                if (lastIndex < item.text.length) {
                    withStyle(normalStyle) {
                        append(item.text.substring(lastIndex))
                    }
                }
            }
        }
    }
    Text(
        text = annotatedString,
        fontSize = textSize,
        color = textColor,
        maxLines = maxLine,
        fontWeight = weight,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun HighlightedText(
    text: String,
    keywords: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    textSize: TextUnit = 14.sp,
    weight: FontWeight = FontWeight.W500,
) {
    HighlightedText(
        textList = listOf(HighlightTextItem(text, keywords)),
        textColor = textColor,
        modifier = modifier,
        textSize = textSize,
        weight = weight
    )
}
