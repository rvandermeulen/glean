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
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class UuidMetricTypeTest {
    @get:Rule
    val gleanRule = GleanTestRule(ApplicationProvider.getApplicationContext())

    @Test
    fun `The API saves to its storage engine`() {
        // Define a 'uuidMetric' uuid metric, which will be stored in "store1"
        val uuidMetric = UuidMetricType(
            CommonMetricData(
                disabled = false,
                category = "telemetry",
                lifetime = Lifetime.APPLICATION,
                name = "uuid_metric",
                sendInPings = listOf("store1"),
            ),
        )

        // Check that there is no UUID recorded
        assertNull(uuidMetric.testGetValue())

        // Record two uuids of the same type, with a little delay.
        val uuid: UUID = uuidMetric.generateAndSet()

        // Check that data was properly recorded.
        assertEquals(uuid, uuidMetric.testGetValue())

        val uuid2 = UUID.fromString("ce2adeb8-843a-4232-87a5-a099ed1e7bb3")
        uuidMetric.set(uuid2)

        // Check that data was properly recorded.
        assertEquals(uuid2, uuidMetric.testGetValue())
    }

    @Test
    fun `disabled uuids must not record data`() {
        // Define a 'uuidMetric' uuid metric, which will be stored in "store1". It's disabled
        // so it should not record anything.
        val uuidMetric = UuidMetricType(
            CommonMetricData(
                disabled = true,
                category = "telemetry",
                lifetime = Lifetime.APPLICATION,
                name = "uuidMetric",
                sendInPings = listOf("store1"),
            ),
        )

        // Attempt to store the uuid.
        uuidMetric.generateAndSet()
        // Check that nothing was recorded.
        assertNull(
            "Uuids must not be recorded if they are disabled",
            uuidMetric.testGetValue(),
        )
    }

    @Test
    fun `testGetValue() returns null if nothing is stored`() {
        val uuidMetric = UuidMetricType(
            CommonMetricData(
                disabled = true,
                category = "telemetry",
                lifetime = Lifetime.APPLICATION,
                name = "uuidMetric",
                sendInPings = listOf("store1"),
            ),
        )
        assertNull(uuidMetric.testGetValue())
    }

    @Test
    fun `The API saves to secondary pings`() {
        // Define a 'uuidMetric' uuid metric, which will be stored in "store1" and "store2"
        val uuidMetric = UuidMetricType(
            CommonMetricData(
                disabled = false,
                category = "telemetry",
                lifetime = Lifetime.APPLICATION,
                name = "uuid_metric",
                sendInPings = listOf("store1", "store2"),
            ),
        )

        // Record two uuids of the same type, with a little delay.
        val uuid = uuidMetric.generateAndSet()

        // Check that data was properly recorded.
        assertEquals(uuid, uuidMetric.testGetValue("store2"))

        val uuid2 = UUID.fromString("ce2adeb8-843a-4232-87a5-a099ed1e7bb3")
        uuidMetric.set(uuid2)

        // Check that data was properly recorded.
        assertEquals(uuid2, uuidMetric.testGetValue("store2"))
    }
}
