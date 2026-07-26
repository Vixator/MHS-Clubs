package com.precon.mhsclubs.data

import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.models.Membership
import com.precon.mhsclubs.screens.announcements.Announcement

/** Authenticated, server-backed content shown only for a student's active clubs. */
interface ClubContentApi {
    suspend fun loadClubs(firebaseIdToken: String): List<Club>
    suspend fun loadMemberships(firebaseIdToken: String): List<Membership>
    suspend fun joinClub(firebaseIdToken: String, clubId: String): Membership
    suspend fun leaveClub(firebaseIdToken: String, membershipId: String)
    suspend fun loadEvents(firebaseIdToken: String): List<Event>
    suspend fun loadAnnouncements(firebaseIdToken: String): List<Announcement>
}
