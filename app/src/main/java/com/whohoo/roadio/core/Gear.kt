package com.whohoo.roadio.core

/** The gear the driver selected, as the car reports it (GEAR_SELECTION). */
enum class Gear {
    UNKNOWN,
    PARK,
    REVERSE,
    NEUTRAL,
    DRIVE;

    companion object {
        // Android's VehicleGear values: NEUTRAL 0x1, REVERSE 0x2, PARK 0x4, DRIVE 0x8, and the manual gears from 0x10 up.
        fun fromVehicle(value: Int): Gear = when (value) {
            0x1 -> NEUTRAL
            0x2 -> REVERSE
            0x4 -> PARK
            0x8 -> DRIVE
            else -> if (value >= 0x10) DRIVE else UNKNOWN
        }
    }
}
