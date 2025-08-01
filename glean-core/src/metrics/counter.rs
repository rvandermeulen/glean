// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

use std::cmp::Ordering;
use std::sync::Arc;

use crate::common_metric_data::{CommonMetricDataInternal, DynamicLabelType};
use crate::error_recording::{record_error, test_get_num_recorded_errors, ErrorType};
use crate::metrics::Metric;
use crate::metrics::MetricType;
use crate::storage::StorageManager;
use crate::Glean;
use crate::{CommonMetricData, TestGetValue};

/// A counter metric.
///
/// Used to count things.
/// The value can only be incremented, not decremented.
#[derive(Clone, Debug)]
pub struct CounterMetric {
    meta: Arc<CommonMetricDataInternal>,
}

impl MetricType for CounterMetric {
    fn meta(&self) -> &CommonMetricDataInternal {
        &self.meta
    }

    fn with_name(&self, name: String) -> Self {
        let mut meta = (*self.meta).clone();
        meta.inner.name = name;
        Self {
            meta: Arc::new(meta),
        }
    }

    fn with_dynamic_label(&self, label: DynamicLabelType) -> Self {
        let mut meta = (*self.meta).clone();
        meta.inner.dynamic_label = Some(label);
        Self {
            meta: Arc::new(meta),
        }
    }
}

// IMPORTANT:
//
// When changing this implementation, make sure all the operations are
// also declared in the related trait in `../traits/`.
impl CounterMetric {
    /// Creates a new counter metric.
    pub fn new(meta: CommonMetricData) -> Self {
        Self {
            meta: Arc::new(meta.into()),
        }
    }

    /// Increases the counter by `amount` synchronously.
    #[doc(hidden)]
    pub fn add_sync(&self, glean: &Glean, amount: i32) {
        if !self.should_record(glean) {
            return;
        }

        match amount.cmp(&0) {
            Ordering::Less => {
                record_error(
                    glean,
                    &self.meta,
                    ErrorType::InvalidValue,
                    format!("Added negative value {}", amount),
                    None,
                );
                return;
            }
            Ordering::Equal => {
                // Silently ignore.
                return;
            }
            Ordering::Greater => (),
        };

        // Let's be defensive here:
        // The uploader tries to store a counter metric,
        // but in tests that storage might be gone already.
        // Let's just ignore those.
        // This should never happen in real app usage.
        if let Some(storage) = glean.storage_opt() {
            storage.record_with(glean, &self.meta, |old_value| match old_value {
                Some(Metric::Counter(old_value)) => {
                    Metric::Counter(old_value.saturating_add(amount))
                }
                _ => Metric::Counter(amount),
            })
        } else {
            log::warn!(
                "Couldn't get storage. Can't record counter '{}'.",
                self.meta.base_identifier()
            );
        }
    }

    /// Increases the counter by `amount`.
    ///
    /// # Arguments
    ///
    /// * `amount` - The amount to increase by. Should be positive.
    ///
    /// ## Notes
    ///
    /// Logs an error if the `amount` is 0 or negative.
    pub fn add(&self, amount: i32) {
        let metric = self.clone();
        crate::launch_with_glean(move |glean| metric.add_sync(glean, amount))
    }

    /// Get current value
    #[doc(hidden)]
    pub fn get_value<'a, S: Into<Option<&'a str>>>(
        &self,
        glean: &Glean,
        ping_name: S,
    ) -> Option<i32> {
        let queried_ping_name = ping_name
            .into()
            .unwrap_or_else(|| &self.meta().inner.send_in_pings[0]);

        match StorageManager.snapshot_metric_for_test(
            glean.storage(),
            queried_ping_name,
            &self.meta.identifier(glean),
            self.meta.inner.lifetime,
        ) {
            Some(Metric::Counter(i)) => Some(i),
            _ => None,
        }
    }

    /// **Exported for test purposes.**
    ///
    /// Gets the number of recorded errors for the given metric and error type.
    ///
    /// # Arguments
    ///
    /// * `error` - The type of error
    ///
    /// # Returns
    ///
    /// The number of errors reported.
    pub fn test_get_num_recorded_errors(&self, error: ErrorType) -> i32 {
        crate::block_on_dispatcher();

        crate::core::with_glean(|glean| {
            test_get_num_recorded_errors(glean, self.meta(), error).unwrap_or(0)
        })
    }
}

impl TestGetValue<i32> for CounterMetric {
    /// **Test-only API (exported for FFI purposes).**
    ///
    /// Gets the currently stored value as an integer.
    ///
    /// This doesn't clear the stored value.
    ///
    /// # Arguments
    ///
    /// * `ping_name` - the optional name of the ping to retrieve the metric
    ///                 for. Defaults to the first value in `send_in_pings`.
    ///
    /// # Returns
    ///
    /// The stored value or `None` if nothing stored.
    fn test_get_value(&self, ping_name: Option<String>) -> Option<i32> {
        crate::block_on_dispatcher();
        crate::core::with_glean(|glean| self.get_value(glean, ping_name.as_deref()))
    }
}
