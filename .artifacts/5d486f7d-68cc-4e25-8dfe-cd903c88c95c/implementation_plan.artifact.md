# Fix Warnings in MainActivity.kt

The goal is to resolve several lint and deprecation warnings in `MainActivity.kt`, including migrating from the deprecated `onActivityResult` to the modern Activity Result API.

## User Review Required

> [!IMPORTANT]
> This fix involves modifying `AndroidAuthService.kt` in the `shared` module to support the Activity Result API. This is necessary to properly "fix" the `onActivityResult` deprecation warning in `MainActivity.kt`.

## Proposed Changes

### [Component Name] Shared Module (Auth)

#### [MODIFY] [AndroidAuthService.kt](file:///C:/Users/preco/Documents/github/MHS-Clubs/app/shared/src/androidMain/kotlin/com/precon/mhsclubs/auth/AndroidAuthService.kt)
- Add support for `ActivityResultLauncher` to handle Google Sign-In.
- Make `REQUEST_CODE` internal or remove its strict requirement in `handleSignInResult` when using the new API.
- Update `signInWithGoogle` to use the registered launcher.

### [Component Name] Android App

#### [MODIFY] [MainActivity.kt](file:///C:/Users/preco/Documents/github/MHS-Clubs/app/androidApp/src/main/kotlin/com/precon/mhsclubs/MainActivity.kt)
- Register an `ActivityResultLauncher` for Google Sign-In.
- Remove the deprecated `onActivityResult` override.
- Use `SharedPreferences.edit { ... }` KTX extension.
- Add clarifying parentheses to logical expressions.
- Move lambda arguments out of parentheses.
- Fix formatting (trailing commas).

## Verification Plan

### Automated Tests
- Run `./gradlew :app:androidApp:assembleDebug` to ensure the project builds without errors.
- Run `analyze_file` on `MainActivity.kt` to verify warnings are gone.

### Manual Verification
- Deploy to an Android device/emulator and verify that Google Sign-In still works correctly.
- Verify that notification preference toggling still works.
