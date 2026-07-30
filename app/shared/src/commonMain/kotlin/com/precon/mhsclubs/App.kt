package com.precon.mhsclubs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.precon.mhsclubs.auth.AuthService
import com.precon.mhsclubs.auth.AuthState
import com.precon.mhsclubs.auth.createAuthService
import com.precon.mhsclubs.data.ClubContentApi
import com.precon.mhsclubs.data.AttendanceUpdate
import com.precon.mhsclubs.data.ClubMember
import com.precon.mhsclubs.model.UserRole
import com.precon.mhsclubs.screens.admin.AdminDashboardScreen
import com.precon.mhsclubs.screens.announcements.AnnouncementListScreen
import com.precon.mhsclubs.screens.attendance.AttendanceMember
import com.precon.mhsclubs.screens.attendance.AttendanceScreen
import com.precon.mhsclubs.screens.attendance.StudentAttendanceScreen
import com.precon.mhsclubs.models.Attendance
import com.precon.mhsclubs.models.AttendanceStatus
import com.precon.mhsclubs.screens.auth.AccountScreen
import com.precon.mhsclubs.screens.auth.LoginScreen
import com.precon.mhsclubs.screens.calendar.CalendarScreen
import com.precon.mhsclubs.screens.clubs.ClubDetailScreen
import com.precon.mhsclubs.screens.clubs.ClubListScreen
import com.precon.mhsclubs.screens.clubs.JoinClubScreen
import com.precon.mhsclubs.screens.events.EventListScreen
import com.precon.mhsclubs.screens.rsvp.RsvpScreen
import com.precon.mhsclubs.ui.MhsClubsTheme
import com.precon.mhsclubs.ui.FigmaBottomNavigation
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.models.Membership
import com.precon.mhsclubs.models.MembershipRole
import com.precon.mhsclubs.models.MembershipStatus
import com.precon.mhsclubs.models.recurringMeetings
import com.precon.mhsclubs.screens.rsvp.RsvpStatus
import kotlinx.datetime.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Main app component that handles authentication and navigation.
 */
@Composable
fun App(
    authServiceOverride: AuthService? = null,
    clubContentApi: ClubContentApi? = null,
    notificationsEnabled: Boolean = false,
    onNotificationsChange: (Boolean) -> Unit = {}
) {
    MhsClubsTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AppContent(authServiceOverride, clubContentApi, notificationsEnabled, onNotificationsChange)
        }
    }
}

/**
 * Navigation destinations for the app.
 */
sealed class AppScreen {
    object Login : AppScreen()
    object ClubList : AppScreen()
    object ClubDirectory : AppScreen()
    object ClubDetail : AppScreen()
    object JoinClub : AppScreen()
    object EventList : AppScreen()
    object Calendar : AppScreen()
    object Account : AppScreen()
    object StudentAttendance : AppScreen()
    object AdminDashboard : AppScreen()
    object Announcements : AppScreen()
    object Attendance : AppScreen()
    object Rsvp : AppScreen()
}

private data class ContentSnapshot(
    val clubs: List<com.precon.mhsclubs.model.Club>,
    val memberships: List<Membership>,
    val events: List<Event>,
    val announcements: List<com.precon.mhsclubs.screens.announcements.Announcement>,
    val rsvps: List<com.precon.mhsclubs.screens.rsvp.Rsvp>
)

/**
 * Main app content with authentication flow and navigation.
 */
