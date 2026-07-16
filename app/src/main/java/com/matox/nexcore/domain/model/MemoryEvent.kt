package com.matox.nexcore.domain.model

/**
 * One entry in the RAM detail screen's "Recent Memory Events"
 * timeline.
 *
 * - [id] — stable identifier; the timeline keys on it for
 *   animation stability.
 * - [timestampMs] — `System.currentTimeMillis()` at the moment
 *   the event was emitted. The UI converts to "now / N m ago /
 *   N h ago" at render time.
 * - [type] — drives the icon shown in the timeline dot.
 * - [title] / [subtitle] — primary and secondary labels.
 * - [accent] — semantic accent that tints the timeline dot and
 *   icon. Maps to a Compose color via `MetricAccent.toColor()`.
 */
data class MemoryEvent(
    val id: String,
    val timestampMs: Long,
    val type: MemoryEventType,
    val title: String,
    val subtitle: String,
    val accent: MetricAccent,
)

/**
 * Categorises [MemoryEvent] for icon selection and analytics.
 *
 *  - [APP_OPENED] — a package just appeared in the top-apps list.
 *  - [LARGE_ALLOCATION] — RAM usage jumped > 0.5 GB between polls.
 *  - [BACKGROUND_CLEANUP] — system reclaimed memory (heuristic:
 *    `lowMemory` flipped false OR sustained drop in usage).
 *  - [MEMORY_PRESSURE_CHANGE] — usage crossed the 70 % threshold
 *    in either direction.
 *  - [LOW_MEMORY_WARNING] — the kernel's `lowMemory` flag flipped
 *    true.
 */
enum class MemoryEventType {
    APP_OPENED,
    LARGE_ALLOCATION,
    BACKGROUND_CLEANUP,
    MEMORY_PRESSURE_CHANGE,
    LOW_MEMORY_WARNING,
}