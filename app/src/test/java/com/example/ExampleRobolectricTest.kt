package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.TapeFilterPreset
import com.example.data.formatDurationMs
import com.example.ui.WlkmnApp
import com.example.ui.theme.WlkmnTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `app name is WLKMN`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WLKMN", appName)
    }

    @Test
    fun `tape filter presets attenuate high frequencies relative to clean digital`() {
        val ferricBands = TapeFilterPreset.TYPE_I_FERRIC.bandsMb
        val wornBands = TapeFilterPreset.WORN_CASSETTE.bandsMb
        assertEquals(5, ferricBands.size)
        assertTrue(ferricBands.last() < 0)
        assertTrue(wornBands.last() < ferricBands.last())
        assertEquals("03:05", formatDurationMs(185_000L))
    }

    @Test
    fun `navigation between song list, cassette deck, and customizer works`() {
        composeRule.setContent {
            WlkmnTheme {
                WlkmnApp()
            }
        }

        // Handwritten song list header is visible
        composeRule.onNodeWithText("WLKMN — Side A Mixtape List").assertIsDisplayed()

        // Navigate to Cassette Deck
        composeRule.onNodeWithTag("nav_cassette_deck").performClick()
        composeRule.onNodeWithTag("transport_play_pause_button").assertIsDisplayed()
        composeRule.onNodeWithTag("deck_tape_filter_badge").assertIsDisplayed()

        // Tap Tape Filter badge to jump to Customizer and select Worn Cassette + Navy Shell
        composeRule.onNodeWithTag("deck_tape_filter_badge").performClick()
        composeRule.onNodeWithTag("preset_worn_cassette").performScrollTo().performClick()
        composeRule.onNodeWithTag("shell_navy_walkman").performScrollTo().performClick()
    }

    @Test
    @Config(qualifiers = "w915dp-h412dp-land")
    fun `landscape orientation shows full screen cassette tape`() {
        composeRule.setContent {
            WlkmnTheme {
                WlkmnApp()
            }
        }

        composeRule.onNodeWithTag("landscape_cassette_root").assertIsDisplayed()
        composeRule.onNodeWithTag("landscape_full_cassette", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("landscape_play_pause_button").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_song_list").assertDoesNotExist()
    }
}
