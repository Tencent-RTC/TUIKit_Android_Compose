package io.trtc.tuikit.atomicx.emojipicker

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RecentEmojiManager {
    private const val MMKV_ID = "recent_emoji_cache"
    private const val KEY_RECENT_EMOJI = "recent_emoji_list"
    private const val MAX_RECENT_EMOJI_COUNT = 8

    private lateinit var mmkv: MMKV
    private val gson = Gson()

    private val _recentEmojis = MutableStateFlow<List<String>>(emptyList())
    val recentEmojis: StateFlow<List<String>> = _recentEmojis.asStateFlow()

    @JvmStatic
    fun initialize(context: Context) {
        MMKV.initialize(context)
        mmkv = MMKV.mmkvWithID(MMKV_ID)
        _recentEmojis.value = getRecentEmojiList()
    }

    @JvmStatic
    fun getRecentEmojiList(): List<String> {
        if (!::mmkv.isInitialized) {
            return emptyList()
        }

        val json = mmkv.getString(KEY_RECENT_EMOJI, null)
        return if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                val type = TypeToken.getParameterized(List::class.java, String::class.java).type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    @JvmStatic
    fun saveRecentEmojiList(emojiList: List<String>) {
        if (!::mmkv.isInitialized) {
            return
        }

        try {
            val json = gson.toJson(emojiList)
            mmkv.putString(KEY_RECENT_EMOJI, json)
            _recentEmojis.value = emojiList
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun updateRecentEmoji(emojiKey: String) {
        val recentList = getRecentEmojiList().toMutableList()

        recentList.remove(emojiKey)

        recentList.add(0, emojiKey)

        if (recentList.size > MAX_RECENT_EMOJI_COUNT) {
            recentList.removeAt(recentList.size - 1)
        }

        saveRecentEmojiList(recentList)
    }

} 