package com.monigarr.streamkit.core

import com.monigarr.streamkit.core.assets.AssetManager3D
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * File: AssetManager3DTest.kt
 * Description: Reference-counted GPU asset registry (pressure flow).
 * Author: monigarr@monigarr.com
 * Date: 2026-04-14
 * Version: 1.3.4
 */
class AssetManager3DTest {

    @Test
    fun retainAndRelease_updatesPressure() =
        runBlocking {
            val mgr = AssetManager3D()
            mgr.retain("atlas-a", 2048L)
            delay(120)
            assertEquals(1, mgr.pressure.value.retainedAssets)
            assertEquals(2048L, mgr.pressure.value.estimatedBytes)
            mgr.retain("atlas-a", 2048L)
            delay(120)
            assertEquals(1, mgr.pressure.value.retainedAssets)
            mgr.release("atlas-a")
            delay(120)
            assertEquals(1, mgr.pressure.value.retainedAssets)
            mgr.release("atlas-a")
            delay(120)
            assertEquals(0, mgr.pressure.value.retainedAssets)
            mgr.cancel()
        }

    @Test
    fun resetForTests_clearsState() =
        runBlocking {
            val mgr = AssetManager3D()
            mgr.retain("x", 100L)
            delay(120)
            mgr.resetForTests()
            assertEquals(0, mgr.pressure.value.retainedAssets)
            mgr.cancel()
        }
}
