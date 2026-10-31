package com.example.reminiskinai.platform

import com.example.reminiskinai.model.AestheticPreset
import com.example.reminiskinai.model.HairShadePreset
import com.example.reminiskinai.model.LipShadePreset
import com.example.reminiskinai.model.SkinEditParams

actual object PlatformSettings {
    private var cachedParams: SkinEditParams = SkinEditParams()
    private var hasSaved: Boolean = false

    actual fun loadSavedParams(): SkinEditParams {
        return cachedParams
    }

    actual fun hasCustomSavedSettings(): Boolean {
        return hasSaved
    }

    actual fun saveParams(params: SkinEditParams) {
        cachedParams = params
        hasSaved = true
    }
}
