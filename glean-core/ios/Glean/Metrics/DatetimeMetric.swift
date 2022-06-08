/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import Foundation

/// This implements the developer facing API for recording datetime metrics.
///
/// Instances of this class type are automatically generated by the parsers at build time,
/// allowing developers to record values that were previously registered in the metrics.yaml file.
///
/// The datetime API only exposes the `DatetimeMetricType.set(_:)` method, which takes care of validating the input
/// data and making sure that limits are enforced.
public class DatetimeMetricType {
    let inner: DatetimeMetric
    let timeUnit: TimeUnit

    /// The public constructor used by automatically generated metrics.
    public init(_ meta: CommonMetricData, _ timeUnit: TimeUnit) {
        self.timeUnit = timeUnit
        self.inner = DatetimeMetric(meta, timeUnit)
    }

    /// Set a datetime value, truncating it to the metric's resolution.
    ///
    /// - parameters:
    ///      * value: The [Date] value to set.  If not provided, will record the current time.
    public func set(_ value: Date = Date()) {
        let calendar = Calendar.current
        let components = calendar.dateComponents(in: TimeZone.current, from: value)
        set(components: components)
    }

    /// Set a datetime value, truncating it to the metric's resolution.
    ///
    /// This is provided as an internal-only function for convenience and so that we can test that timezones
    /// are passed through correctly.  The normal public interface uses `Date` objects which are in the local
    /// timezone.
    ///
    /// - parameters:
    ///     * components: The [DateComponents] value to set.
    func set(components: DateComponents) {
        let dt = Datetime(from: components)
        inner.set(dt)
    }

    /// Returns the string representation of the stored value for testing purposes only. This function
    /// will attempt to await the last task (if any) writing to the the metric's storage engine before returning
    ///  a value.
    ///
    /// - parameters:
    ///     * pingName: represents the name of the ping to retrieve the metric for.
    ///                 Defaults to the first value in `sendInPings`.
    ///
    /// - returns:  value of the stored metric
    public func testGetValueAsString(_ pingName: String? = nil) -> String? {
        inner.testGetValueAsString(pingName)
    }

    /// Returns the stored value for testing purposes only. This function will attempt to await the
    /// last task (if any) writing to the the metric's storage engine before returning a value.
    ///
    /// - parameters:
    ///     * pingName: represents the name of the ping to retrieve the metric for.
    ///                 Defaults to the first value in `sendInPings`.
    ///
    /// - returns:  value of the stored metric
    public func testGetValue(_ pingName: String? = nil) -> Date? {
        guard let date = inner.testGetValueAsString(pingName) else { return nil }
        return Date.fromISO8601String(
            dateString: date,
            precision: timeUnit
        )!
    }

    /// Returns the number of errors recorded for the given metric.
    ///
    /// - parameters:
    ///     * errorType: The type of error recorded.
    ///     * pingName: represents the name of the ping to retrieve the metric for.
    ///                 Defaults to the first value in `sendInPings`.
    ///
    /// - returns: The number of errors recorded for the metric for the given error type.
    public func testGetNumRecordedErrors(_ error: ErrorType, pingName: String? = nil) -> Int32 {
        inner.testGetNumRecordedErrors(error, pingName)
    }
}
