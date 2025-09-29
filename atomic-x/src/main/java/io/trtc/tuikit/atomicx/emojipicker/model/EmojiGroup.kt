package io.trtc.tuikit.atomicx.emojipicker.model

data class EmojiGroup(
    val id: String,
    val name: String,
    val desc: String = "",
    val emojiGroupIconUrl: Any,
    var emojis: List<Emoji>,
    val isLittleEmoji: Boolean = false,
)

open class Emoji(
    val key: String,
    val emojiName: String,
    var emojiUrl: Any = "",
)

