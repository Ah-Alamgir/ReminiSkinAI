package com.example.reminiskinai.platform

import com.example.reminiskinai.model.AestheticPreset
import com.example.reminiskinai.model.HairShadePreset
import com.example.reminiskinai.model.LipShadePreset
import com.example.reminiskinai.model.SkinEditParams
import platform.Foundation.NSUserDefaults

actual object PlatformSettings {
    private const val KEY_HAS_SAVED = "has_saved_settings"
    private const val KEY_PRESET = "preset_name"
    private const val KEY_LIP_SHADE = "lip_shade_name"
    private const val KEY_HAIR_SHADE = "hair_shade_name"
    private const val KEY_SKIN_SMOOTHNESS = "skin_smoothness"
    private const val KEY_LIP_COLOR = "lip_color_intensity"
    private const val KEY_HAIR_COLOR = "hair_color_intensity"
    private const val KEY_SKIN_RADIANCE = "skin_radiance"
    private const val KEY_SKIN_WARMTH = "skin_warmth"
    private const val KEY_FACIAL_CLARITY = "facial_clarity"
    private const val KEY_SKIN_COLOR_ENABLED = "skin_color_enabled"
    private const val KEY_FACE_RETOUCH_ENABLED = "face_retouch_enabled"
    private const val KEY_FACE_ENHANCER_ENABLED = "face_enhancer_enabled"
    private const val KEY_BODY_SYNC_ENABLED = "body_sync_enabled"

    actual fun loadSavedParams(): SkinEditParams {
        val def = NSUserDefaults.standardUserDefaults
        if (!def.boolForKey(KEY_HAS_SAVED)) return SkinEditParams()

        val presetName = def.stringForKey(KEY_PRESET) ?: AestheticPreset.REMINI_NATURAL.name
        val preset = try { AestheticPreset.valueOf(presetName) } catch (_: Exception) { AestheticPreset.REMINI_NATURAL }

        val lipName = def.stringForKey(KEY_LIP_SHADE) ?: LipShadePreset.NATURAL.name
        val lip = try { LipShadePreset.valueOf(lipName) } catch (_: Exception) { LipShadePreset.NATURAL }

        val hairName = def.stringForKey(KEY_HAIR_SHADE) ?: HairShadePreset.ORIGINAL.name
        val hair = try { HairShadePreset.valueOf(hairName) } catch (_: Exception) { HairShadePreset.ORIGINAL }

        val smoothness = if (def.objectForKey(KEY_SKIN_SMOOTHNESS) != null) def.floatForKey(KEY_SKIN_SMOOTHNESS) else 0.85f
        val lipColor = if (def.objectForKey(KEY_LIP_COLOR) != null) def.floatForKey(KEY_LIP_COLOR) else 1.00f
        val hairColor = if (def.objectForKey(KEY_HAIR_COLOR) != null) def.floatForKey(KEY_HAIR_COLOR) else 0.85f
        val radiance = if (def.objectForKey(KEY_SKIN_RADIANCE) != null) def.floatForKey(KEY_SKIN_RADIANCE) else 1.00f
        val warmth = if (def.objectForKey(KEY_SKIN_WARMTH) != null) def.floatForKey(KEY_SKIN_WARMTH) else 1.00f
        val clarity = if (def.objectForKey(KEY_FACIAL_CLARITY) != null) def.floatForKey(KEY_FACIAL_CLARITY) else 0.82f

        return SkinEditParams(
            skinColorEnabled = def.boolForKey(KEY_SKIN_COLOR_ENABLED),
            faceRetouchEnabled = def.boolForKey(KEY_FACE_RETOUCH_ENABLED),
            faceEnhancerEnabled = def.boolForKey(KEY_FACE_ENHANCER_ENABLED),
            bodySkinSyncEnabled = def.boolForKey(KEY_BODY_SYNC_ENABLED),
            preset = preset,
            lipShade = lip,
            hairShade = hair,
            skinSmoothness = smoothness,
            lipColorIntensity = lipColor,
            hairColorIntensity = hairColor,
            skinRadiance = radiance,
            skinWarmth = warmth,
            facialClarity = clarity
        )
    }

    actual fun hasCustomSavedSettings(): Boolean {
        return NSUserDefaults.standardUserDefaults.boolForKey(KEY_HAS_SAVED)
    }

    actual fun saveParams(params: SkinEditParams) {
        val def = NSUserDefaults.standardUserDefaults
        def.setBool(true, KEY_HAS_SAVED)
        def.setObject(params.preset.name, KEY_PRESET)
        def.setObject(params.lipShade.name, KEY_LIP_SHADE)
        def.setObject(params.hairShade.name, KEY_HAIR_SHADE)
        def.setFloat(params.skinSmoothness, KEY_SKIN_SMOOTHNESS)
        def.setFloat(params.lipColorIntensity, KEY_LIP_COLOR)
        def.setFloat(params.hairColorIntensity, KEY_HAIR_COLOR)
        def.setFloat(params.skinRadiance, KEY_SKIN_RADIANCE)
        def.setFloat(params.skinWarmth, KEY_SKIN_WARMTH)
        def.setFloat(params.facialClarity, KEY_FACIAL_CLARITY)
        def.setBool(params.skinColorEnabled, KEY_SKIN_COLOR_ENABLED)
        def.setBool(params.faceRetouchEnabled, KEY_FACE_RETOUCH_ENABLED)
        def.setBool(params.faceEnhancerEnabled, KEY_FACE_ENHANCER_ENABLED)
        def.setBool(params.bodySkinSyncEnabled, KEY_BODY_SYNC_ENABLED)
    }
}
