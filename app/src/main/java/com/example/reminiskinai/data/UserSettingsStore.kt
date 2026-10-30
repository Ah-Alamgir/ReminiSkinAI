package com.example.reminiskinai.data

import android.content.Context
import com.example.reminiskinai.ml.AestheticPreset
import com.example.reminiskinai.ml.HairShadePreset
import com.example.reminiskinai.ml.LipShadePreset
import com.example.reminiskinai.ml.SkinEditParams

/**
 * Persists the user's preferred Skin Smoothness, Regional Skin Color Preset, Lip Shade,
 * Hair Color Shade, and AI toggles in SharedPreferences so they automatically apply on every photo next time.
 */
object UserSettingsStore {
    private const val PREFS_NAME = "remini_skin_ai_user_settings"

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

    fun loadSavedParams(context: Context): SkinEditParams {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_HAS_SAVED, false)) {
            return SkinEditParams()
        }
        val presetName = prefs.getString(KEY_PRESET, AestheticPreset.REMINI_NATURAL.name)
            ?: AestheticPreset.REMINI_NATURAL.name
        val preset = try {
            AestheticPreset.valueOf(presetName)
        } catch (_: Exception) {
            AestheticPreset.REMINI_NATURAL
        }

        val lipShadeName = prefs.getString(KEY_LIP_SHADE, LipShadePreset.NATURAL.name)
            ?: LipShadePreset.NATURAL.name
        val lipShade = try {
            LipShadePreset.valueOf(lipShadeName)
        } catch (_: Exception) {
            LipShadePreset.NATURAL
        }

        val hairShadeName = prefs.getString(KEY_HAIR_SHADE, HairShadePreset.ORIGINAL.name)
            ?: HairShadePreset.ORIGINAL.name
        val hairShade = try {
            HairShadePreset.valueOf(hairShadeName)
        } catch (_: Exception) {
            HairShadePreset.ORIGINAL
        }

        return SkinEditParams(
            preset = preset,
            lipShade = lipShade,
            hairShade = hairShade,
            skinSmoothness = prefs.getFloat(KEY_SKIN_SMOOTHNESS, 0.85f).coerceIn(0f, 1f),
            lipColorIntensity = prefs.getFloat(KEY_LIP_COLOR, 1.00f).coerceIn(0f, 1.5f),
            hairColorIntensity = prefs.getFloat(KEY_HAIR_COLOR, 0.85f).coerceIn(0f, 1.2f),
            skinRadiance = prefs.getFloat(KEY_SKIN_RADIANCE, 1.00f).coerceIn(0f, 1.5f),
            skinWarmth = prefs.getFloat(KEY_SKIN_WARMTH, 1.00f).coerceIn(0.5f, 1.5f),
            facialClarity = prefs.getFloat(KEY_FACIAL_CLARITY, 0.82f).coerceIn(0f, 1.2f),
            skinColorEnabled = prefs.getBoolean(KEY_SKIN_COLOR_ENABLED, true),
            faceRetouchEnabled = prefs.getBoolean(KEY_FACE_RETOUCH_ENABLED, true),
            faceEnhancerEnabled = prefs.getBoolean(KEY_FACE_ENHANCER_ENABLED, true),
            bodySkinSyncEnabled = prefs.getBoolean(KEY_BODY_SYNC_ENABLED, true)
        )
    }

    fun hasCustomSavedSettings(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_HAS_SAVED, false)
    }

    fun saveParams(context: Context, params: SkinEditParams) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_HAS_SAVED, true)
            .putString(KEY_PRESET, params.preset.name)
            .putString(KEY_LIP_SHADE, params.lipShade.name)
            .putString(KEY_HAIR_SHADE, params.hairShade.name)
            .putFloat(KEY_SKIN_SMOOTHNESS, params.skinSmoothness)
            .putFloat(KEY_LIP_COLOR, params.lipColorIntensity)
            .putFloat(KEY_HAIR_COLOR, params.hairColorIntensity)
            .putFloat(KEY_SKIN_RADIANCE, params.skinRadiance)
            .putFloat(KEY_SKIN_WARMTH, params.skinWarmth)
            .putFloat(KEY_FACIAL_CLARITY, params.facialClarity)
            .putBoolean(KEY_SKIN_COLOR_ENABLED, params.skinColorEnabled)
            .putBoolean(KEY_FACE_RETOUCH_ENABLED, params.faceRetouchEnabled)
            .putBoolean(KEY_FACE_ENHANCER_ENABLED, params.faceEnhancerEnabled)
            .putBoolean(KEY_BODY_SYNC_ENABLED, params.bodySkinSyncEnabled)
            .apply()
    }
}
