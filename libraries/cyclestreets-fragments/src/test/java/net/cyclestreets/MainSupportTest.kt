package net.cyclestreets

import android.net.Uri
import net.cyclestreets.LaunchIntent.Type.JOURNEY
import net.cyclestreets.LaunchIntent.Type.LOCATION
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@Config(manifest = Config.NONE, sdk = [28])
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
        val launchIntent = determineLaunchIntent(Uri.parse("http://m.cyclestreets.net/journey/#57201887/balanced"))!!
        assertThat(launchIntent.type).isEqualTo(JOURNEY)
        assertThat(launchIntent.id).isEqualTo(57201887)
    }

    @Test
    fun mobileLocation() {
        val launchIntent = determineLaunchIntent(Uri.parse("https://m.cyclestreets.net/location/#5678"))!!
        assertThat(launchIntent.type).isEqualTo(LOCATION)
        assertThat(launchIntent.id).isEqualTo(5678)
    }

    @Test
    fun cycleStreetsNetJourney() {
        val launchIntent = determineLaunchIntent(Uri.parse("http://cyclestreets.net/journey/61207326/#balanced"))!!
        assertThat(launchIntent.type).isEqualTo(JOURNEY)
        assertThat(launchIntent.id).isEqualTo(61207326)
    }

    @Test
    fun cycleStreetsNetLocation() {
        val launchIntent = determineLaunchIntent(Uri.parse("https://www.cyclestreets.net/location/1234"))!!
        assertThat(launchIntent.type).isEqualTo(LOCATION)
        assertThat(launchIntent.id).isEqualTo(1234)
    }

}
