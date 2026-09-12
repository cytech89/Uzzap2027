package com.example

import com.example.ui.auth.AUTH_PROGRESS_TOTAL_DURATION_MILLIS
import com.example.ui.viewmodel.remainingAuthProgressMillis
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthProgressTest {
    @Test
    fun `successful authentication waits for the full progress sequence`() {
        assertEquals(
            AUTH_PROGRESS_TOTAL_DURATION_MILLIS,
            remainingAuthProgressMillis(startedAtMillis = 1_000L, nowMillis = 1_000L)
        )
        assertEquals(
            1_400L,
            remainingAuthProgressMillis(startedAtMillis = 1_000L, nowMillis = 1_700L)
        )
        assertEquals(
            0L,
            remainingAuthProgressMillis(startedAtMillis = 1_000L, nowMillis = 4_000L)
        )
    }
}
