/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@testable import Glean
import OHHTTPStubs
import OHHTTPStubsSwift
import XCTest

private typealias GleanInternalMetrics = GleanMetrics.GleanInternalMetrics

// swiftlint:disable type_body_length
class GleanTests: XCTestCase {
    var expectation: XCTestExpectation?

    override func setUp() {
        resetGleanDiscardingInitialPings(testCase: self, tag: "GleanTests")
    }

    override func tearDown() {
        Glean.shared.testDestroyGleanHandle()
        expectation = nil
        tearDownStubs()
    }

    func testInitializeGlean() {
        // Glean is already initialized by the `setUp()` function
        XCTAssert(Glean.shared.isInitialized(), "Glean should be initialized")
    }

    func testExperimentRecording() {
        Glean.shared.setExperimentActive(
            "experiment_test",
            branch: "branch_a",
            extra: nil
        )
        Glean.shared.setExperimentActive(
            "experiment_api",
            branch: "branch_b",
            extra: ["test_key": "value"]
        )
        XCTAssertTrue(
            Glean.shared.testIsExperimentActive("experiment_test"),
            "Experiment must be active"
        )
        XCTAssertTrue(
            Glean.shared.testIsExperimentActive("experiment_api"),
            "Experiment must be active"
        )

        Glean.shared.setExperimentInactive("experiment_test")
        XCTAssertFalse(
            Glean.shared.testIsExperimentActive("experiment_test"),
            "Experiment must not be active"
        )
        XCTAssertTrue(
            Glean.shared.testIsExperimentActive("experiment_api"),
            "Experiment must be active"
        )

        let experimentData = Glean.shared.testGetExperimentData("experiment_api")
        XCTAssertEqual(
            "branch_b",
            experimentData?.branch,
            "Experiment must have expected branch"
        )
        XCTAssertEqual(
            "value",
            experimentData?.extra?["test_key"],
            "Experiment extra must have expected key/value"
        )
    }

    func testExperimentRecordingBeforeGleanInit() {
        // This test relies on Glean not being initialized and the task queueing to be on
        Glean.shared.testDestroyGleanHandle()

        Glean.shared.setExperimentActive(
            "experiment_set_preinit",
            branch: "branch_a",
            extra: nil
        )
        Glean.shared.setExperimentActive(
            "experiment_preinit_disabled",
            branch: "branch_a",
            extra: nil
        )

        // Deactivate the second experiment
        Glean.shared.setExperimentInactive("experiment_preinit_disabled")

        // This will reset Glean and flush the queued tasks
        resetGleanDiscardingInitialPings(testCase: self, tag: "GleanTests", clearStores: false)

        // Verify the tasks were executed
        XCTAssertTrue(
            Glean.shared.testIsExperimentActive("experiment_set_preinit"),
            "Experiment must be active"
        )
        XCTAssertFalse(
            Glean.shared.testIsExperimentActive("experiment_preinit_disabled"),
            "Experiment must not be active"
        )
    }

    func testGleanExperimentationIdIsSet() {
        Glean.shared.resetGlean(
            configuration: Configuration(
                maxEvents: nil,
                channel: nil,
                serverEndpoint: nil,
                dataPath: nil,
                logLevel: nil,
                enableEventTimestamps: true,
                experimentationId: "alpha-beta-gamma-delta"
            ),
            clearStores: true)
        XCTAssertEqual(
            "alpha-beta-gamma-delta",
            Glean.shared.testGetExperimentationId()!,
            "Experimenatation ids must match"
        )
    }

    func testGleanIsNotInitializedFromOtherProcesses() {
        // Check to see if Glean is initialized
        XCTAssert(Glean.shared.isInitialized())

        // Set the control variable to false to simulate that we are not running
        // in the main process
        Glean.shared.isMainProcess = false

        expectation = expectation(description: "GleanTests: Ping Received")
        // We are using OHHTTPStubs combined with an XCTestExpectation in order to capture
        // outgoing network requests and prevent actual requests being made from tests.
        stubServerReceive { _, _ in
            // Fulfill test's expectation once we parsed the incoming data.
            DispatchQueue.main.async {
                // Let the response get processed before we mark the expectation fulfilled
                self.expectation?.fulfill()
            }
        }

        // Invert the expectation so that it will assert if it gets fulfilled. Since Glean
        // is simulating being initialized on another process, we should not get any pings
        // since init should fail.
        expectation?.isInverted = true
        // Restart Glean
        Glean.shared.resetGlean(clearStores: false)
        waitForExpectations(timeout: 2.0) { error in
            XCTAssertNil(error, "Received a ping upload when we shouldn't have: \(error!)")
        }

        // Check to see if Glean is initialized
        XCTAssertFalse(Glean.shared.isInitialized())

        // Reset variable so as to not interfere with other tests.
        Glean.shared.isMainProcess = true
    }

