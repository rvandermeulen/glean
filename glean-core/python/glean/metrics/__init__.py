# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.


"""
This module contains all of the metric types.
"""

# Re-export utilities
from .._uniffi import AttributionMetrics
from .._uniffi import CommonMetricData
from .._uniffi import DistributionMetrics
from .._uniffi import LabeledMetricData
from .._uniffi import Lifetime
from .._uniffi import MemoryUnit
from .._uniffi import TimerId
from .._uniffi import TimeUnit
from .._uniffi import RecordedExperiment

# Re-export some metrics directly
from .._uniffi import BooleanMetric as BooleanMetricType
from .._uniffi import CounterMetric as CounterMetricType
from .._uniffi import MemoryDistributionMetric as MemoryDistributionMetricType
from .._uniffi import QuantityMetric as QuantityMetricType
from .._uniffi import StringListMetric as StringListMetricType

# Export wrapper implementations for metric types
from .datetime import DatetimeMetricType
from .event import EventMetricType, EventExtras, RecordedEvent
from .object import ObjectMetricType, ObjectSerialize
from .labeled import (
    LabeledBooleanMetricType,
    LabeledCounterMetricType,
    LabeledStringMetricType,
)
from .ping import PingType
from .string import StringMetricType
from .timespan import TimespanMetricType
from .timing_distribution import TimingDistributionMetricType
from .url import UrlMetricType
from .uuid import UuidMetricType


__all__ = [
    "AttributionMetrics",
    "BooleanMetricType",
    "CommonMetricData",
    "CounterMetricType",
    "DatetimeMetricType",
    "DistributionMetrics",
    "EventExtras",
    "EventMetricType",
    "LabeledBooleanMetricType",
    "LabeledCounterMetricType",
    "LabeledMetricData",
    "LabeledStringMetricType",
    "Lifetime",
    "MemoryDistributionMetricType",
    "MemoryUnit",
    "ObjectMetricType",
    "ObjectSerialize",
    "PingType",
    "QuantityMetricType",
    "RecordedEvent",
    "RecordedExperiment",
    "StringListMetricType",
    "StringMetricType",
    "TimeUnit",
    "TimerId",
    "TimespanMetricType",
    "TimingDistributionMetricType",
    "UrlMetricType",
    "UuidMetricType",
]
