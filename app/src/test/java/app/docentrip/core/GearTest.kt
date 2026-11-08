package app.docentrip.core

import org.junit.Assert.assertEquals
import org.junit.Test

class GearTest {

    @Test
    fun vehicleGearValuesMap() {
        assertEquals(Gear.NEUTRAL, Gear.fromVehicle(0x1))
        assertEquals(Gear.REVERSE, Gear.fromVehicle(0x2))
        assertEquals(Gear.PARK, Gear.fromVehicle(0x4))
        assertEquals(Gear.DRIVE, Gear.fromVehicle(0x8))
        // Manual gears (GEAR_1 and up) are driving forward.
        assertEquals(Gear.DRIVE, Gear.fromVehicle(0x10))
        assertEquals(Gear.DRIVE, Gear.fromVehicle(0x1000))
        assertEquals(Gear.UNKNOWN, Gear.fromVehicle(0))
    }
}