    func testSettingDebugViewTagBeforeInitialization() {
        // This test relies on Glean not being initialized
        Glean.shared.testDestroyGleanHandle()

        XCTAssert(Glean.shared.setDebugViewTag("valid-tag"))

        // Restart glean
        resetGleanDiscardingInitialPings(testCase: self, tag: "GleanTest", clearStores: false)

        let host = URL(string: Configuration.Constants.defaultTelemetryEndpoint)!.host!
        stub(condition: isHost(host)) { data in
            let request = data as NSURLRequest
            XCTAssertEqual(request.value(forHTTPHeaderField: "X-Debug-ID"), "valid-tag")

            // Fulfill test's expectation once we parsed the incoming data.
            DispatchQueue.main.async {
                // Let the response get processed before we mark the expectation fulfilled
                self.expectation?.fulfill()
            }

            return HTTPStubsResponse(
                data: Data("OK".utf8),
                statusCode: 200,
                headers: nil
            )
        }

        expectation = expectation(description: "Completed upload")

        // Resetting Glean doesn't trigger pings in tests so we must call the method
        // directly to invoke a ping to be created
        Glean.shared.submitPingByName("baseline")

        waitForExpectations(timeout: 5.0) { error in
            XCTAssertNil(error, "Test timed out waiting for upload: \(error!)")
        }
    }

    func testSettingSourceTagsBeforeInitialization() {
        // This test relies on Glean not being initialized
        Glean.shared.testDestroyGleanHandle()

        XCTAssert(Glean.shared.setSourceTags(["valid-tag", "tag-valid"]))

        // Restart glean, disposing of any pings from startup that might interfere with the test
        resetGleanDiscardingInitialPings(testCase: self, tag: "GleanTest", clearStores: false)

        let host = URL(string: Configuration.Constants.defaultTelemetryEndpoint)!.host!
        stub(condition: isHost(host)) { data in
            let request = data as NSURLRequest
            XCTAssertEqual(request.value(forHTTPHeaderField: "X-Source-Tags"), "valid-tag,tag-valid")

            // Fulfill test's expectation once we parsed the incoming data.
            DispatchQueue.main.async {
                // Let the response get processed before we mark the expectation fulfilled
                self.expectation?.fulfill()
            }

            return HTTPStubsResponse(
                data: Data("OK".utf8),
                statusCode: 200,
                headers: nil
            )
        }

        expectation = expectation(description: "Completed upload")

        // We only want to submit the baseline ping, so we sumbit it by name
        Glean.shared.submitPingByName("baseline")

        waitForExpectations(timeout: 5.0) { error in
            XCTAssertNil(error, "Test timed out waiting for upload: \(error!)")
        }
    }

    func testFlippingUploadEnabledRespectsOrderOfEvents() {
        // This test relies on Glean not being initialized
        Glean.shared.testDestroyGleanHandle()

        // We expect only a single ping later
        stubServerReceive { pingType, _ in
            if pingType == "baseline" {
                // Ignore initial "active" baseline ping
                return
            }

            XCTAssertEqual("deletion-request", pingType)

            // Fulfill test's expectation once we parsed the incoming data.
            DispatchQueue.main.async {
                // Let the response get processed before we mark the expectation fulfilled
                self.expectation?.fulfill()
            }
        }

        let customPing = Ping<NoReasonCodes>(
            name: "custom",
            includeClientId: true,
            sendIfEmpty: false,
            preciseTimestamps: true,
            includeInfoSections: true,
            enabled: true,
            schedulesPings: [],
            reasonCodes: [],
            followsCollectionEnabled: true,
            uploaderCapabilities: []
        )

        let counter = CounterMetricType(CommonMetricData(
            category: "telemetry",
            name: "counter_metric",
            sendInPings: ["custom"],
            lifetime: .application,
            disabled: false
        ))

        expectation = expectation(description: "Completed upload")

        // Set the last time the "metrics" ping was sent to now. This is required for us to not
        // send a metrics pings the first time we initialize Glean and to keep it from interfering
        // with these tests.
        let now = Date()
        MetricsPingScheduler(true).updateSentDate(now)
        // Restart glean
        Glean.shared.resetGlean(clearStores: false)

        // Glean might still be initializing. Disable upload.
        Glean.shared.setCollectionEnabled(false)

        // Set data and try to submit a custom ping.
        counter.add(1)
        customPing.submit()

        waitForExpectations(timeout: 5.0) { error in
            XCTAssertNil(error, "Test timed out waiting for upload: \(error!)")
        }
    }

