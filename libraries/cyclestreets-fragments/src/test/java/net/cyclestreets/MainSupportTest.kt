package net.cyclestreets

import android.net.Uri
import net.cyclestreets.LaunchIntent.Type.JOURNEY
import net.cyclestreets.LaunchIntent.Type.LOCATION
import net.cyclestreets.view.BuildConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(constants = BuildConfig::class, manifest = Config.NONE, sdk = [27])
@RunWith(RobolectricTestRunner::class)
class MainSupportTest {

    @Test
    fun cycleStJourney() {
        val launchIntent = determineLaunchIntent(Uri.parse("http://cycle.st/j61207326"))!!
        assertThat(launchIntent.type).isEqualTo(JOURNEY)
        assertThat(launchIntent.id).isEqualTo(61207326)
    }

    @Test
    fun cycleStLocation() {
        val launchIntent = determineLaunchIntent(Uri.parse("https://cycle.st/p93348"))!!
        assertThat(launchIntent.type).isEqualTo(LOCATION)
        assertThat(launchIntent.id).isEqualTo(93348)
    }

    @Test
    fun mobileJourney() {
        val launchIntent = determineLaunchIntent(Uri.parse("https://m.cyclestreets.net/journey/#57201887/balanced"))!!
        assertThat(launchIntent.type).isEqualTo(JOURNEY)
        assertThat(launchIntent.id).isEqualTo(57201887)
    }

    @Test
    fun cycleStreetsNetJourney() {
        val launchIntent = determineLaunchIntent(Uri.parse("http://cyclestreets.net/journey/61207326/#balanced"))!!
        assertThat(launchIntent.type).isEqualTo(JOURNEY)
        assertThat(launchIntent.id).isEqualTo(61207326)
    }

    @Test
    fun cycleStreetsNetLocation() {
        val launchIntent = determineLaunchIntent(Uri.parse("http://cyclestreets.net/location/1234"))!!
        assertThat(launchIntent.type).isEqualTo(LOCATION)
        assertThat(launchIntent.id).isEqualTo(1234)
    }

}
