package uk.co.deanwild.materialshowcaseview

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrefsManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun newShowcase_hasNotFired() {
        val prefs = PrefsManager(context, "a")
        assertThat(prefs.hasFired()).isFalse()
        assertThat(prefs.getSequenceStatus()).isEqualTo(PrefsManager.SEQUENCE_NEVER_STARTED)
    }

    @Test
    fun setFired_isPersistedAcrossInstances() {
        PrefsManager(context, "a").setFired()
        assertThat(PrefsManager(context, "a").hasFired()).isTrue()
        assertThat(PrefsManager(context, "b").hasFired()).isFalse()
    }

    @Test
    fun sequenceStatus_roundTrips() {
        PrefsManager(context, "seq").setSequenceStatus(2)
        assertThat(PrefsManager(context, "seq").getSequenceStatus()).isEqualTo(2)
    }

    @Test
    fun resetShowcase_clearsOnlyThatId() {
        PrefsManager(context, "a").setFired()
        PrefsManager(context, "b").setFired()

        MaterialShowcaseView.resetSingleUse(context, "a")

        assertThat(PrefsManager(context, "a").hasFired()).isFalse()
        assertThat(PrefsManager(context, "b").hasFired()).isTrue()
    }

    @Test
    fun resetAll_clearsEverything() {
        PrefsManager(context, "a").setFired()
        PrefsManager(context, "b").setSequenceStatus(1)

        MaterialShowcaseView.resetAll(context)

        assertThat(PrefsManager(context, "a").hasFired()).isFalse()
        assertThat(PrefsManager(context, "b").getSequenceStatus()).isEqualTo(PrefsManager.SEQUENCE_NEVER_STARTED)
    }

    @Test
    fun instanceReset_works() {
        val prefs = PrefsManager(context, "a")
        prefs.setFired()
        prefs.resetShowcase()
        assertThat(prefs.hasFired()).isFalse()
    }

    @Test
    fun close_thenUse_doesNotCrash() {
        val prefs = PrefsManager(context, "a")
        prefs.close()
        prefs.resetShowcase()
    }
}
