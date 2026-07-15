package com.precon.mhsclubs.model

/**
 * User roles determined at sign-in by email domain:
 * - Student: @students.mcpasd.k12.wi.us
 * - Teacher: @mcpasd.k12.wi.us (auto-admin of all clubs)
 *
 * Student Leader is a per-club flag on Membership, not a top-level role.
 */
enum class UserRole(val label: String) {
    Student("student"),
    Teacher("teacher");

    companion object {
        fun fromLabel(label: String): UserRole =
            entries.find { it.label == label }
                ?: throw IllegalArgumentException("Unknown UserRole label: $label")
    }
}
