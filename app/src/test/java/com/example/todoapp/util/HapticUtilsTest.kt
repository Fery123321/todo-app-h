package com.example.todoapp.util

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class HapticFeedbackManagerTest {

    private lateinit var context: Context
    private lateinit var vibrator: Vibrator
    private lateinit var hapticManager: HapticFeedbackManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        vibrator = mockk(relaxed = true)
        
        every { context.getSystemService(Context.VIBRATOR_SERVICE) } returns vibrator
        
        hapticManager = HapticFeedbackManager(context)
    }

    @Test
    fun `lightTap should trigger short vibration`() {
        // When
        hapticManager.lightTap()
        
        // Then
        verify { vibrator.vibrate(any()) }
    }

    @Test
    fun `mediumTap should trigger medium vibration`() {
        // When
        hapticManager.mediumTap()
        
        // Then
        verify { vibrator.vibrate(any()) }
    }

    @Test
    fun `strongTap should trigger strong vibration`() {
        // When
        hapticManager.strongTap()
        
        // Then
        verify { vibrator.vibrate(any()) }
    }

    @Test
    fun `success should trigger success pattern`() {
        // When
        hapticManager.success()
        
        // Then
        verify { vibrator.vibrate(any()) }
    }

    @Test
    fun `error should trigger error pattern`() {
        // When
        hapticManager.error()
        
        // Then
        verify { vibrator.vibrate(any()) }
    }
}