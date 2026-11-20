package dev.bitacora

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

// Ya está hecho: una captura del parte con texto. La de referencia no está: la grabas tú (paso 5).
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTest {
    @get:Rule
    val activity = ActivityScenarioRule(ReportActivity::class.java)

    @Test
    fun aReportWithText() {
        onView(withId(R.id.note)).perform(typeText("riego roto"), closeSoftKeyboard())
        onView(isRoot()).captureRoboImage()
    }
}