    func testSettingRemoteMetricConfiguration() {
        let counter = CounterMetricType(CommonMetricData(
            category: "telemetry",
            name: "counter_metric",
            sendInPings: ["custom"],
            lifetime: .application,
            disabled: true
        ))

        // Set a metric configuration that enables telemetry.counter_metric
        let metricConfigStringifiedJson =
"""
{
  "metrics_enabled": {
    "telemetry.counter_metric": true
  }
}
"""
        Glean.shared.applyServerKnobsConfig(metricConfigStringifiedJson)

        // Attempt to add to the counter, this should succeed.
        counter.add(1)
        if let value = counter.testGetValue() {
            XCTAssertEqual(1, value)
        } else {
            XCTAssert(false, "Failed to set metric config to enable counter")
        }
    }

    func testForegroundCounter() {
        // Reset Glean to ensure that this isn't affected by concurrent tests
        // This will trigger the initial "foreground" event
        resetGleanDiscardingInitialPings(testCase: self, tag: "GleanTests")

        // Put it in the background
        Glean.shared.handleBackgroundEvent()

        // Bring it back
        Glean.shared.handleForegroundEvent()

        let foregroundCounter = GleanMetrics.GleanValidation.foregroundCount
        XCTAssertEqual(2, foregroundCounter.testGetValue())
    }

    func testPassingInExplicitBuildInfo() {
        Glean.shared.testDestroyGleanHandle()

        Glean.shared.initialize(uploadEnabled: true, buildInfo: stubBuildInfo("2020-11-06T11:30:50+0000"))
        let expected = Date.fromISO8601String(dateString: "2020-11-06T11:30:50+00:00", precision: .second)
        XCTAssertEqual(
            expected,
            GleanInternalMetrics.buildDate.testGetValue()
        )
    }

    func testGleanDoesNotInitializeWithInvalidDbPath() {
        Glean.shared.testDestroyGleanHandle()

        // The path provided here is invalid because it is an empty string.
        let cfg = Configuration(dataPath: "")
        Glean.shared.initialize(uploadEnabled: true, configuration: cfg, buildInfo: stubBuildInfo())

        // Since the path is invalid, Glean should not properly initialize.
        XCTAssertFalse(Glean.shared.isInitialized())
    }

    func testGleanIsCustomDataPathIsSetCorrectly() {
        // Initialize with a custom data path and ensure `isCustomDataPath` is true.
        Glean.shared.testDestroyGleanHandle()
        let cfg = Configuration(dataPath: "glean_test")
        Glean.shared.initialize(uploadEnabled: true, configuration: cfg, buildInfo: stubBuildInfo())
        XCTAssertTrue(Glean.shared.isCustomDataPath)

        // Initialize without a custom data path and ensure `isCustomDataPath` is false.
        Glean.shared.testDestroyGleanHandle()
        Glean.shared.initialize(uploadEnabled: true, buildInfo: stubBuildInfo())
        XCTAssertFalse(Glean.shared.isCustomDataPath)
    }

    func testShutdown() {
        // This test relies on Glean not being initialized
        Glean.shared.testDestroyGleanHandle()

        // We expect 10 pings later
        stubServerReceive { pingType, _ in
            if pingType == "baseline" {
                // Ignore initial "active" baseline ping
                return
            }

            XCTAssertEqual("custom", pingType)

            // Fulfill test's expectation once we parsed the incoming data.
            DispatchQueue.main.async {
                // Let the response get processed before we mark the expectation fulfilled
                self.expectation?.fulfill()
            }
        }

        let customPing = Ping<NoReasonCodes>(
            name: "custom",
            includeClientId: true,
            sendIfEmpty: false,
            preciseTimestamps: true,
            includeInfoSections: true,
            enabled: true,
            schedulesPings: [],
            reasonCodes: [],
            followsCollectionEnabled: true,
            uploaderCapabilities: []
        )

        let counter = CounterMetricType(CommonMetricData(
            category: "telemetry",
            name: "counter_metric",
            sendInPings: ["custom"],
            lifetime: .application,
            disabled: false
        ))

        expectation = expectation(description: "Completed upload")
        expectation?.expectedFulfillmentCount = 10

        // Set the last time the "metrics" ping was sent to now. This is required for us to not
        // send a metrics pings the first time we initialize Glean and to keep it from interfering
        // with these tests.
        let now = Date()
        MetricsPingScheduler(true).updateSentDate(now)
        // Restart glean
        Glean.shared.resetGlean(clearStores: false)

        // Set data and try to submit a custom ping 10x.
        for _ in (0..<10) {
            counter.add(1)
            customPing.submit()
        }

        Glean.shared.shutdown()
        waitForExpectations(timeout: 5.0) { error in
            XCTAssertNil(error, "Test timed out waiting for upload: \(error!)")
        }
    }
}
// swiftlint:enable type_body_length
