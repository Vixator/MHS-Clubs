# Fix Unresolved Reference in ClubSearchTest.kt

The test file `ClubSearchTest.kt` uses a `fuzzyMatch` extension function that is not implemented in the project. This plan implements the missing functionality.

## Proposed Changes

### [Component Name] Shared Module (UI/Search)

#### [NEW] [ClubSearch.kt](file:///C:/Users/preco/Documents/github/MHS-Clubs/app/shared/src/commonMain/kotlin/com/precon/mhsclubs/screens/clubs/ClubSearch.kt)
- Implement `Iterable<Club>.fuzzyMatch(query: String)` as an extension function.
- Use a fuzzy matching algorithm that checks if the characters of the query appear in order within the target string.
- Search across club `name`, `code`, and `description`.
- Return a list of matching clubs.

## Verification Plan

### Automated Tests
- Run the existing test `ClubSearchTest.kt`.
- Run `./gradlew :app:shared:allTests` (or the appropriate test task for common tests).
- Run `analyze_file` on `ClubSearchTest.kt` to ensure the error is resolved.

### Manual Verification
- N/A (UI integration will use this once implemented in `ClubListScreen`).
