package com.precon.mhsclubs

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.precon.mhsclubs.auth.AuthService
import com.precon.mhsclubs.auth.AuthState
import com.precon.mhsclubs.auth.createAuthService
import com.precon.mhsclubs.model.UserRole
import com.precon.mhsclubs.screens.admin.AdminDashboardScreen
import com.precon.mhsclubs.screens.announcements.AnnouncementListScreen
import com.precon.mhsclubs.screens.attendance.AttendanceMember
import com.precon.mhsclubs.screens.attendance.AttendanceScreen
import com.precon.mhsclubs.screens.attendance.AttendanceStatus
import com.precon.mhsclubs.screens.auth.AccountScreen
import com.precon.mhsclubs.screens.auth.LoginScreen
import com.precon.mhsclubs.screens.calendar.CalendarScreen
import com.precon.mhsclubs.screens.clubs.ClubDetailScreen
import com.precon.mhsclubs.screens.clubs.ClubListScreen
import com.precon.mhsclubs.screens.clubs.JoinClubScreen
import com.precon.mhsclubs.screens.events.EventListScreen
import com.precon.mhsclubs.screens.rsvp.RsvpScreen
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.models.Membership
import com.precon.mhsclubs.models.MembershipRole
import com.precon.mhsclubs.models.MembershipStatus
import kotlinx.datetime.Instant

/**
 * Main app component that handles authentication and navigation.
 */
@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppContent()
        }
    }
}

/**
 * Navigation destinations for the app.
 */
sealed class AppScreen {
    object Login : AppScreen()
    object ClubList : AppScreen()
    object ClubDetail : AppScreen()
    object JoinClub : AppScreen()
    object EventList : AppScreen()
    object Calendar : AppScreen()
    object Account : AppScreen()
    object AdminDashboard : AppScreen()
    object Announcements : AppScreen()
    object Attendance : AppScreen()
    object Rsvp : AppScreen()
}

/**
 * Main app content with authentication flow and navigation.
 */
