package dev.bitacora

import android.content.Intent
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "+es")
class ShareTest {
    @get:Rule(order = 0)
    val intents = IntentsRule()

    @get:Rule(order = 1)
    val rule = createEmptyComposeRule()

    @Test
    fun shareSendsTheText() {
        rule.launch()
        rule.onNodeWithText("Descripción").performTextInput("riego")
        rule.onNodeWithText("Alta").performClick()
        rule.onNodeWithText("Compartir").performClick()
        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(org.hamcrest.Matchers.equalTo(Intent.EXTRA_INTENT), allOf(hasAction(Intent.ACTION_SEND), hasExtra(org.hamcrest.Matchers.equalTo(Intent.EXTRA_TEXT), containsString("A-1 · Alta · riego")))),
            ),
        )
    }
}
