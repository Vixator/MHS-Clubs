# Google Calendar Sync

## Overview

The MHS Clubs backend automatically syncs events from Google Calendar to the app's database (NocoDB). This allows club advisors to manage events in Google Calendar while students see them in the mobile/web app.

## How It Works

### Sync Triggers

1. **Automatic (Student-Initiated)**: When a student opens the Calendar screen, the server automatically syncs their clubs' calendars if they haven't been synced in the last 2 minutes
2. **Manual (Staff-Initiated)**: Staff can force an immediate sync via the `/api/calendar/sync` endpoint

### Sync Operations

The sync performs three operations:

#### 1. Create New Events
When a new event is added to Google Calendar, it's created in the database on the next sync.

#### 2. Update Existing Events
When an event is modified in Google Calendar (title, time, location, etc.), the database record is updated on the next sync.

#### 3. Delete Removed Events ✨ NEW
When an event is deleted from Google Calendar, it's automatically removed from the database on the next sync.

**Important**: The deletion logic only removes events from clubs that were just synced. This prevents accidentally deleting events from other clubs if a partial sync is performed.

## Database Fields

Each event in NocoDB includes:
- `club_id`: Links the event to a specific club
- `google_event_id`: The unique Google Calendar event ID
- `title`: Event name
- `description`: Event details
- `location`: Event location
- `start_time`: Event start (ISO-8601 timestamp)
- `end_time`: Event end (ISO-8601 timestamp)
- `CreatedAt`: When the record was created in NocoDB
- `UpdatedAt`: When the record was last updated

## Sync Result

After each sync, the server returns:
```json
{
  "success": true,
  "data": {
    "calendars": 3,    // Number of club calendars synced
    "created": 2,      // New events added
    "updated": 5,      // Existing events updated
    "deleted": 1       // Events removed (deleted from Google Calendar)
  }
}
```

## Setup Requirements

1. **Service Account**: Set `GOOGLE_CALENDAR_SERVICE_ACCOUNT` environment variable to the path of your Google service account JSON file
2. **Calendar Sharing**: Each club's Google Calendar must be shared with the service account email (with "See all event details" permission)
3. **Club Configuration**: Each club in NocoDB must have its Google Calendar ID stored in the `Calendar` field

## Testing Deletion

To test the deletion feature:

1. Create a test event in a club's Google Calendar
2. Wait for it to sync (check `/api/my/events` to confirm it appears)
3. Delete the event in Google Calendar
4. Trigger a sync (either manually via `/api/calendar/sync` or wait for automatic sync)
5. Check the logs for: `Deleted event '<title>' (Google ID: <id>) from club <clubId>`
6. Verify the event no longer appears in `/api/my/events`

## Logs

Sync operations are logged for debugging:

```
Calendar sync completed: 3 calendars, 2 created, 5 updated, 1 deleted
Deleted event 'Engineering Club Meeting' (Google ID: abc123) from club 442
```

## Technical Details

### Deletion Algorithm

1. Fetch all existing events from NocoDB that have a `google_event_id`
2. Fetch current events from Google Calendar (with `showDeleted=false`)
3. For each club being synced:
   - Track which events still exist in Google Calendar
   - After processing all Google events, delete database events that:
     - Belong to a club that was just synced (prevents cascading deletions)
     - No longer exist in Google Calendar

### Safety Measures

- Only deletes events from clubs explicitly included in the current sync
- Preserves events from clubs not included in the sync request
- Handles partial syncs safely (e.g., when syncing only one student's clubs)

### Edge Cases Handled

- Events without a Google Calendar ID are never deleted (may be manually created)
- Events from clubs without a configured calendar are never deleted
- Concurrent syncs are handled safely (each operates on its own set of clubs)
