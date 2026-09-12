package com.example

import com.example.data.model.PhilippineRegions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhilippineRegionsTest {
    @Test
    fun `catalog contains every current region and province`() {
        assertEquals(18, PhilippineRegions.REGIONS.size)
        assertEquals(18, PhilippineRegions.REGIONS.map { it.id }.distinct().size)
        assertEquals(18, PhilippineRegions.REGIONS.map { it.name }.distinct().size)
        assertEquals(82, PhilippineRegions.PROVINCES.count { it.region != NCR })
        assertEquals(17, PhilippineRegions.provincesFor(NCR).size)
        assertEquals(
            PhilippineRegions.REGIONS.map { it.name }.toSet(),
            PhilippineRegions.PROVINCES.map { it.region }.toSet()
        )
    }

    @Test
    fun `Negros Island Region has its official provinces`() {
        assertEquals(
            setOf("Negros Occidental", "Negros Oriental", "Siquijor"),
            PhilippineRegions.provincesFor(NIR).map { it.name }.toSet()
        )
        assertEquals(5, PhilippineRegions.provincesFor("Region VI (Western Visayas)").size)
        assertEquals(2, PhilippineRegions.provincesFor("Region VII (Central Visayas)").size)
    }

    @Test
    fun `Sulu is listed under Region IX rather than BARMM`() {
        assertTrue(
            PhilippineRegions.provincesFor("Region IX (Zamboanga)").any { it.name == "Sulu" }
        )
        assertFalse(
            PhilippineRegions.provincesFor("BARMM (Bangsamoro)").any { it.name == "Sulu" }
        )
    }

    @Test
    fun `region selector is derived from the official catalog`() {
        assertEquals("All", PhilippineRegions.REGION_LIST.first())
        assertEquals(
            PhilippineRegions.REGIONS.map { it.name },
            PhilippineRegions.REGION_LIST.drop(1)
        )
    }

    private companion object {
        const val NCR = "NCR (Metro Manila)"
        const val NIR = "Negros Island Region (NIR)"
    }
}
