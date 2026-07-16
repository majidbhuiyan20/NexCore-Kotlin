package com.matox.nexcore.domain.model

/**
 * Domain model for the greeting block shown on the dashboard.
 */
data class UserGreeting(
    val userName: String,
    val greeting: String,
    val tagline: String,
    val detail: String,
    val subtitle: String,
)