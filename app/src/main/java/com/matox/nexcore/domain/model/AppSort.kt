package com.matox.nexcore.domain.model

/**
 * Sort options for the App Manager list. The [label] is what
 * shows up in the dropdown.
 */
enum class AppSort(val label: String) {
    NAME_ASC("Name (A→Z)"),
    NAME_DESC("Name (Z→A)"),
    SIZE_DESC("Size (Largest first)"),
    SIZE_ASC("Size (Smallest first)"),
    DATE_DESC("Recently updated"),
    DATE_ASC("Least recently updated"),
}
