package com.precon.mhsclubs.models

import kotlin.test.Test
import kotlin.test.assertEquals

class MembershipTest {
    @Test
    fun `membership values parse after model initialization`() {
        assertEquals(MembershipRole.Member, MembershipRole.fromValue("MEMBER"))
        assertEquals(MembershipRole.Advisor, MembershipRole.fromValue("advisor"))
        assertEquals(MembershipStatus.Pending, MembershipStatus.fromValue("pending"))
        assertEquals(MembershipStatus.Active, MembershipStatus.fromValue("ACTIVE"))
        assertEquals(MembershipStatus.Revoked, MembershipStatus.fromValue("revoked"))
    }
}
