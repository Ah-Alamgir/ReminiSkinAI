package com.example.reminiskinai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.reminiskinai.platform.PlatformActions
import com.example.reminiskinai.shared.App

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    PlatformActions.appContext = applicationContext
    enableEdgeToEdge()
    setContent {
      App()
    }
  }
}
