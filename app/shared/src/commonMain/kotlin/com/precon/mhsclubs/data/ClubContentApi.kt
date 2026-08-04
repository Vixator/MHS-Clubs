package com.precon.mhsclubs.data

import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.models.Membership
import com.precon.mhsclubs.screens.announcements.Announcement
import com.precon.mhsclubs.models.AttendanceStatus
import com.precon.mhsclubs.models.Attendance
import com.precon.mhsclubs.screens.rsvp.Rsvp

/** Authenticated, server-backed content shown only for a student's active clubs. */
interface ClubContentApi {
    suspend fun loadClubs(firebaseIdToken: String): List<Club>
    suspend fun loadMemberships(firebaseIdToken: String): List<Membership>
    suspend fun joinClub(firebaseIdToken: String, clubId: String): Membership
    suspend fun leaveClub(firebaseIdToken: String, clubId: String)
    suspend fun loadEvents(firebaseIdToken: String): List<Event>
    suspend fun loadAnnouncements(firebaseIdToken: String): List<Announcement>
    suspend fun loadRsvps(firebaseIdToken: String): List<Rsvp>
    suspend fun respondToRsvp(firebaseIdToken: String, eventId: String, status: String)
    suspend fun loadMemberCount(firebaseIdToken: String, clubId: String): Int
    suspend fun loadAttendance(firebaseIdToken: String): List<Attendance>
    suspend fun loadAttendanceRoster(firebaseIdToken: String, clubId: String, eventId: String): List<ClubMember>
    suspend fun saveAttendance(firebaseIdToken: String, clubId: String, eventId: String, records: List<AttendanceUpdate>)
}

data class ClubMember(val userId: String, val displayName: String, val email: String, val status: AttendanceStatus?)

data class AttendanceUpdate(val userId: String, val status: AttendanceStatus)
