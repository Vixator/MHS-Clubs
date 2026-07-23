package com.precon.mhsclubs.model

/**
 * User roles determined at sign-in by the verified Google email domain:
 * - Student: @students.mcpasd.k12.wi.us
 * - Staff: @mcpasd.k12.wi.us (administrator for all clubs)
 *
 * Student Leader is a per-club flag on Membership, not a top-level role.
 */
enum class UserRole(val label: String) {
    Student("student"),
    Staff("staff");

    companion object {
        fun fromLabel(label: String): UserRole =
            entries.find { it.label == label }
                ?: throw IllegalArgumentException("Unknown UserRole label: $label")
    }
}
