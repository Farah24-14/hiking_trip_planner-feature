package com.usj.hikingplanner.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())

/** e.g. "Sat, 15 Nov 2026" */
fun LocalDate.pretty(): String = format(dateFormatter)

/** The Material date picker works in UTC milliseconds; these convert to and from LocalDate. */
fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun utcMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

fun Long.toLocalDateTimeText(): String =
    Instant.ofEpochMilli(this).atZone(java.time.ZoneId.systemDefault()).toLocalDate().pretty()
