package com.precon.mhsclubs.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a user's membership in a club.
 *
 * Represents a membership record stored in NocoDB.
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
    object Advisor : MembershipRole("advisor")
    object StudentLeader : MembershipRole("student_leader")

    companion object {
        fun fromValue(value: String): MembershipRole =
            when (value.lowercase()) {
                "member" -> Member
                "advisor" -> Advisor
                "student_leader" -> StudentLeader
                else -> throw IllegalArgumentException("Unknown MembershipRole: $value")
            }
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
        fun fromValue(value: String): MembershipStatus =
            when (value.lowercase()) {
                "pending" -> Pending
                "active" -> Active
                "revoked" -> Revoked
                else -> throw IllegalArgumentException("Unknown MembershipStatus: $value")
            }
    }

    override fun toString(): String = value
}
