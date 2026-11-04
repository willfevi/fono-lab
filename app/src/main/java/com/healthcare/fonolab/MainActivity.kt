package com.healthcare.fonolab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.healthcare.fonolab.core.designsystem.theme.FonoLabTheme
import com.healthcare.fonolab.core.navigation.FonoLabApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FonoLabTheme {
                FonoLabApp()
            }
        }
    }
}