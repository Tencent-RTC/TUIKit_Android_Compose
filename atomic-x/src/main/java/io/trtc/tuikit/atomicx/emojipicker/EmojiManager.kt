package io.trtc.tuikit.atomicx.emojipicker

import android.content.Context
import android.graphics.drawable.Drawable
import coil3.Image
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.target.Target
import io.trtc.tuikit.atomicx.R
import io.trtc.tuikit.atomicx.emojipicker.model.Emoji
import io.trtc.tuikit.atomicx.emojipicker.model.EmojiGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object EmojiManager {

    private var _isInitialized = false
    private val _littleEmojiList = mutableListOf<Emoji>()
    private val _emojiGroupList = mutableListOf<EmojiGroup>()

    private val _emojiImageCache = mutableMapOf<String, Drawable>()
    private var _isPreloadingImages = false
    private val preloadScope = CoroutineScope(Dispatchers.Main)

    val littleEmojiList: List<Emoji>
        get() = _littleEmojiList.toList()

    val littleEmojiKeyList: List<String>
        get() = _littleEmojiList.map { it.key }

    val emojiGroupList: List<EmojiGroup>
        get() = _emojiGroupList.toList()


    fun initialize(context: Context) {
        synchronized(this) {
            if (_isInitialized) {
                return
            }
            _isInitialized = true
        }

        try {
            val emojiKeys: Array<String> = context.resources
                .getStringArray(R.array.buildin_emoji_key)
            val emojiNames: Array<String> = context.resources
                .getStringArray(R.array.buildin_emoji_name)
            val emojiPath: Array<String> = context.resources
                .getStringArray(R.array.buildin_emoji_file_name)

            val emojis: MutableList<Emoji> = mutableListOf()
            for (i in emojiKeys.indices) {
                val emojiKey = emojiKeys[i]
                val emojiName = emojiNames[i]
                val emojiUrl = "file:///android_asset/buildinemojis/" + emojiPath[i]
                val emoji = Emoji(emojiKey, emojiName, emojiUrl)
                emojis.add(emoji)
                _littleEmojiList.add(emoji)
            }

            val emojiGroup = EmojiGroup(
                id = "LittleEmoji",
                "LittleYellowFaceEmoji",
                emojiGroupIconUrl = emojis.firstOrNull()?.emojiUrl ?: "",
                emojis = emojis,
                isLittleEmoji = true
            )

            _emojiGroupList.clear()
            _emojiGroupList.addAll(listOf(emojiGroup))

            preloadEmojiImages(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun findEmojiByKey(key: String): Emoji? {
        return _littleEmojiList.find { it.key == key }
    }

    fun containsEmojiKey(text: String): Boolean {
        return _littleEmojiList.any { text.contains(it.key) }
    }

    fun getCachedEmojiDrawable(key: String): Drawable? {
        return _emojiImageCache[key]
    }

    private fun preloadEmojiImages(context: Context) {
        if (_isPreloadingImages) return
        _isPreloadingImages = true

        val imageLoader = EmojiImageLoader.getInstance(context)

        preloadScope.launch {
            try {
                _littleEmojiList.forEach { emoji ->
                    if (!_emojiImageCache.containsKey(emoji.key)) {
                        val request = ImageRequest.Builder(context)
                            .data(emoji.emojiUrl)
                            .target(object : Target {
                                override fun onSuccess(result: Image) {
                                    val drawable = result.asDrawable(context.resources)
                                    _emojiImageCache[emoji.key] = drawable
                                }

                                override fun onError(error: Image?) {
                                }
                            })
                            .build()

                        imageLoader.enqueue(request)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPreloadingImages = false
            }
        }
    }

    fun clearImageCache() {
        _emojiImageCache.clear()
    }

    fun getCacheSize(): Int {
        return _emojiImageCache.size
    }
} 