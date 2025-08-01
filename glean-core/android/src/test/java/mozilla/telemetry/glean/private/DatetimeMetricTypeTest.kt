/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.telemetry.glean.private

/* This file is based on the tests in the Glean android-components implementation.
 *
 * Care should be taken to not reorder elements in this file so it will be easier
 * to track changes in Glean android-components.
 */

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.telemetry.glean.testing.GleanTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

const val MILLIS_PER_SEC = 1000L

private fun Date.asSeconds() = time / MILLIS_PER_SEC

@RunWith(AndroidJUnit4::class)
class DatetimeMetricTypeTest {
    @get:Rule
    val gleanRule = GleanTestRule(ApplicationProvider.getApplicationContext())

    @Test
    fun `The API saves to its storage engine`() {
        // Define a 'datetimeMetric' datetime metric, which will be stored in "store1"
        val datetimeMetric = DatetimeMetricType(
            CommonMetricData(
                disabled = false,
                category = "telemetry",
                lifetime = Lifetime.APPLICATION,
                name = "datetime_metric",
                sendInPings = listOf("store1"),
            ),
        )

        val value = Calendar.getInstance()
        value.set(2004, 11, 9, 8, 3, 29)
        value.timeZone = TimeZone.getTimeZone("America/Los_Angeles")
        datetimeMetric.set(value)
        assertEquals("2004-12-09T08:03-08:00", datetimeMetric.testGetValueAsString())

        val value2 = Calendar.getInstance()
        value2.set(1993, 1, 23, 9, 5, 43)
        value2.timeZone = TimeZone.getTimeZone("GMT+0")
        datetimeMetric.set(value2)
        // Check that data was properly recorded.
        assertEquals("1993-02-23T09:05+00:00", datetimeMetric.testGetValueAsString())

        // A date prior to the UNIX epoch
        val value3 = Calendar.getInstance()
        value3.set(1969, 7, 20, 20, 17, 3)
        value3.timeZone = TimeZone.getTimeZone("GMT-12")
        datetimeMetric.set(value3)
        // Check that data was properly recorded.
        assertEquals("1969-08-20T20:17-12:00", datetimeMetric.testGetValueAsString())

        // A date following 2038 (the extent of signed 32-bits after UNIX epoch)
        val value4 = Calendar.getInstance()
        value4.set(2039, 7, 20, 20, 17, 3)
        value4.timeZone = TimeZone.getTimeZone("GMT-4")
        datetimeMetric.set(value4)
        // Check that data was properly recorded.
        assertEquals("2039-08-20T20:17-04:00", datetimeMetric.testGetValueAsString())
    }

    @Test
    fun `disabled datetimes must not record data`() {
        // Define a 'datetimeMetric' datetime metric, which will be stored in "store1". It's disabled
        // so it should not record anything.
        val datetimeMetric = DatetimeMetricType(
            CommonMetricData(
                disabled = true,
                category = "telemetry",
                lifetime = Lifetime.APPLICATION,
                name = "datetimeMetric",
                sendInPings = listOf("store1"),
            ),
        )

        // Attempt to store the datetime.
        datetimeMetric.set()
        assertNull(datetimeMetric.testGetValue())
    }

    @Test
    fun `Regression test - setting date and reading results in the same`() {
        // This test is adopted from `SyncTelemetryTest.kt` in android-components.
        // Previously we failed to properly deal with DST when converting from `Calendar` into its pieces.

        val datetimeMetric = DatetimeMetricType(
            CommonMetricData(
                disabled = false,
                category = "telemetry",
                lifetime = Lifetime.PING,
                name = "datetimeMetric",
                sendInPings = listOf("store1"),
            ),
            timeUnit = TimeUnit.MILLISECOND,
        )

        val nowDate = Date()
        val now = nowDate.asSeconds()
        val timestamp = Date(now * MILLIS_PER_SEC)

        datetimeMetric.set(timestamp)

        assertEquals(now, datetimeMetric.testGetValue()!!.asSeconds())
    }
}
