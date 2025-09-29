package io.trtc.tuikit.atomicx.imageviewer.ui

import android.R
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.trtc.tuikit.atomicx.imageviewer.ImageViewer
import io.trtc.tuikit.atomicx.videoplayer.ui.VideoPlayer

class ImageViewerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ImageViewerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        window.setBackgroundDrawableResource(R.color.black)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        enableEdgeToEdge()
        setContent {

            val dataList by ImageViewer.mediaList.collectAsState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                val index = dataList.indexOf(ImageViewer.initDataInternal)
                val pagerState = rememberPagerState(initialPage = index.coerceAtLeast(0), pageCount = { dataList.size })
                LaunchedEffect(pagerState.currentPage) {
                    if (dataList.isNotEmpty()) {
                        if (pagerState.currentPage == dataList.size - 1) {
                            Log.d(TAG, "Loading more images")
                            ImageViewer.eventHandler?.onEvent(mutableMapOf("onLoadMoreNewer" to ""), callback = {

                            })
                        } else if (pagerState.currentPage == 0) {
                            Log.d(TAG, "Loading more images")
                            ImageViewer.eventHandler?.onEvent(mutableMapOf("onLoadMoreOlder" to ""), callback = {
                            })
                        }
                    }
                }
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
                    val index = it
                    val data = dataList[index]
                    if (data.type == 0) {
                        ZoomablePhotoView(
                            modifier = Modifier.fillMaxSize(),
                            data = data.data!!,
                            onTap = {
                                Log.d(TAG, "Image tapped, finishing activity")
                                finish()
                            }
                        )
                    } else {
                        VideoPlayer(
                            data = data.data as Uri,
                            width = data.width.toFloat(),
                            height = data.height.toFloat()
                        )
                    }
                }
            }
        }
    }
}