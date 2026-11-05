package com.healthcare.fonolab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.healthcare.fonolab.shared.App
import com.healthcare.fonolab.data.di.androidRoomModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val roomModule = androidRoomModule(applicationContext)
        setContent {
            App(platformModule = roomModule)
        }
    }
}
