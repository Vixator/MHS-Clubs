# MHS Clubs — Codebase Documentation & Agent Guide

This document contains authoritative details about the codebase's purpose, goals, critical files, data schema, visual identity, and component patterns. Use this as your single source of truth when modifying or extending this repository.

---

## 1. Product Purpose & Goals

**MHS Clubs** is a Kotlin Multiplatform club directory for Android, iOS, and Web, with a Ktor API. It allows students and staff at Middleton High School (MHS) to discover clubs, manage memberships, schedule club events, share announcements, RSVP to events, and record attendance.

### Core Goals and Features
1. **Authenticated Club Discovery, Search, and Joining**: Students can browse all active school clubs, search, and join or leave them.
2. **Calendar & Event Timeline**: An in-app calendar to view meetings and events without requiring user-facing Google Calendar OAuth.
3. **RSVP & Attendance**: Students can RSVP (Going/Not Going) to scheduled events before they start. Advisers (Teachers) can track student attendance (Present, Absent, Late).
4. **Club Announcements**: Real-time announcements from advisers or designated student leaders.
5. **Teacher/Adviser Administration**: Global administration across all clubs for teachers and club-specific administration for designated student leaders.
6. **Strict Authentication boundaries**: Firebase Google Sign-In linked to personal Firebase projects. Enforces `@students.mcpasd.k12.wi.us` (Student role) and `@mcpasd.k12.wi.us` (Teacher Administrator role) domains.

---

## 2. Critical Files & Architecture

The project consists of three main modules:
- **`server/`**: A containerized Ktor API server acting as an adapter for NocoDB, Google Calendar, and Firebase verification. There is no SQL database container (no PostgreSQL/SQLite).
- **`app/shared/`**: Shared Compose Multiplatform UI code (screens, themes, models, and shared logic) for Android, Web, and iOS.
- **`app/webApp/`**: Kotlin/JS wrapper and static web resources serving the web application.

### Most Critical Files
- **`server/src/main/kotlin/com/precon/mhsclubs/Application.kt`**: Main Ktor entry point, defining routes, authentication verifiers, CORS configuration, and third-party API integration.
- **`server/src/main/kotlin/com/precon/mhsclubs/Environment.kt`**: Helper that automatically loads project-root `.env` by walking upwards from the current directory.
- **`server/src/main/kotlin/com/precon/mhsclubs/services/NocoDbClient.kt`**: Client responsible for all NocoDB API operations.
- **`server/src/main/kotlin/com/precon/mhsclubs/services/GoogleCalendarSyncService.kt`**: Background sync service that mirrors Google Calendar events to NocoDB using a service account.
- **`app/shared/src/commonMain/kotlin/com/precon/mhsclubs/App.kt`**: The main Compose Multiplatform entry point and navigation coordinator.
- **`app/shared/src/commonMain/kotlin/com/precon/mhsclubs/ui/MhsClubsTheme.kt`**: Core design system containing the official MHS Clubs color palette, typography scales, and shapes.
- **`app/shared/src/commonMain/kotlin/com/precon/mhsclubs/ui/FigmaComponents.kt`**: Collection of pixel-perfect UI widgets (cards, segmented controls, bottom navigation, textfields, pills, etc.).

---

## 3. Visual Identity & Design System

The visual design system of MHS Clubs is strictly defined in code. Every layout must adhere to these existing values.

### Color Tokens
Layered, near-black surfaces dominate the UI. Tan is reserved for precise accents.

*   **Background (Ink)**: `Color(0xFF0F0F0F)`
*   **Surface Cards**: `Color(0xFF171717)`
*   **Surface Raised (Segmented Controls/Bridges)**: `Color(0xFF1F1F1F)`
*   **Surface Input**: `Color(0xFF242424)`
*   **Hairline Boarders**: `Color(0xFF2C2C2C)`
*   **Hairline Strong**: `Color(0xFF3D3A36)`
*   **Accent (Tan)**: `Color(0xFFC9A77F)` (Lightened to pass WCAG AA on dark surfaces)
*   **Accent Pressed (TanPressed)**: `Color(0xFFD8BC9A)`
*   **Primary Text (TextPrimary)**: `Color(0xFFF4F3F1)`
*   **Secondary Text (TextSecondary)**: `Color(0xFFA8A29B)`
*   **Tertiary Text (TextTertiary)**: `Color(0xFF6F6A64)` (For inactive dates/disabled metadata)
*   **Maroon (Brand Tertiary)**: `Color(0xFF991B1E)`
*   **Maroon Surface**: `Color(0xFF2A1315)`
*   **Danger / Error**: `Color(0xFFFF5A5F)`
*   **Danger Surface**: `Color(0xFF3A1B1D)`

### Typography Scale
A clean seven-step type scale is utilized with negative letter spacing on larger display sizes:

| Text Style | Weight | Font Size | Line Height | Letter Spacing |
|---|---|---|---|---|
| `displayLarge` | ExtraBold | 40.sp | 44.sp | -1.2.sp |
| `headlineMedium` | Bold | 28.sp | 34.sp | -0.6.sp |
| `titleLarge` | Bold | 20.sp | 26.sp | -0.3.sp |
| `titleMedium` | SemiBold | 17.sp | 22.sp | -0.1.sp |
| `titleSmall` | SemiBold | 15.sp | 20.sp | 0.sp |
| `bodyLarge` | Normal | 15.sp | 21.sp | 0.sp |
| `bodyMedium` | Normal | 13.sp | 18.sp | 0.sp |
| `bodySmall` | Normal | 11.sp | 15.sp | 0.sp |
| `labelLarge` | Medium | 15.sp | 20.sp | 0.sp |
| `labelMedium` | Medium | 13.sp | 18.sp | 0.1.sp |
| `labelSmall` | Medium | 11.sp | 15.sp | 0.4.sp |

### Shape Tokens
*   **`extraSmall`**: `RoundedCornerShape(8.dp)`
*   **`small`**: `RoundedCornerShape(12.dp)`
*   **`medium`**: `RoundedCornerShape(16.dp)` (Standard surface card size)
*   **`large`**: `RoundedCornerShape(20.dp)`
*   **`extraLarge`**: `RoundedCornerShape(28.dp)`
*   **`Stadium`**: `RoundedCornerShape(100.dp)` (Buttons, pills, segmented controls, tabs)

---

## 4. UI Component Patterns

To maintain a consistent "physical" tactile feel, build layouts using these pre-designed Composables from `FigmaComponents.kt`:

1.  **`FigmaScreen`**: Container ensuring all pages are bounded with a maximum width of `402.dp` and include horizontal padding.
2.  **`FigmaTitle`**: Consistent styling for titles (displayLarge or compact headlineMedium).
3.  **`FigmaBackLabel`**: Standardized back button with `ChevronLeft`.
4.  **`FigmaPill`**: Compact metadata chips (for days, times, and categories).
5.  **`FigmaActionButton`**: Flat tactile action buttons with a physical `.pressable(...)` scale effect on press.
6.  **`FigmaCard`**: High-fidelity dark cards with 1.dp hairline border (`outlineVariant`).
7.  **`FigmaSegmentedControl`**: Pill-shaped horizontal selector for page sub-tabs.
8.  **`FigmaBottomNavigation`**: Main app bottom tabs navigator (Clubs, Calendar, Updates, Account).
9.  **`FigmaOutlinedTextField`**: Uniform style for inputs with helper/error text support.
10. **`FigmaMonogram`**: Circular profile fallback displaying initials if an avatar is absent.
11. **`pressable` Modifier**: Shrinks any pressable surface slightly (`0.97f`) on press to simulate physical clicks.

---

## 5. NocoDB Schema Contract & Constraints

Data representation is highly structured. Never invent fields or use alternative spellings.

| Table Name | Critical Columns & Enums / Constraints |
|---|---|
| **`clubs`** | `Id` (Primary Key), `sheetSourceId`, `name`, `code`, `description`, `logoUrl`, `category`, `meetingDay`, `meetingTime`, `meetingLocation`, `Calendar` (Google Calendar ID), `contactEmail`, `isActive`. |
| **`users`** | `Id`, `CreatedAt`, `display_name`, `firebase_uid`, `email`, `role` (`Student`, `Teacher`), `avatar_url`. |
| **`memberships`** | `id`, `firebase_uid`, `club_id` (references `clubs.Id`), `status` (`pending`, `active`, `revoked`), `is_club_admin` (Boolean), `role` (`member`, `advisor`, `student_leader`). |
| **`events`** | `id`, `club_id` (references `clubs.Id`), `google_event_id`, `title`, `description`, `location`, `start_time` (Instant), `end_time` (Instant), `UpdatedAt` (built-in). |
| **`rsvps`** | `id`, `event_id`, `firebase_uid`, `status` (`yes`, `no`, `maybe`). |
| **`attendance`** | `id`, `club_id`, `event_id`, `user_id`, `status` (`present`, `absent`, `late`), `recorded_at`. |
| **`announcements`**| `id`, `club_id`, `title`, `content`, `author_name`, `is_active`, `CreatedAt`. Do not add any links or message fields. |

### Domain Validation Rules
*   Student domain: `@students.mcpasd.k12.wi.us` (Mapped to role `Student`)
*   Staff domain: `@mcpasd.k12.wi.us` (Mapped to role `Teacher`)

---

## 6. Native Product Shapes & Structure

Instead of standard visual sections or landing page templates, let the product's native structures govern page design:
-   **Club List / Directory**: Built as a directory of cards, highlighting club titles, active statuses, and upcoming meeting times.
-   **Calendar / Timeline**: Chronological events view highlighting upcoming events first.
-   **Announcements / Feed**: A structured timeline view of real announcements sorted chronologically using `CreatedAt` or fallback fields.

---

## Frequently Used Functions

- **Write**: `Write(file_path, content)` - Create or overwrite files with specific content
- **Edit**: `Edit(file_path, old_string, new_string)` - Perform exact string replacement in files
- **Read**: `Read(file_path)` - Read file content for inspection or modification
- **Glob**: `Glob(pattern)` - Find files matching a pattern
- **Grep**: `Grep(pattern, path, glob, type)` - Search for content in files
- **TaskCreate**: `TaskCreate(subject, description)` - Track progress on development tasks
- **Workflow**: `Workflow(script)` - Orchestrate complex multi-step development processes
