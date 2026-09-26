package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Chess Arena", appName)
    }

    @Test
    fun `launch MainActivity and interact with Chess Arena`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("CHESS ARENA").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chess_board_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("battle_scenario_button").performClick()
        composeTestRule.waitForIdle()
    }
}
