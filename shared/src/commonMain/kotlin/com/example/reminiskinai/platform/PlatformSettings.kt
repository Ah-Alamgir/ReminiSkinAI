package com.example.reminiskinai.platform

import com.example.reminiskinai.model.SkinEditParams

expect object PlatformSettings {
    fun loadSavedParams(): SkinEditParams
    fun hasCustomSavedSettings(): Boolean
    fun saveParams(params: SkinEditParams)
}
