package com.ragicorp.macassette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ragicorp.macassette.home.HomeScreen
import com.ragicorp.macassette.ui.theme.MaCassetteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaCassetteTheme {
                HomeScreen()
            }
        }
    }
}
