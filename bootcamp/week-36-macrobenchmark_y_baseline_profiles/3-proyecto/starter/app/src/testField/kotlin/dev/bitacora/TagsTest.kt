package dev.bitacora

import androidx.compose.ui.semantics.SemanticsPropertiesAndroid
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "+es")
class TagsTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    @Test
    fun theRobotAddsAndFinds() {
        rule.launch()
        rule.bitacora {
            add("riego", "Alta")
            add("helada", "Baja")
            assertRows(2)
            assertRow("A-2 · Baja · helada")
        }
    }

    @Test
    fun theTagsAreResourceIds() {
        rule.launch()
        rule.onNode(SemanticsMatcher.expectValue(SemanticsPropertiesAndroid.TestTagsAsResourceId, true)).assertExists()
    }
}
