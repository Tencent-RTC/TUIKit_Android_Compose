package io.trtc.tuikit.chat.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tencent.mmkv.MMKV
import io.trtc.tuikit.atomicx.basecomponent.basiccontrols.Toast
import io.trtc.tuikit.atomicx.basecomponent.theme.LocalTheme
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.chat.BaseActivity
import io.trtc.tuikit.chat.MainActivity
import io.trtc.tuikit.chat.R
import io.trtc.tuikit.chat.signature.GenerateTestUserSig

class LoginActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loginUser = MMKV.defaultMMKV().decodeString("LoginUser", "")
        if (!loginUser.isNullOrEmpty()) {
            login(loginUser)
            return
        }

        setContent {
            val colors = LocalTheme.current.colors
            Scaffold(modifier = Modifier) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colors.bgColorOperate)
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var userID by remember { mutableStateOf("") }
                    val windowSize = LocalWindowInfo.current.containerSize
                    val density = LocalDensity.current
                    Box(
                        modifier = Modifier
                            .width(with(density) { (windowSize.width * 0.8f).toDp() })
                            .background(
                                color = colors.bgColorInput,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = userID,
                            onValueChange = { newText ->
                                userID = newText
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = colors.textColorPrimary,
                                fontWeight = FontWeight.W400
                            ),
                            singleLine = false,
                            maxLines = 1,
                            cursorBrush = SolidColor(colors.textColorLink),
                            decorationBox = { innerTextField ->
                                if (userID.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.compose_demo_input_user_id_tips),
                                        fontSize = 16.sp,
                                        color = colors.textColorSecondary,
                                        fontWeight = FontWeight.W400
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                login(userID)
                            }
                            .background(color = colors.bgColorInput)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.compose_demo_login),
                            fontSize = 16.sp,
                            color = colors.textColorPrimary
                        )
                    }
                }
            }
        }
    }

    fun login(userID: String) {
        LoginStore.shared.login(
            this,
            GenerateTestUserSig.SDKAPPID,
            userID,
            GenerateTestUserSig.genTestUserSig(userID), object :
                CompletionHandler {
                override fun onSuccess() {
                    MMKV.defaultMMKV().encode("LoginUser", userID)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }

                override fun onFailure(code: Int, desc: String) {
                    Toast.error(this@LoginActivity, desc)
                }
            })
    }
}