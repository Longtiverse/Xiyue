package com.xiyue.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xiyue.app.ui.XiyueApp
import com.xiyue.app.ui.theme.XiyueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XiyueTheme {
                XiyueApp()
            }
        }
    }
}
