package com.whohoo.roadio.car

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Follows the car's driving restrictions (CarUxRestrictions), which Pleos Connect asks every distraction-optimized app
 * to apply itself. [onChange] is told what the screen may do: while the car limits text, the story is heard and not
 * read; while it forbids setup, settings such as the location source wait until the car stops. Phones and other
 * devices without the car service limit nothing.
 */
class DrivingRestrictions(
    context: Context,
    private val onChange: (Limits) -> Unit,
) {

    data class Limits(val text: Boolean = false, val setup: Boolean = false)

    private var car: Car? = null
    private var manager: CarUxRestrictionsManager? = null

    init {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            try {
                // Called on the main thread when the car service is ready, and again after it restarts.
                car = Car.createCar(context.applicationContext, null, Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT) { car, ready ->
                    manager = null
                    if (ready) {
                        val manager = car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
                        this.manager = manager
                        manager?.let {
                            update(it.currentCarUxRestrictions)
                            it.registerListener(::update)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "driving restrictions not available: story text is always shown", e)
            } catch (e: LinkageError) {
                Log.w(TAG, "driving restrictions not available: story text is always shown", e)
            }
        }
    }

    private fun update(restrictions: CarUxRestrictions) {
        val active = if (restrictions.isRequiresDistractionOptimization) restrictions.activeRestrictions else 0
        val limits = Limits(
            text = active and LIMITS_TEXT != 0,
            setup = active and CarUxRestrictions.UX_RESTRICTIONS_NO_SETUP != 0,
        )
        Log.i(TAG, "driving restrictions 0x${Integer.toHexString(restrictions.activeRestrictions)}: $limits")
        onChange(limits)
    }

    fun release() {
        runCatching { manager?.unregisterListener() }
        runCatching { car?.disconnect() }
        manager = null
        car = null
    }

    private companion object {
        const val TAG = "Roadio"
        const val LIMITS_TEXT =
            CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH or CarUxRestrictions.UX_RESTRICTIONS_LIMIT_CONTENT
    }
}