@Composable
fun AppContent() {
    // Create auth service
    val authService: AuthService = remember { createAuthService() }
    
    // Collect auth state
    val authState by authService.authState.collectAsState()
    
    // Track the current screen and navigation stack
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Login) }
    var selectedClubId by remember { mutableStateOf<String?>(null) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    
    // Track temporary state for screens
    var showJoinClub by remember { mutableStateOf(false) }
    var showEventDetail by remember { mutableStateOf(false) }
    var showRsvp by remember { mutableStateOf(false) }
    var showAttendance by remember { mutableStateOf(false) }
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignedIn -> {
                val userRole = authService.currentUser?.role
                // Navigate based on user role
                currentScreen = when (userRole) {
                    UserRole.Teacher -> AppScreen.AdminDashboard
                    UserRole.Student -> AppScreen.ClubList
                    null -> AppScreen.Login
                }
            }
            is AuthState.SignedOut -> {
                currentScreen = AppScreen.Login
                showJoinClub = false
                showEventDetail = false
                showRsvp = false
                showAttendance = false
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
        showJoinClub = true
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
    
    val navigateToAdmin: () -> Unit = {
        currentScreen = AppScreen.AdminDashboard
    }
    
    val navigateToAnnouncements: () -> Unit = {
        currentScreen = AppScreen.Announcements
    }
    
    val navigateToAttendance: (String) -> Unit = { eventId ->
        selectedEventId = eventId
        showAttendance = true
    }
    
    val navigateToRsvp: (String) -> Unit = { eventId ->
        selectedEventId = eventId
        showRsvp = true
    }
    
    val navigateBack: () -> Unit = {
        when (currentScreen) {
            AppScreen.ClubDetail -> currentScreen = AppScreen.ClubList
            AppScreen.EventList -> currentScreen = AppScreen.ClubList
            AppScreen.Calendar -> currentScreen = AppScreen.ClubList
            AppScreen.Announcements -> currentScreen = AppScreen.ClubList
            AppScreen.AdminDashboard -> currentScreen = AppScreen.ClubList
            AppScreen.Account -> currentScreen = AppScreen.ClubList
            else -> currentScreen = AppScreen.Login
        }
        showJoinClub = false
        showEventDetail = false
        showRsvp = false
        showAttendance = false
    }
    
    // Render the current screen
    when (currentScreen) {
        AppScreen.Login -> {
            LoginScreen(
                authService = authService,
                onSignedIn = { role ->
                    currentScreen = when (role) {
                        UserRole.Teacher -> AppScreen.AdminDashboard
                        UserRole.Student -> AppScreen.ClubList
                    }
                },
                onError = { error ->
                    // Show error
                }
            )
        }
        
        AppScreen.ClubList -> {
            ClubListScreen(
                onAccountClick = navigateToAccount,
                onClubClick = navigateToClubDetail,
                onJoinClubClick = navigateToJoinClub
            )
        }
        
        AppScreen.ClubDetail -> {
            selectedClubId?.let { clubId ->
                ClubDetailScreen(
                    club = getSampleClub(clubId),
                    membership = getSampleMembership(clubId),
                    userRole = authService.currentUser?.role ?: UserRole.Student,
                    onBackClick = navigateBack,
                    onJoinClick = navigateToJoinClub,
                    onLeaveClick = { /* Handle leave club */ },
                    onEventClick = { eventId ->
                        selectedEventId = eventId
                        showEventDetail = true
                    }
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
                events = getSampleEvents(),
                onEventClick = { eventId ->
                    selectedEventId = eventId
                    showRsvp = true
                },
                onBackClick = navigateBack
            )
        }
        
        AppScreen.Calendar -> {
            CalendarScreen(
                events = getSampleEvents(),
                onEventClick = { eventId ->
                    selectedEventId = eventId
                    showRsvp = true
                },
                onBackClick = navigateBack
            )
        }
        
        AppScreen.Account -> {
            AccountScreen(
                authService = authService,
                onSignedOut = { navigateToClubList() }
            )
        }
        
        AppScreen.AdminDashboard -> {
            AdminDashboardScreen(
                clubCount = 5,
                memberCount = 120,
                eventCount = 8,
                onCreateClub = { /* Navigate to create club */ },
                onManageClubs = navigateToClubList,
                onManageMembers = { /* Navigate to members */ },
                onCreateEvent = { /* Navigate to create event */ },
                onManageEvents = navigateToEventList,
                onSyncData = { /* Trigger sync */ }
            )
        }
        
        AppScreen.Announcements -> {
            AnnouncementListScreen(
                announcements = getSampleAnnouncements(),
                onAnnouncementClick = { /* Show announcement detail */ },
                onBackClick = navigateBack
            )
        }
    }
    
    // Modal screens
    if (showJoinClub) {
        JoinClubScreen(
            onBackClick = { showJoinClub = false },
            onJoinClick = { code ->
                // Handle join club
                showJoinClub = false
            }
        )
    }
    
    if (showRsvp && selectedEventId != null) {
        RsvpScreen(
            event = getSampleEvent(selectedEventId!!),
            onRsvp = { status ->
                // Handle RSVP
                showRsvp = false
            }
        )
    }
    
    if (showAttendance && selectedEventId != null) {
        AttendanceScreen(
            eventTitle = getSampleEvent(selectedEventId!!).title,
            members = getSampleAttendanceMembers(),
            isTeacher = authService.currentUser?.role == UserRole.Teacher,
            onMarkAttendance = { userId, status ->
                // Handle attendance marking
            }
        )
    }
}

// Sample data generators for preview/demo purposes
@Composable
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

@Composable
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

@Composable
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
            googleCalendarSynced = true,
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
            googleCalendarSynced = false,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        )
    )
}

@Composable
private fun getSampleEvent(eventId: String): Event {
    return getSampleEvents().find { it.id == eventId } ?: getSampleEvents()[0]
}

@Composable
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

@Composable
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