@Composable
fun AppContent(
    authServiceOverride: AuthService? = null,
    clubContentApi: ClubContentApi? = null,
    notificationsEnabled: Boolean = false,
    onNotificationsChange: (Boolean) -> Unit = {}
) {
    // Create auth service
    val authService: AuthService = remember(authServiceOverride) { authServiceOverride ?: createAuthService() }
    
    // Collect auth state
    val authState by authService.authState.collectAsState(initial = AuthState.SignedOut)
    
    // Track the current screen and navigation stack
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Login) }
    var selectedClubId by remember { mutableStateOf<String?>(null) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    
    // Track temporary state for screens
    var showJoinClub by remember { mutableStateOf(false) }
    var showEventDetail by remember { mutableStateOf(false) }
    var showRsvp by remember { mutableStateOf(false) }
    var showAttendance by remember { mutableStateOf(false) }
    var syncedEvents by remember { mutableStateOf<List<Event>?>(null) }
    var syncedClubs by remember { mutableStateOf<List<com.precon.mhsclubs.model.Club>?>(null) }
    var syncedMemberships by remember { mutableStateOf<List<Membership>?>(null) }
    var syncedAnnouncements by remember { mutableStateOf<List<com.precon.mhsclubs.screens.announcements.Announcement>?>(null) }
    var syncedRsvps by remember { mutableStateOf<List<com.precon.mhsclubs.screens.rsvp.Rsvp>>(emptyList()) }
    var attendanceMembers by remember { mutableStateOf<List<ClubMember>>(emptyList()) }
    var syncedStudentAttendance by remember { mutableStateOf<List<Attendance>?>(null) }
    var selectedClubMemberCount by remember { mutableStateOf<Int?>(null) }
    var studentAttendanceError by remember { mutableStateOf<String?>(null) }
    var studentAttendanceRefreshKey by remember { mutableStateOf(0) }
    var clubLoadError by remember { mutableStateOf<String?>(null) }
    var contentRefreshKey by remember { mutableStateOf(0) }
    // A Compose effect can be recreated while its parent state settles.  Keep the
    // network request idempotent so a re-composition cannot hammer the API.
    var completedContentLoadKey by remember { mutableStateOf<String?>(null) }
    var joinError by remember { mutableStateOf<String?>(null) }
    var isJoining by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val attendanceSaveMutex = remember { Mutex() }
    val signedInFirebaseUid = (authState as? AuthState.SignedIn)?.user?.firebaseUid

    // Restores an existing browser Firebase session after a refresh. Native implementations
    // either maintain their own listener or return their current state.
    LaunchedEffect(authService) {
        authService.refreshUser()
    }

    LaunchedEffect(signedInFirebaseUid, clubContentApi, contentRefreshKey) {
        val api = clubContentApi ?: return@LaunchedEffect
        val requestKey = "$signedInFirebaseUid:$contentRefreshKey"
        if (completedContentLoadKey == requestKey) return@LaunchedEffect
        completedContentLoadKey = requestKey
        val token = authService.getIdToken() ?: run {
            if (authState is AuthState.SignedIn) clubLoadError = "Couldn't authenticate to load clubs. Try again."
            return@LaunchedEffect
        }
        if (authState is AuthState.SignedIn) {
            // Club membership is the essential data for the Clubs screen.  Do not let an
            // unrelated optional feed (events, announcements, or RSVPs) erase it.
            runCatching {
                api.loadClubs(token) to api.loadMemberships(token)
            }
                .onSuccess {
                    syncedClubs = it.first
                    syncedMemberships = it.second
                    clubLoadError = null
                }
                .onFailure {
                    if (it is CancellationException) throw it
                    clubLoadError = "Couldn't load your club data. Check your connection and try again."
                }

        }
    }

    // Do not fan out to every NocoDB table while opening My Clubs.  Besides making the
    // initial screen slower, that burst can be rate-limited by NocoDB.  Each feed is
    // requested only when the student opens the screen that needs it.
    LaunchedEffect(currentScreen, signedInFirebaseUid, clubContentApi, contentRefreshKey) {
        if (currentScreen != AppScreen.Calendar) return@LaunchedEffect
        val api = clubContentApi ?: return@LaunchedEffect
        val token = authService.getIdToken() ?: return@LaunchedEffect
        syncedEvents = runCatching { api.loadEvents(token) }.getOrElse { emptyList() }
    }

    LaunchedEffect(currentScreen, signedInFirebaseUid, clubContentApi, contentRefreshKey) {
        if (currentScreen != AppScreen.Announcements) return@LaunchedEffect
        val api = clubContentApi ?: return@LaunchedEffect
        val token = authService.getIdToken() ?: return@LaunchedEffect
        syncedAnnouncements = runCatching { api.loadAnnouncements(token) }.getOrElse { emptyList() }
    }

    LaunchedEffect(showRsvp, signedInFirebaseUid, clubContentApi, contentRefreshKey) {
        if (!showRsvp) return@LaunchedEffect
        val api = clubContentApi ?: return@LaunchedEffect
        val token = authService.getIdToken() ?: return@LaunchedEffect
        syncedRsvps = runCatching { api.loadRsvps(token) }.getOrElse { emptyList() }
    }

    LaunchedEffect(currentScreen, selectedClubId, signedInFirebaseUid, clubContentApi, syncedMemberships) {
        if (currentScreen != AppScreen.ClubDetail) return@LaunchedEffect
        val api = clubContentApi ?: return@LaunchedEffect
        val clubId = selectedClubId ?: return@LaunchedEffect
        selectedClubMemberCount = null
        val token = authService.getIdToken() ?: return@LaunchedEffect
        runCatching { api.loadMemberCount(token, clubId) }.getOrNull()?.let { count ->
            selectedClubMemberCount = count
            syncedClubs = syncedClubs?.map { club ->
                if (club.id == clubId) club.copy(memberCount = count) else club
            }
        }
    }

    LaunchedEffect(currentScreen, signedInFirebaseUid, clubContentApi, studentAttendanceRefreshKey) {
        if (currentScreen != AppScreen.StudentAttendance) return@LaunchedEffect
        val api = clubContentApi ?: return@LaunchedEffect
        val token = authService.getIdToken() ?: run {
            studentAttendanceError = "Couldn't authenticate to load attendance. Try again."
            return@LaunchedEffect
        }
        runCatching { api.loadAttendance(token) }
            .onSuccess {
                syncedStudentAttendance = it
                studentAttendanceError = null
            }
            .onFailure {
                if (it is CancellationException) throw it
                studentAttendanceError = "Couldn't load attendance. Check your connection and try again."
            }
    }
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignedIn -> {
                currentScreen = AppScreen.ClubList
            }
            is AuthState.SignedOut -> {
                currentScreen = AppScreen.Login
                showJoinClub = false
                showEventDetail = false
                showRsvp = false
                showAttendance = false
                selectedClubId = null
                selectedEventId = null
                syncedEvents = null
                syncedClubs = null
                syncedMemberships = null
                syncedAnnouncements = null
                syncedRsvps = emptyList()
                attendanceMembers = emptyList()
                syncedStudentAttendance = null
                selectedClubMemberCount = null
                studentAttendanceError = null
                clubLoadError = null
                completedContentLoadKey = null
                joinError = null
            }
            else -> {
                // Loading or error state - stay on current screen
            }
        }
    }
    
    // Navigation handlers
    val navigateToClubList: () -> Unit = {
        currentScreen = AppScreen.ClubList
        selectedClubId = null
    }
    
    val navigateToClubDetail: (String) -> Unit = { clubId ->
        selectedClubId = clubId
        currentScreen = AppScreen.ClubDetail
    }
    
    val navigateToJoinClub: () -> Unit = {
        currentScreen = AppScreen.ClubDirectory
    }
    
    val navigateToEventList: () -> Unit = {
        currentScreen = AppScreen.EventList
    }
    
    val navigateToCalendar: () -> Unit = {
        currentScreen = AppScreen.Calendar
    }
    
    val navigateToAccount: () -> Unit = {
        currentScreen = AppScreen.Account
    }

    val navigateToStudentAttendance: () -> Unit = {
        syncedStudentAttendance = null
        studentAttendanceError = null
        studentAttendanceRefreshKey++
        currentScreen = AppScreen.StudentAttendance
    }
    
    val navigateToAdmin: () -> Unit = {
        currentScreen = AppScreen.AdminDashboard
    }
    
    val navigateToAnnouncements: () -> Unit = {
        currentScreen = AppScreen.Announcements
    }
    
    val navigateToAttendance: (String) -> Unit = { eventId ->
        val memberIds = syncedMemberships.orEmpty().filter { it.status == MembershipStatus.Active }.map { it.clubId }.toSet()
        val event = (syncedEvents.orEmpty() + recurringMeetings(syncedClubs.orEmpty().filter { it.id in memberIds })).firstOrNull { it.id == eventId }
        val club = event?.let { item -> syncedClubs?.firstOrNull { it.id == item.clubId } }
        if (club?.contactEmail.equals(authService.currentUser?.email, ignoreCase = true)) {
            selectedEventId = eventId
            showAttendance = true
        }
    }
    
    val navigateToRsvp: (String) -> Unit = { eventId ->
        selectedEventId = eventId
        showRsvp = true
    }
    
    val navigateBack: () -> Unit = {
        when (currentScreen) {
            AppScreen.ClubDetail -> currentScreen = AppScreen.ClubList
            AppScreen.ClubDirectory -> currentScreen = AppScreen.ClubList
            AppScreen.EventList -> currentScreen = AppScreen.ClubList
            AppScreen.Calendar -> currentScreen = AppScreen.ClubList
            AppScreen.Announcements -> currentScreen = AppScreen.ClubList
            AppScreen.AdminDashboard -> currentScreen = AppScreen.ClubList
            AppScreen.Account -> currentScreen = AppScreen.ClubList
            AppScreen.StudentAttendance -> currentScreen = AppScreen.Account
            else -> currentScreen = AppScreen.Login
        }
        showJoinClub = false
        showEventDetail = false
        showRsvp = false
        showAttendance = false
    }

    val shouldHandleSystemBack = showJoinClub || showRsvp || showAttendance ||
        (currentScreen != AppScreen.Login && currentScreen != AppScreen.ClubList) ||
        (currentScreen == AppScreen.ClubList && authService.currentUser?.role == UserRole.Staff)

    PlatformBackHandler(enabled = shouldHandleSystemBack) {
        when {
            showJoinClub -> showJoinClub = false
            showRsvp -> showRsvp = false
            showAttendance -> showAttendance = false
            currentScreen == AppScreen.ClubList -> currentScreen = AppScreen.AdminDashboard
            currentScreen == AppScreen.EventList && authService.currentUser?.role == UserRole.Staff -> {
                currentScreen = AppScreen.AdminDashboard
            }
            else -> navigateBack()
        }
    }
    
    val primaryDestination = currentScreen in setOf(
        AppScreen.ClubList,
        AppScreen.Calendar,
        AppScreen.Announcements,
        AppScreen.Account,
        AppScreen.AdminDashboard
    )

    val activeClubIds = syncedMemberships.orEmpty().filter { it.status == MembershipStatus.Active }.map { it.clubId }.toSet()
    /** True until the first club request settles, so the list can show placeholders instead of an empty state. */
    val isLoadingClubs = clubContentApi != null && syncedClubs == null && clubLoadError == null
    val allClubs = syncedClubs ?: if (clubContentApi == null) getSampleClubs() else emptyList()
    val myClubs = allClubs.filter { it.id in activeClubIds }
    val allEvents = ((syncedEvents ?: if (clubContentApi == null) getSampleEvents() else emptyList()) + recurringMeetings(myClubs)).distinctBy { it.id }
    val nextMeetings = allEvents.filter { it.isScheduledMeeting && it.startTime > kotlin.time.Clock.System.now() }
        .groupBy { it.clubId }
        .mapValues { (_, meetings) -> meetings.minBy { it.startTime } }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Individual screens manage their own top bars and scroll containers.
            when (currentScreen) {
        AppScreen.Login -> {
            LoginScreen(
                authService = authService,
                onSignedIn = { currentScreen = AppScreen.ClubList },
                onError = { error ->
                    // Show error
                }
            )
        }
        
        AppScreen.ClubList -> {
            ClubListScreen(
                clubs = myClubs,
                isLoading = isLoadingClubs,
                onJoinClubClick = navigateToJoinClub,
                onClubClick = navigateToClubDetail,
                memberClubIds = activeClubIds,
                nextMeetings = nextMeetings,
                errorMessage = clubLoadError,
                onRetry = { contentRefreshKey++ }
            )
        }
        
        AppScreen.ClubDetail -> {
            selectedClubId?.let { clubId ->
                val club = allClubs.firstOrNull { it.id == clubId } ?: getSampleClub(clubId)
                ClubDetailScreen(
                    club = club,
                    memberCount = if (clubContentApi != null) selectedClubMemberCount else club.memberCount,
                    membership = syncedMemberships?.firstOrNull { it.clubId == clubId },
                    userRole = authService.currentUser?.role ?: UserRole.Student,
                    onBackClick = navigateBack,
                    onJoinClick = {
                        if (isJoining) return@ClubDetailScreen
                        scope.launch {
                            isJoining = true
                            joinError = null
                            val token = authService.getIdToken()
                            if (token == null) {
                                joinError = "Couldn't authenticate to add this club. Please try again."
                                isJoining = false
                                return@launch
                            }
                            runCatching { clubContentApi?.joinClub(token, clubId) ?: getSampleMembership(clubId) }
                                .onSuccess { membership ->
                                    syncedMemberships = syncedMemberships.orEmpty().filterNot { it.clubId == clubId } + membership
                                    contentRefreshKey++
                                }
                                .onFailure {
                                    if (it is CancellationException) throw it
                                    joinError = "Couldn't add this club. Please try again."
                                }
                            isJoining = false
                        }
                    },
                    onLeaveClick = {
                        val membership = syncedMemberships?.firstOrNull { it.clubId == clubId } ?: return@ClubDetailScreen
                        if (isJoining) return@ClubDetailScreen
                        scope.launch {
                            isJoining = true
                            joinError = null
                            val token = authService.getIdToken()
                            if (clubContentApi != null && token == null) {
                                joinError = "Couldn't authenticate to remove this club. Please try again."
                                isJoining = false
                                return@launch
                            }
                            runCatching {
                                if (clubContentApi != null) clubContentApi.leaveClub(token!!, membership.id)
                            }.onSuccess {
                                syncedMemberships = syncedMemberships?.filterNot { it.id == membership.id }
                                contentRefreshKey++
                            }.onFailure {
                                if (it is CancellationException) throw it
                                joinError = "Couldn't remove this club. Please try again."
                            }
                            isJoining = false
                        }
                    },
                    isJoining = isJoining,
                    joinError = joinError
                )
            }
        }
        
        AppScreen.JoinClub -> {
            JoinClubScreen(
                onBackClick = navigateBack,
                onJoinClick = { code ->
                    // Handle join club with code
                    showJoinClub = false
                }
            )
        }
        
        AppScreen.EventList -> {
            EventListScreen(
                events = allEvents,
                onEventClick = { eventId ->
                    selectedEventId = eventId
                    showRsvp = true
                }
            )
        }
        
        AppScreen.Calendar -> {
            CalendarScreen(
                events = allEvents,
                clubs = myClubs,
                onEventClick = { eventId ->
                    selectedEventId = eventId
                    showRsvp = true
                }
            )
        }
        
        AppScreen.Account -> {
            AccountScreen(
                authService = authService,
                memberType = if (allClubs.any { it.contactEmail.equals(authService.currentUser?.email, ignoreCase = true) }) "Adviser" else "Student",
                notificationsEnabled = notificationsEnabled,
                onNotificationsChange = onNotificationsChange,
                onViewAttendance = navigateToStudentAttendance,
                onSignedOut = { currentScreen = AppScreen.Login }
            )
        }

        AppScreen.StudentAttendance -> {
            StudentAttendanceScreen(
                events = allEvents,
                clubs = myClubs,
                attendance = syncedStudentAttendance,
                isLoading = clubContentApi != null && syncedStudentAttendance == null && studentAttendanceError == null,
                errorMessage = studentAttendanceError,
                onBackClick = navigateBack
            )
        }

        AppScreen.ClubDirectory -> {
            ClubListScreen(
                clubs = allClubs,
                isLoading = isLoadingClubs,
                onJoinClubClick = {},
                onClubClick = navigateToClubDetail,
                memberClubIds = activeClubIds,
                nextMeetings = nextMeetings,
                title = "Add Clubs",
                backLabel = "My Clubs",
                onBackClick = navigateBack,
                errorMessage = clubLoadError,
                onRetry = { contentRefreshKey++ }
            )
        }
        
        AppScreen.AdminDashboard -> {
            AdminDashboardScreen(
                onManageClubs = navigateToClubList,
                onManageEvents = navigateToEventList
            )
        }
        
        AppScreen.Announcements -> {
            AnnouncementListScreen(
                announcements = syncedAnnouncements ?: if (clubContentApi == null) getSampleAnnouncements() else emptyList(),
                clubs = myClubs,
                onAnnouncementClick = { /* Show announcement detail */ }
            )
        }
                AppScreen.Attendance, AppScreen.Rsvp -> Unit
            }
        }
    if (primaryDestination) {
        FigmaBottomNavigation(
            selected = when (currentScreen) {
                AppScreen.ClubList -> "Clubs"
                AppScreen.Calendar -> "Calendar"
                AppScreen.Announcements -> "Updates"
                else -> "Account"
            },
            onClubs = navigateToClubList,
            onCalendar = navigateToCalendar,
            onUpdates = navigateToAnnouncements,
            onAccount = navigateToAccount,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 24.dp, vertical = 24.dp).fillMaxWidth().widthIn(max = 402.dp)
        )
    }
    }
    
    // Modal screens
    if (showJoinClub) {
        JoinClubScreen(
            onBackClick = { showJoinClub = false },
            onJoinClick = { name ->
                val club = (syncedClubs ?: getSampleClubs()).firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
                if (club == null) {
                    joinError = "Club name not found. Please check and try again."
                } else {
                    scope.launch {
                        isJoining = true
                        joinError = null
                        val token = authService.getIdToken()
                        val membership = runCatching {
                            if (clubContentApi != null && token != null) clubContentApi.joinClub(token, club.id)
                            else getSampleMembership(club.id)
                        }.getOrElse {
                            joinError = "Unable to join this club. Please try again."
                            isJoining = false
                            return@launch
                        }
                        syncedMemberships = (syncedMemberships ?: emptyList()).filterNot { it.clubId == club.id } + membership
                        selectedClubId = club.id
                        currentScreen = AppScreen.ClubDetail
                        showJoinClub = false
                        isJoining = false
                    }
                }
            },
            isLoading = isJoining,
            errorMessage = joinError
        )
    }
    
    if (showRsvp && selectedEventId != null) {
        allEvents.firstOrNull { it.id == selectedEventId }?.let { event ->
            val club = allClubs.firstOrNull { it.id == event.clubId }
            RsvpScreen(
            event = event,
            currentRsvp = syncedRsvps.firstOrNull { it.eventId == event.id },
            showAttendance = club?.contactEmail.equals(authService.currentUser?.email, ignoreCase = true),
            onRsvp = { status ->
                scope.launch {
                    val token = authService.getIdToken() ?: return@launch
                    val value = if (status == RsvpStatus.Going) "yes" else "no"
                    runCatching { clubContentApi?.respondToRsvp(token, event.id, value) }
                    syncedRsvps = runCatching { clubContentApi?.loadRsvps(token) ?: emptyList() }.getOrDefault(syncedRsvps)
                }
            },
            onAttendanceClick = { navigateToAttendance(event.id) },
            onBackClick = { showRsvp = false }
        ) }
    }
    
    if (showAttendance && selectedEventId != null) {
        allEvents.firstOrNull { it.id == selectedEventId }?.let { event ->
            val club = allClubs.firstOrNull { it.id == event.clubId }
            val isAdviser = club?.contactEmail.equals(authService.currentUser?.email, ignoreCase = true)
            LaunchedEffect(event.id, isAdviser) {
                if (isAdviser) {
                    val token = authService.getIdToken() ?: return@LaunchedEffect
                    attendanceMembers = runCatching { clubContentApi?.loadAttendanceRoster(token, event.clubId, event.id) ?: emptyList() }.getOrDefault(emptyList())
                } else showAttendance = false
            }
            if (isAdviser) AttendanceScreen(
            eventId = event.id,
            eventTitle = event.title,
            members = attendanceMembers.map { AttendanceMember(it.userId, it.displayName, it.email, it.status ?: AttendanceStatus.Absent) },
            isTeacher = true,
            onMarkAttendance = { userId, status ->
                val updatedMembers = attendanceMembers.map { if (it.userId == userId) it.copy(status = status) else it }
                attendanceMembers = updatedMembers
                scope.launch {
                    attendanceSaveMutex.withLock {
                        val token = authService.getIdToken() ?: return@withLock
                        runCatching {
                            clubContentApi?.saveAttendance(
                                token,
                                event.clubId,
                                event.id,
                                updatedMembers.map { AttendanceUpdate(it.userId, it.status ?: AttendanceStatus.Absent) }
                            )
                        }
                    }
                }
            },
            onBackClick = { showAttendance = false }
        ) }
    }
}

