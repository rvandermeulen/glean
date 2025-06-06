{{#include ../../../shared/blockquote-info.html}}

# The Glean JavaScript SDK provides a slightly different set of metrics and pings

> If you are looking for the metrics collected by Glean.js,
> refer to the documentation over on the [`@mozilla/glean.js`](https://github.com/mozilla/glean.js/blob/main/docs/reference/metrics.md) repository.
<!-- AUTOGENERATED BY glean_parser v17.1.0. DO NOT EDIT. -->

# Metrics

This document enumerates the metrics collected by this project using the [Glean SDK](https://mozilla.github.io/glean/book/index.html).
This project may depend on other projects which also collect metrics.
This means you might have to go searching through the dependency tree to get a full picture of everything collected by this project.

# Pings

- [all-pings](#all-pings)
- [baseline](#baseline)
- [deletion-request](#deletion-request)
- [glean_internal_info](#glean_internal_info)
- [metrics](#metrics)

## all-pings

These metrics are sent in every ping.

All Glean pings contain built-in metrics in the [`ping_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-ping_info-section) and [`client_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-client_info-section) sections.

In addition to those built-in metrics, the following metrics are added to the ping:

| Name | Type | Description | Data reviews | Extras | Expiration | [Data Sensitivity](https://wiki.mozilla.org/Firefox/Data_Collection) |
| --- | --- | --- | --- | --- | --- | --- |
| glean.client.annotation.experimentation_id |[string](https://mozilla.github.io/glean/book/user/metrics/string.html) |An experimentation identifier derived and provided by the application for the purpose of experimentation enrollment.  |[Bug 1848201](https://bugzilla.mozilla.org/show_bug.cgi?id=1848201#c5)||never |1 |
| glean.error.invalid_label |[labeled_counter](https://mozilla.github.io/glean/book/user/metrics/labeled_counters.html) |Counts the number of times a metric was set with an invalid label. The labels are the `category.name` identifier of the metric.  |[Bug 1499761](https://bugzilla.mozilla.org/show_bug.cgi?id=1499761#c5)||never |1 |
| glean.error.invalid_overflow |[labeled_counter](https://mozilla.github.io/glean/book/user/metrics/labeled_counters.html) |Counts the number of times a metric was set a value that overflowed. The labels are the `category.name` identifier of the metric.  |[Bug 1591912](https://bugzilla.mozilla.org/show_bug.cgi?id=1591912#c3)||never |1 |
| glean.error.invalid_state |[labeled_counter](https://mozilla.github.io/glean/book/user/metrics/labeled_counters.html) |Counts the number of times a timing metric was used incorrectly. The labels are the `category.name` identifier of the metric.  |[Bug 1499761](https://bugzilla.mozilla.org/show_bug.cgi?id=1499761#c5)||never |1 |
| glean.error.invalid_value |[labeled_counter](https://mozilla.github.io/glean/book/user/metrics/labeled_counters.html) |Counts the number of times a metric was set to an invalid value. The labels are the `category.name` identifier of the metric.  |[Bug 1499761](https://bugzilla.mozilla.org/show_bug.cgi?id=1499761#c5)||never |1 |
| glean.restarted |[event](https://mozilla.github.io/glean/book/user/metrics/event.html) |Recorded when the Glean SDK is restarted.  Only included in custom pings that record events.  For more information, please consult the [Custom Ping documentation](https://mozilla.github.io/glean/book/user/pings/custom.html#the-gleanrestarted-event).  |[Bug 1716725](https://bugzilla.mozilla.org/show_bug.cgi?id=1716725)||never |1 |

## baseline

This is a built-in ping that is assembled out of the box by the Glean SDK.

See the Glean SDK documentation for the [`baseline` ping](https://mozilla.github.io/glean/book/user/pings/baseline.html).

This ping is sent if empty.

This ping includes the [client id](https://mozilla.github.io/glean/book/user/pings/index.html#the-client_info-section).

**Data reviews for this ping:**

- <https://bugzilla.mozilla.org/show_bug.cgi?id=1512938#c3>
- <https://bugzilla.mozilla.org/show_bug.cgi?id=1599877#c25>

**Bugs related to this ping:**

- <https://bugzilla.mozilla.org/1512938>
- <https://bugzilla.mozilla.org/1599877>

**Reasons this ping may be sent:**

- `active`: The ping was submitted when the application became active again, which
      includes when the application starts. In earlier versions, this was called
      `foreground`.

      *Note*: this ping will not contain the `glean.baseline.duration` metric.

- `dirty_startup`: The ping was submitted at startup, because the application process was
      killed before the Glean SDK had the chance to generate this ping, before
      becoming inactive, in the last session.

      *Note*: this ping will not contain the `glean.baseline.duration` metric.

- `inactive`: The ping was submitted when becoming inactive. In earlier versions, this
      was called `background`.


All Glean pings contain built-in metrics in the [`ping_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-ping_info-section) and [`client_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-client_info-section) sections.

In addition to those built-in metrics, the following metrics are added to the ping:

| Name | Type | Description | Data reviews | Extras | Expiration | [Data Sensitivity](https://wiki.mozilla.org/Firefox/Data_Collection) |
| --- | --- | --- | --- | --- | --- | --- |
| glean.baseline.duration |[timespan](https://mozilla.github.io/glean/book/user/metrics/timespan.html) |The duration of the last foreground session.  |[Bug 1512938](https://bugzilla.mozilla.org/show_bug.cgi?id=1512938#c3)||never |1, 2 |
| glean.validation.pings_submitted |[labeled_counter](https://mozilla.github.io/glean/book/user/metrics/labeled_counters.html) |A count of the built-in pings submitted, by ping type.  This metric appears in both the metrics and baseline pings.  - On the metrics ping, the counts include the number of pings sent since   the last metrics ping (including the last metrics ping) - On the baseline ping, the counts include the number of pings send since   the last baseline ping (including the last baseline ping)  Note: Previously this also recorded the number of submitted custom pings. Now it only records counts for the Glean built-in pings.  |[Bug 1586764](https://bugzilla.mozilla.org/show_bug.cgi?id=1586764#c3)||never |1 |

## deletion-request

This is a built-in ping that is assembled out of the box by the Glean SDK.

See the Glean SDK documentation for the [`deletion-request` ping](https://mozilla.github.io/glean/book/user/pings/deletion-request.html).

This ping is sent if empty.

This ping includes the [client id](https://mozilla.github.io/glean/book/user/pings/index.html#the-client_info-section).

**Data reviews for this ping:**

- <https://bugzilla.mozilla.org/show_bug.cgi?id=1587095#c6>
- <https://bugzilla.mozilla.org/show_bug.cgi?id=1702622#c2>

**Bugs related to this ping:**

- <https://bugzilla.mozilla.org/1587095>
- <https://bugzilla.mozilla.org/1702622>

**Reasons this ping may be sent:**

- `at_init`: The ping was submitted at startup.
      Glean discovered that between the last time it was run and this time,
      upload of data has been disabled.

- `set_upload_enabled`: The ping was submitted between Glean init and Glean shutdown.
      Glean was told after init but before shutdown that upload has changed
      from enabled to disabled.


All Glean pings contain built-in metrics in the [`ping_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-ping_info-section) and [`client_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-client_info-section) sections.

This ping contains no metrics.

## glean_internal_info

All Glean pings contain built-in metrics in the [`ping_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-ping_info-section) and [`client_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-client_info-section) sections.

In addition to those built-in metrics, the following metrics are added to the ping:

| Name | Type | Description | Data reviews | Extras | Expiration | [Data Sensitivity](https://wiki.mozilla.org/Firefox/Data_Collection) |
| --- | --- | --- | --- | --- | --- | --- |
| glean.internal.metrics.attribution.campaign |[string](https://mozilla.github.io/glean/book/user/metrics/string.html) |The optional attribution campaign. Similar to or the same as UTM `campaign`.  |[Bug 1955428](https://bugzilla.mozilla.org/show_bug.cgi?id=1955428)||never |1 |
| glean.internal.metrics.attribution.content |[string](https://mozilla.github.io/glean/book/user/metrics/string.html) |The optional attribution `content`. Similar to or the same as UTM `content`.  |[Bug 1955428](https://bugzilla.mozilla.org/show_bug.cgi?id=1955428)||never |1 |
| glean.internal.metrics.attribution.medium |[string](https://mozilla.github.io/glean/book/user/metrics/string.html) |The optional attribution medium. Similar to or the same as UTM `medium`.  |[Bug 1955428](https://bugzilla.mozilla.org/show_bug.cgi?id=1955428)||never |1 |
| glean.internal.metrics.attribution.source |[string](https://mozilla.github.io/glean/book/user/metrics/string.html) |The optional attribution source. Similar to or the same as UTM `source`.  |[Bug 1955428](https://bugzilla.mozilla.org/show_bug.cgi?id=1955428)||never |1 |
| glean.internal.metrics.attribution.term |[string](https://mozilla.github.io/glean/book/user/metrics/string.html) |The optional attribution term. Similar to or the same as UTM `term`.  |[Bug 1955428](https://bugzilla.mozilla.org/show_bug.cgi?id=1955428)||never |1 |
| glean.internal.metrics.distribution.name |[string](https://mozilla.github.io/glean/book/user/metrics/string.html) |The optional distribution name. Can be a partner name, or a distribution configuration preset.  |[Bug 1955428](https://bugzilla.mozilla.org/show_bug.cgi?id=1955428)||never |1 |

## metrics

This is a built-in ping that is assembled out of the box by the Glean SDK.

See the Glean SDK documentation for the [`metrics` ping](https://mozilla.github.io/glean/book/user/pings/metrics.html).

This ping includes the [client id](https://mozilla.github.io/glean/book/user/pings/index.html#the-client_info-section).

**Data reviews for this ping:**

- <https://bugzilla.mozilla.org/show_bug.cgi?id=1512938#c3>
- <https://bugzilla.mozilla.org/show_bug.cgi?id=1557048#c13>

**Bugs related to this ping:**

- <https://bugzilla.mozilla.org/1512938>

**Reasons this ping may be sent:**

- `overdue`: The last ping wasn't submitted on the current calendar day, but it's after
      4am, so this ping submitted immediately

- `reschedule`: A ping was just submitted. This ping was rescheduled for the next calendar
      day at 4am.

- `today`: The last ping wasn't submitted on the current calendar day, but it is
      still before 4am, so schedule to send this ping on the current calendar
      day at 4am.

- `tomorrow`: The last ping was already submitted on the current calendar day, so
      schedule this ping for the next calendar day at 4am.

- `upgrade`: This ping was submitted at startup because the application was just
      upgraded.


All Glean pings contain built-in metrics in the [`ping_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-ping_info-section) and [`client_info`](https://mozilla.github.io/glean/book/user/pings/index.html#the-client_info-section) sections.

In addition to those built-in metrics, the following metrics are added to the ping:

| Name | Type | Description | Data reviews | Extras | Expiration | [Data Sensitivity](https://wiki.mozilla.org/Firefox/Data_Collection) |
| --- | --- | --- | --- | --- | --- | --- |
| glean.database.rkv_load_error |[string](https://mozilla.github.io/glean/book/user/metrics/string.html) |If there was an error loading the RKV database, record it.  |[Bug 1815253](https://bugzilla.mozilla.org/show_bug.cgi?id=1815253)||never |1 |
| glean.database.size |[memory_distribution](https://mozilla.github.io/glean/book/user/metrics/memory_distribution.html) |The size of the database file at startup.  |[Bug 1656589](https://bugzilla.mozilla.org/show_bug.cgi?id=1656589#c7)||never |1 |
| glean.database.write_time |[timing_distribution](https://mozilla.github.io/glean/book/user/metrics/timing_distribution.html) |The time it takes for a write-commit for the Glean database.  |[Bug 1896193](https://bugzilla.mozilla.org/show_bug.cgi?id=1896193#c4)||never |1 |
| glean.error.io |[counter](https://mozilla.github.io/glean/book/user/metrics/counter.html) |The number of times we encountered an IO error when writing a pending ping to disk.  |[Bug 1686233](https://bugzilla.mozilla.org/show_bug.cgi?id=1686233#c2)||never |1 |
| glean.error.preinit_tasks_overflow |[counter](https://mozilla.github.io/glean/book/user/metrics/counter.html) |The number of tasks that overflowed the pre-initialization buffer. Only sent if the buffer ever overflows.  In Version 0 this reported the total number of tasks enqueued.  |[Bug 1609482](https://bugzilla.mozilla.org/show_bug.cgi?id=1609482#c3)||never |1 |
| glean.upload.deleted_pings_after_quota_hit |[counter](https://mozilla.github.io/glean/book/user/metrics/counter.html) |The number of pings deleted after the quota for the size of the pending pings directory or number of files is hit. Since quota is only calculated for the pending pings directory, and deletion request ping live in a different directory, deletion request pings are never deleted.  |[Bug 1601550](https://bugzilla.mozilla.org/show_bug.cgi?id=1601550#c3)||never |1 |
| glean.upload.discarded_exceeding_pings_size |[memory_distribution](https://mozilla.github.io/glean/book/user/metrics/memory_distribution.html) |The size of pings that exceeded the maximum ping size allowed for upload.  |[Bug 1597761](https://bugzilla.mozilla.org/show_bug.cgi?id=1597761#c10)||never |1 |
| glean.upload.in_flight_pings_dropped |[counter](https://mozilla.github.io/glean/book/user/metrics/counter.html) |How many pings were dropped because we found them already in-flight.  |[Bug 1816401](https://bugzilla.mozilla.org/show_bug.cgi?id=1816401)||never |1 |
| glean.upload.missing_send_ids |[counter](https://mozilla.github.io/glean/book/user/metrics/counter.html) |How many ping upload responses did we not record as a success or failure (in `glean.upload.send_success` or `glean.upload.send_failue`, respectively) due to an inconsistency in our internal bookkeeping?  |[Bug 1816400](https://bugzilla.mozilla.org/show_bug.cgi?id=1816400)||never |1 |
| glean.upload.pending_pings |[counter](https://mozilla.github.io/glean/book/user/metrics/counter.html) |The total number of pending pings at startup. This does not include deletion-request pings.  |[Bug 1665041](https://bugzilla.mozilla.org/show_bug.cgi?id=1665041#c23)||never |1 |
| glean.upload.pending_pings_directory_size |[memory_distribution](https://mozilla.github.io/glean/book/user/metrics/memory_distribution.html) |The size of the pending pings directory upon initialization of Glean. This does not include the size of the deletion request pings directory.  |[Bug 1601550](https://bugzilla.mozilla.org/show_bug.cgi?id=1601550#c3)||never |1 |
| glean.upload.ping_upload_failure |[labeled_counter](https://mozilla.github.io/glean/book/user/metrics/labeled_counters.html) |Counts the number of ping upload failures, by type of failure. This includes failures for all ping types, though the counts appear in the next successfully sent `metrics` ping.  |[Bug 1589124](https://bugzilla.mozilla.org/show_bug.cgi?id=1589124#c1)|<ul><li>status_code_4xx</li><li>status_code_5xx</li><li>status_code_unknown</li><li>unrecoverable</li><li>recoverable</li><li>incapable</li></ul>|never |1 |
| glean.upload.send_failure |[timing_distribution](https://mozilla.github.io/glean/book/user/metrics/timing_distribution.html) |Time needed for a failed send of a ping to the servers and getting a reply back.  |[Bug 1814592](https://bugzilla.mozilla.org/show_bug.cgi?id=1814592#c3)||never |1 |
| glean.upload.send_success |[timing_distribution](https://mozilla.github.io/glean/book/user/metrics/timing_distribution.html) |Time needed for a successful send of a ping to the servers and getting a reply back  |[Bug 1814592](https://bugzilla.mozilla.org/show_bug.cgi?id=1814592#c3)||never |1 |
| glean.validation.foreground_count |[counter](https://mozilla.github.io/glean/book/user/metrics/counter.html) |On mobile, the number of times the application went to foreground.  |[Bug 1683707](https://bugzilla.mozilla.org/show_bug.cgi?id=1683707#c2)||never |1 |
| glean.validation.pings_submitted |[labeled_counter](https://mozilla.github.io/glean/book/user/metrics/labeled_counters.html) |A count of the built-in pings submitted, by ping type.  This metric appears in both the metrics and baseline pings.  - On the metrics ping, the counts include the number of pings sent since   the last metrics ping (including the last metrics ping) - On the baseline ping, the counts include the number of pings send since   the last baseline ping (including the last baseline ping)  Note: Previously this also recorded the number of submitted custom pings. Now it only records counts for the Glean built-in pings.  |[Bug 1586764](https://bugzilla.mozilla.org/show_bug.cgi?id=1586764#c3)||never |1 |
| glean.validation.shutdown_dispatcher_wait |[timing_distribution](https://mozilla.github.io/glean/book/user/metrics/timing_distribution.html) |Time waited for the dispatcher to unblock during shutdown. Most samples are expected to be below the 10s timeout used.  |[Bug 1828066](https://bugzilla.mozilla.org/show_bug.cgi?id=1828066#c7)||never |1 |
| glean.validation.shutdown_wait |[timing_distribution](https://mozilla.github.io/glean/book/user/metrics/timing_distribution.html) |Time waited for the uploader at shutdown.  |[Bug 1814592](https://bugzilla.mozilla.org/show_bug.cgi?id=1814592#c3)||never |1 |

Data categories are [defined here](https://wiki.mozilla.org/Firefox/Data_Collection).

<!-- AUTOGENERATED BY glean_parser v17.1.0. DO NOT EDIT. -->

