/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.telemetry.glean.debug

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.telemetry.glean.Glean
import mozilla.telemetry.glean.getMockWebServer
import mozilla.telemetry.glean.private.BooleanMetricType
import mozilla.telemetry.glean.private.CommonMetricData
import mozilla.telemetry.glean.private.Lifetime
import mozilla.telemetry.glean.private.NoReasonCodes
import mozilla.telemetry.glean.private.PingType
import mozilla.telemetry.glean.resetGlean
import mozilla.telemetry.glean.testing.GleanTestRule
import mozilla.telemetry.glean.triggerWorkManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class GleanDebugActivityTest {
    private val testPackageName = "mozilla.telemetry.glean.test"

    @get:Rule
    val gleanRule = GleanTestRule(ApplicationProvider.getApplicationContext())

    @Before
    fun setup() {
        // This makes sure we have a "launch" intent in our package, otherwise
        // it will fail looking for it in `GleanDebugActivityTest`.
        val pm = ApplicationProvider.getApplicationContext<Context>().packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN)
        launchIntent.setPackage(testPackageName)
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        // Add a test main launcher activity.
        val resolveInfo = ResolveInfo()
        resolveInfo.activityInfo = ActivityInfo()
        resolveInfo.activityInfo.packageName = testPackageName
        resolveInfo.activityInfo.name = "LauncherActivity"
        @Suppress("DEPRECATION")
        shadowOf(pm).addResolveInfoForIntent(launchIntent, resolveInfo)

        // Add a second testing activity.
        val otherActivityInfo = ActivityInfo()
        otherActivityInfo.packageName = testPackageName
        otherActivityInfo.name = "OtherActivity"
        otherActivityInfo.exported = true
        shadowOf(pm).addOrUpdateActivity(otherActivityInfo)

        // Add another hidden testing activity.
        val hiddenActivity = ActivityInfo()
        hiddenActivity.packageName = testPackageName
        hiddenActivity.name = "HiddenActivity"
        hiddenActivity.exported = false
        shadowOf(pm).addOrUpdateActivity(hiddenActivity)
    }

    @Ignore("Fails with robolectric 4.5.1 - see bug 1698471")
    @Test
    fun `the main activity is correctly started and intent args are propagated`() {
        // Build the intent that will call our debug activity, with no extra.
        val intent = Intent(
            ApplicationProvider.getApplicationContext<Context>(),
            GleanDebugActivity::class.java,
        )
        // Add at least an option, otherwise the activity will be removed.
        intent.putExtra(GleanDebugActivity.LOG_PINGS_EXTRA_KEY, true)
        intent.putExtra("TestOptionFromCLI", "TestValue")
        // Start the activity through our intent.
        val scenario = launch<GleanDebugActivity>(intent)

        // Check that our main activity was launched.
        scenario.onActivity { activity ->
            val startedIntent = shadowOf(activity).peekNextStartedActivityForResult().intent
            assertEquals(testPackageName, startedIntent.`package`!!)
            // Make sure that the extra intent option was propagated to this intent.
            assertEquals("TestValue", startedIntent.getStringExtra("TestOptionFromCLI"))
        }
    }

    // TODO(jer): can we make this actually test something?
    // What we would want to do is really just the
    // "a custom activity is correctly started" test below:
    //
    // Even when Glean is not initialized we want the debug activity to run through,
    // not crash and just work.
    // If Glean is initialized later it should still trigger the debug activity tasks.
    @Test
    fun `it works without Glean initialized`() {
        // Destroy Glean. Launching the Debug Activity should only schedule tasks,
        // so they run once Glean is initialized.
        Glean.testDestroyGleanHandle()

        // Set the extra values and start the intent.
        val intent = Intent(
            ApplicationProvider.getApplicationContext<Context>(),
            GleanDebugActivity::class.java,
        )
        intent.putExtra(GleanDebugActivity.SEND_PING_EXTRA_KEY, "metrics")
        launch<GleanDebugActivity>(intent)
    }

    @Test
    fun `pings are sent using sendPing`() {
        val server = getMockWebServer()

        // Destroy Glean. Launching the Debug Activity should only schedule tasks,
        // so they run once Glean is initialized.
        Glean.testDestroyGleanHandle()

        val context = ApplicationProvider.getApplicationContext<Context>()

        // Put some metric data in the store, otherwise we won't get a ping out
        // Define a 'booleanMetric' boolean metric, which will be stored in "store1"
        val booleanMetric = BooleanMetricType(
            CommonMetricData(
                disabled = false,
                category = "telemetry",
                lifetime = Lifetime.APPLICATION,
                name = "boolean_metric",
                sendInPings = listOf("metrics"),
            ),
        )

        booleanMetric.set(true)

        // Set the extra values and start the intent.
        val intent = Intent(
            ApplicationProvider.getApplicationContext<Context>(),
            GleanDebugActivity::class.java,
        )
        intent.putExtra(GleanDebugActivity.SEND_PING_EXTRA_KEY, "metrics")
        launch<GleanDebugActivity>(intent)

        val config = Glean.configuration.copy(
            serverEndpoint = "http://" + server.hostName + ":" + server.port,
        )
        resetGlean(context, config)

        // Since we reset the serverEndpoint back to the default for untagged pings, we need to
        // override it here so that the local server we created to intercept the pings will
        // be the one that the ping is sent to.
        Glean.configuration = config

        triggerWorkManager(context)
        val request = server.takeRequest(10L, TimeUnit.SECONDS)!!

        assertTrue(
            request.requestUrl!!.encodedPath.startsWith("/submit/mozilla-telemetry-glean-test/metrics"),
        )

        server.shutdown()
    }

    @Test
    fun `debugViewTag filters ID's that don't match the pattern`() {
        val server = getMockWebServer()

        val context = ApplicationProvider.getApplicationContext<Context>()
        resetGlean(
            context,
            Glean.configuration.copy(
                serverEndpoint = "http://" + server.hostName + ":" + server.port,
            ),
        )

        // Put some metric data in the store, otherwise we won't get a ping out
        // Define a 'booleanMetric' boolean metric, which will be stored in "store1"
        val booleanMetric = BooleanMetricType(
            CommonMetricData(
                disabled = false,
                category = "telemetry",
                lifetime = Lifetime.APPLICATION,
                name = "boolean_metric",
                sendInPings = listOf("metrics"),
            ),
        )

        booleanMetric.set(true)
        assertTrue(booleanMetric.testGetValue()!!)

        // Set the extra values and start the intent.
        val intent = Intent(
            ApplicationProvider.getApplicationContext<Context>(),
            GleanDebugActivity::class.java,
        )
        intent.putExtra(GleanDebugActivity.SEND_PING_EXTRA_KEY, "metrics")
        intent.putExtra(GleanDebugActivity.TAG_DEBUG_VIEW_EXTRA_KEY, "inv@lid_id")
        launch<GleanDebugActivity>(intent)

        // Since a bad tag ID results in resetting the endpoint to the default, verify that
        // has happened.
        assertEquals(
            "Server endpoint must be reset if tag didn't pass regex",
            "http://" + server.hostName + ":" + server.port,
            Glean.configuration.serverEndpoint,
        )

        triggerWorkManager(context)
        val request = server.takeRequest(10L, TimeUnit.SECONDS)!!

        assertTrue(
            "Request path must be correct",
            request.requestUrl!!.encodedPath.startsWith("/submit/mozilla-telemetry-glean-test/metrics"),
        )

        // resetGlean doesn't actually reset the debug view tag,
        // so we might have a tag from other tests here.
        assertNotEquals("inv@lid_id", request.headers.get("X-Debug-ID"))

        server.shutdown()
    }

    @Test
    fun `pings are correctly tagged using sourceTags`() {
        val server = getMockWebServer()
        val testTags = setOf("tag1", "tag2")

        val context = ApplicationProvider.getApplicationContext<Context>()
        resetGlean(
            context,
            Glean.configuration.copy(
                serverEndpoint = "http://" + server.hostName + ":" + server.port,
            ),
        )

        // Create a custom ping for testing. Since we're testing headers,
        // it's fine for this to be empty.
        val customPing = PingType<NoReasonCodes>(
            name = "custom",
            includeClientId = false,
            sendIfEmpty = true,
            preciseTimestamps = true,
            includeInfoSections = true,
            enabled = true,
            schedulesPings = emptyList(),
            reasonCodes = listOf(),
            followsCollectionEnabled = true,
            uploaderCapabilities = emptyList(),
        )

        // Set the extra values and start the intent.
        val intent = Intent(
            ApplicationProvider.getApplicationContext<Context>(),
            GleanDebugActivity::class.java,
        )
        intent.putExtra(GleanDebugActivity.SEND_PING_EXTRA_KEY, "metrics")
        intent.putExtra(GleanDebugActivity.SOURCE_TAGS_KEY, testTags.toTypedArray())
        launch<GleanDebugActivity>(intent)

        customPing.submit()

        triggerWorkManager(context)

        val expectedTags = testTags.joinToString(",")

        // Expecting 2 pings: metrics, custom
        for (i in 1..2) {
            val request = server.takeRequest(10L, TimeUnit.SECONDS)!!

            assertTrue(
                "Request path must be correct",
                request.requestUrl!!.encodedPath.startsWith("/submit/mozilla-telemetry-glean-test"),
            )

            assertEquals(expectedTags, request.headers.get("X-Source-Tags"))
        }

        server.shutdown()
    }

    @Ignore("Fails with robolectric 4.5.1 - see bug 1698471")
    @Test
    fun `a custom activity is correctly started`() {
        // Build the intent that will call our debug activity, with no extra.
        val intent = Intent(
            ApplicationProvider.getApplicationContext<Context>(),
            GleanDebugActivity::class.java,
        )
        // Add at least an option, otherwise the activity will be removed.
        intent.putExtra(GleanDebugActivity.NEXT_ACTIVITY_TO_RUN, "OtherActivity")
        intent.putExtra("TestOptionFromCLI", "TestValue")
        // Start the activity through our intent.
        val scenario = launch<GleanDebugActivity>(intent)

        // Check that our main activity was launched.
        scenario.onActivity { activity ->
            val startedIntent = shadowOf(activity).peekNextStartedActivityForResult().intent
            assertEquals(testPackageName, startedIntent.`package`!!)
            assertEquals("OtherActivity", startedIntent.component!!.className)
            // Make sure that the extra intent option was propagated to this intent.
            assertEquals("TestValue", startedIntent.getStringExtra("TestOptionFromCLI"))
        }
    }

    @Ignore("Fails with robolectric 4.5.1 - see bug 1698471")
    @Test
    fun `non-exported activity is not started`() {
        // Build the intent that will call our debug activity, with no extra.
        val intent = Intent(
            ApplicationProvider.getApplicationContext<Context>(),
            GleanDebugActivity::class.java,
        )
        // Add at least an option, otherwise the activity will be removed.
        intent.putExtra(GleanDebugActivity.NEXT_ACTIVITY_TO_RUN, "HiddenActivity")
        // Start the activity through our intent.
        val scenario = launch<GleanDebugActivity>(intent)
        scenario.onActivity { activity ->
            // We don't expect any activity to be launched.
            assertNull(shadowOf(activity).peekNextStartedActivityForResult())
        }
    }
}
