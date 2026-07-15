package com.precon.mhsclubs.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a user's membership in a club.
 *
 * Mirrors the `memberships` table in the SQLDelight schema.
 * A membership tracks how a user is associated with a club,
 * including their role within it and current status.
 */
@Serializable
data class Membership(
    val id: String,
    val userId: String,
    val clubId: String,
    val role: MembershipRole,
    val status: MembershipStatus,
    val joinedAt: Instant?,
    val leaderGrantedAt: Instant?,
    val revokedAt: Instant?,
)

/**
 * The role a user holds within a club membership.
 *
 * Maps to the `role` column in the `memberships` table.
 */
sealed class MembershipRole(val value: String) {
    object Member : MembershipRole("member")
    object Officer : MembershipRole("officer")
    object StudentLeader : MembershipRole("student_leader")

    companion object {
        private val BY_VALUE = entries.associateBy(MembershipRole::value)

        fun fromValue(value: String): MembershipRole =
            BY_VALUE[value] ?: throw IllegalArgumentException("Unknown MembershipRole: $value")
    }

    override fun toString(): String = value
}

/**
 * The current status of a club membership.
 *
 * Maps to the `status` column in the `memberships` table.
 */
sealed class MembershipStatus(val value: String) {
    object Pending : MembershipStatus("pending")
    object Active : MembershipStatus("active")
    object Revoked : MembershipStatus("revoked")

    companion object {
        private val BY_VALUE = entries.associateBy(MembershipStatus::value)

        fun fromValue(value: String): MembershipStatus =
            BY_VALUE[value] ?: throw IllegalArgumentException("Unknown MembershipStatus: $value")
    }

    override fun toString(): String = value
}
