package com.example.reminiskinai.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.reminiskinai.theme.ReminiBlack
import com.example.reminiskinai.theme.ReminiSkinAITheme
import com.example.reminiskinai.ui.main.MainScreen
import com.example.reminiskinai.ui.passport.PassportPhotoScreen

enum class AppScreen {
    MAIN,
    PASSPORT_STUDIO
}

@Composable
fun App() {
    ReminiSkinAITheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(ReminiBlack),
            color = ReminiBlack
        ) {
            var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }

            when (currentScreen) {
                AppScreen.MAIN -> {
                    MainScreen(
                        onNavigateToPassport = { currentScreen = AppScreen.PASSPORT_STUDIO },
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    )
                }
                AppScreen.PASSPORT_STUDIO -> {
                    PassportPhotoScreen(
                        onBackToMain = { currentScreen = AppScreen.MAIN },
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    )
                }
            }
        }
    }
}