// Sample data generators for preview/demo purposes
private fun getSampleClubs(): List<com.precon.mhsclubs.model.Club> = listOf(
    getSampleClub("club1"),
    com.precon.mhsclubs.model.Club(
        id = "club2", sheetSourceId = "sheet_club2", name = "Chess Club",
        description = "Play chess and build your strategy skills.", category = "Games",
        meetingDay = "Tuesday", meetingTime = "3:15 PM", meetingLocation = "Library", code = "CHESS"
    )
)

private fun getSampleClub(clubId: String): com.precon.mhsclubs.model.Club {
    return com.precon.mhsclubs.model.Club(
        id = clubId,
        sheetSourceId = "sheet_$clubId",
        name = "Robotics Club",
        description = "Build and program robots for competitions. We meet weekly to work on projects and learn new skills.",
        logoUrl = null,
        category = "STEM",
        meetingDay = "Monday",
        meetingTime = "3:30 PM - 5:00 PM",
        meetingLocation = "Room 204",
        code = "ROBOT"
    )
}

private fun getSampleMembership(clubId: String): Membership {
    return Membership(
        id = "membership_${clubId}_user1",
        userId = "user1",
        clubId = clubId,
        role = MembershipRole.Member,
        status = MembershipStatus.Active,
        joinedAt = Instant.parse("2024-01-01T00:00:00Z"),
        leaderGrantedAt = null,
        revokedAt = null
    )
}

