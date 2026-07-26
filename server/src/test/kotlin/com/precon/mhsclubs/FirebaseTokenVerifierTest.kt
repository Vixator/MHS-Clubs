package com.precon.mhsclubs

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.FirebaseTokenVerifier
import com.precon.mhsclubs.auth.UserIdentity
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FirebaseTokenVerifierTest {
    private val verifier = FirebaseTokenVerifier("test-project")

    @Test
    fun `student may authenticate but is not staff admin`() {
        val student = UserIdentity("student", "student@students.mcpasd.k12.wi.us", emailVerified = true)

        assertTrue(verifier.checkScheme(student, AuthenticationScheme.AnyAuthenticated))
        assertFalse(verifier.checkScheme(student, AuthenticationScheme.StaffOnly))
    }

    @Test
    fun `verified staff email is an administrator`() {
        val staff = UserIdentity("staff", "teacher@mcpasd.k12.wi.us", emailVerified = true)

        assertTrue(verifier.checkScheme(staff, AuthenticationScheme.StaffOnly))
    }

    @Test
    fun `configured Gmail administrator receives staff access`() {
        val administrator = UserIdentity("administrator", "precon3515@gmail.com", emailVerified = true)

        assertTrue(verifier.checkScheme(administrator, AuthenticationScheme.AnyAuthenticated))
        assertTrue(verifier.checkScheme(administrator, AuthenticationScheme.StaffOnly))
    }

    @Test
    fun `unverified or non school email cannot receive staff access`() {
        val unverified = UserIdentity("staff", "teacher@mcpasd.k12.wi.us", emailVerified = false)
        val external = UserIdentity("external", "person@example.com", emailVerified = true)

        assertFalse(verifier.checkScheme(unverified, AuthenticationScheme.StaffOnly))
        assertFalse(verifier.checkScheme(external, AuthenticationScheme.StaffOnly))
    }
}
