package com.precon.mhsclubs.data

import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.screens.announcements.Announcement

/** Authenticated, server-backed content shown only for a student's active clubs. */
interface ClubContentApi {
    suspend fun loadEvents(firebaseIdToken: String): List<Event>
    suspend fun loadAnnouncements(firebaseIdToken: String): List<Announcement>
}
