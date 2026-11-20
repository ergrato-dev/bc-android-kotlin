package dev.bitacora

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Ya está hecho: el enunciado. No lo toques. IntentsRule va antes que la actividad: graba todo lo que sale de ella.
@RunWith(AndroidJUnit4::class)
class ReportTest {
    @get:Rule(order = 0)
    val intents = IntentsRule()

    @get:Rule(order = 1)
    val activity = ActivityScenarioRule(ReportActivity::class.java)

    // Espresso: buscar una vista, hacer algo con ella y comprobar cómo queda

    @Test
    fun sendIsOffWithoutText() {
        onView(withId(R.id.send)).check(matches(not(isEnabled())))
    }

    @Test
    fun typingTurnsSendOn() {
        onView(withId(R.id.note)).perform(typeText("riego"), closeSoftKeyboard())
        onView(withId(R.id.send)).check(matches(isEnabled()))
    }

    @Test
    fun onlySpacesIsNoText() {
        onView(withId(R.id.note)).perform(typeText("   "), closeSoftKeyboard())
        onView(withId(R.id.send)).check(matches(not(isEnabled())))
    }

    // Espresso-Intents: lo que sale de la app

    @Test
    fun sendOpensTheMail() {
        onView(withId(R.id.note)).perform(typeText("  riego roto  "), closeSoftKeyboard())
        onView(withId(R.id.send)).perform(click())
        intended(
            allOf(
                hasAction(Intent.ACTION_SENDTO),
                hasData(Uri.parse("mailto:$REPORTS")),
                hasExtra(Intent.EXTRA_SUBJECT, "Parte de campo"),
                hasExtra(Intent.EXTRA_TEXT, "riego roto"),
            ),
        )
    }

    @Test
    fun thePhotoComesFromTheCamera() {
        val photo = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
            .respondWith(ActivityResult(Activity.RESULT_OK, Intent().putExtra("data", photo)))
        onView(withId(R.id.photo)).perform(click())
        onView(withId(R.id.status)).check(matches(withText("Con foto")))
        onView(withId(R.id.preview)).check(matches(isDisplayed()))
    }

    @Test
    fun cancellingLeavesNoPhoto() {
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(ActivityResult(Activity.RESULT_CANCELED, null))
        onView(withId(R.id.photo)).perform(click())
        onView(withId(R.id.status)).check(matches(withText("Sin foto")))
        onView(withId(R.id.preview)).check(matches(not(isDisplayed())))
    }
}