private fun getSampleEvents(): List<Event> {
    return listOf(
        Event(
            id = "event1",
            clubId = "club1",
            title = "Robotics Competition",
            description = "Annual state robotics competition",
            location = "State Fair Grounds",
            startTime = Instant.parse("2024-03-15T09:00:00Z"),
            endTime = Instant.parse("2024-03-15T17:00:00Z"),
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        ),
        Event(
            id = "event2",
            clubId = "club1",
            title = "Weekly Meeting",
            description = "Regular team meeting to work on projects",
            location = "Room 204",
            startTime = Instant.parse("2024-03-20T15:30:00Z"),
            endTime = Instant.parse("2024-03-20T17:00:00Z"),
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        )
    )
}

private fun getSampleEvent(eventId: String): Event {
    return getSampleEvents().find { it.id == eventId } ?: getSampleEvents()[0]
}

private fun getSampleAttendanceMembers(): List<AttendanceMember> {
    return listOf(
        AttendanceMember(
            userId = "user1",
            displayName = "John Doe",
            email = "john@students.mcpasd.k12.wi.us",
            status = AttendanceStatus.Present
        ),
        AttendanceMember(
            userId = "user2",
            displayName = "Jane Smith",
            email = "jane@students.mcpasd.k12.wi.us",
            status = AttendanceStatus.Late
        ),
        AttendanceMember(
            userId = "user3",
            displayName = "Bob Johnson",
            email = "bob@students.mcpasd.k12.wi.us",
            status = AttendanceStatus.Absent
        )
    )
}

private fun getSampleAnnouncements(): List<com.precon.mhsclubs.screens.announcements.Announcement> {
    return listOf(
        com.precon.mhsclubs.screens.announcements.Announcement(
            id = "announcement1",
            clubId = "club1",
            title = "Robotics Competition Results",
            content = "Congratulations to everyone who participated! We placed 2nd overall and won the Innovation Award.",
            authorId = "teacher1",
            authorName = "Mr. Smith",
            isActive = true,
            postedAt = Instant.parse("2024-03-16T00:00:00Z"),
            updatedAt = Instant.parse("2024-03-16T00:00:00Z")
        ),
        com.precon.mhsclubs.screens.announcements.Announcement(
            id = "announcement2",
            clubId = "club1",
            title = "Next Meeting",
            content = "Our next meeting will be on Monday at 3:30 PM in Room 204.",
            authorId = "student1",
            authorName = "Jane Doe",
            isActive = true,
            postedAt = Instant.parse("2024-03-18T00:00:00Z"),
            updatedAt = Instant.parse("2024-03-18T00:00:00Z")
        )
    )
}

@Preview
@Composable
fun AppPreview() {
    App()
}
