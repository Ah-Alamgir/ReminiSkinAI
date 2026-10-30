package com.example.reminiskinai.ui.main

import com.example.reminiskinai.ml.AestheticPreset
import com.example.reminiskinai.ml.SkinEditParams
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class MainScreenViewModelTest {
    @Test
    fun defaultParams_skinOnlyEnhancementEnabled() {
        val params = SkinEditParams()
        assertTrue(params.skinColorEnabled)
        assertTrue(params.faceRetouchEnabled)
        assertTrue(params.faceEnhancerEnabled)
        assertTrue(params.bodySkinSyncEnabled)
        assertEquals(AestheticPreset.REMINI_NATURAL, params.preset)
    }

    @Test
    fun reminiNaturalCurve_matchesTargetSkinMidtones() {
        val preset = AestheticPreset.REMINI_NATURAL
        // Input median skin RGB from IMG_2476: [146, 118, 104]
        val rIn = 146f / 255f
        val gIn = 118f / 255f
        val bIn = 104f / 255f

        val rOut = ((rIn + preset.alphaR * rIn * (1f - rIn)) * 255f).roundToInt()
        val gOut = ((gIn + preset.alphaG * gIn * (1f - gIn)) * 255f).roundToInt()
        val bOut = ((bIn + preset.alphaB * bIn * (1f - bIn)) * 255f).roundToInt()

        // Verify warm peachy-golden skin balance: R > G > B and within Remini target [183, 141, 121] ± 3
        assertTrue(rOut in 180..186)
        assertTrue(gOut in 138..145)
        assertTrue(bOut in 118..125)
    }
}
