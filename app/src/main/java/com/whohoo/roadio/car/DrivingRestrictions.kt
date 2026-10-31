package com.whohoo.roadio.car

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Follows the car's driving restrictions (CarUxRestrictions), which Pleos Connect asks every distraction-optimized app
 * to apply itself. [onTextLimited] is told whether the screen may show a story's full text: while the car limits text,
 * the story is heard and not read. Phones and other devices without the car service never limit it.
 */
class DrivingRestrictions(
    context: Context,
    private val onTextLimited: (Boolean) -> Unit,
) {

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
        val limited = restrictions.isRequiresDistractionOptimization &&
            restrictions.activeRestrictions and LIMITS_TEXT != 0
        Log.i(TAG, "driving restrictions 0x${Integer.toHexString(restrictions.activeRestrictions)}: story text ${if (limited) "hidden" else "shown"}")
        onTextLimited(limited)
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
