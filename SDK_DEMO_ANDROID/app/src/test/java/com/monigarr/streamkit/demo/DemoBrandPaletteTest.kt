package com.monigarr.streamkit.demo

import androidx.compose.ui.graphics.Color
import com.monigarr.streamkit.demo.ui.theme.BrandAccentGreen
import com.monigarr.streamkit.demo.ui.theme.BrandBackground
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * File: DemoBrandPaletteTest.kt
 * Description: Locks Echelon brand palette constants used by the reference Compose shell.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.3.4
 */
class DemoBrandPaletteTest {

    @Test
    fun brandBackground_matchesDesignToken() {
        assertEquals(Color(0xFF08080C), BrandBackground)
    }

    @Test
    fun brandAccentGreen_matchesDesignToken() {
        assertEquals(Color(0xFF81C784), BrandAccentGreen)
    }
}
