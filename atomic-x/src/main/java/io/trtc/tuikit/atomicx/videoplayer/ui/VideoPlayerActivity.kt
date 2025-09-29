package io.trtc.tuikit.atomicx.videoplayer.ui

import android.R
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier

class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.color.black)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        enableEdgeToEdge()
        if (intent == null) {
            Log.e("VideoPlayerActivity", "Image Data is null!")
            finish()
            return
        }
        val data = intent.getParcelableExtra<Uri>("data")
        val width = intent.getIntExtra("width", 0)
        val height = intent.getIntExtra("height", 0)
        setContent {
            Column {
                Box(modifier = Modifier) {
                    VideoPlayer(data = data!!, width = width.toFloat(), height = height.toFloat())
                }
            }
        }
    }
}