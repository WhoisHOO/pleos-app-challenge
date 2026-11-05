package com.whohoo.roadio.car

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Follows the car's driving restrictions (CarUxRestrictions), which Pleos Connect asks every distraction-optimized app
 * to apply itself, and the gear the driver selected. [onChange] is told what the screen may do: while the car limits
 * text, the story is heard and not read; while it forbids setup, settings such as the location source wait until the
 * car stops. [onGear] gets the selected gear as Android's VehicleGear value (GEAR_SELECTION, permission
 * [GEAR_PERMISSION], granted at install). Phones and other devices without the car service limit nothing and report no
 * gear.
 */
class DrivingRestrictions(
    context: Context,
    private val onGear: (Int) -> Unit = {},
    private val onChange: (Limits) -> Unit,
) {

    data class Limits(val text: Boolean = false, val setup: Boolean = false)

    private val main = Handler(Looper.getMainLooper())
    // The gear is subscribed to off the main thread: registerCallback first asks the car service for the property's
    // configuration and waits for it, and on the Pleos emulator that wait once outlasted Android's 20 s limit for a
    // starting service (an ANR in RoadioMediaService).
    private val worker: ExecutorService = Executors.newSingleThreadExecutor { Thread(it, "roadio-gear").apply { isDaemon = true } }
    private var car: Car? = null
    private var manager: CarUxRestrictionsManager? = null
    @Volatile
    private var properties: CarPropertyManager? = null
    // Made only once the car service is there: phones have no android.car classes, and loading the callback's class
    // there would stop the app.
    @Volatile
    private var gearCallback: CarPropertyManager.CarPropertyEventCallback? = null

    init {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            try {
                // Called on the main thread when the car service is ready, and again after it restarts.
                car = Car.createCar(context.applicationContext, null, Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT) { car, ready ->
                    manager = null
                    properties = null
                    if (ready) {
                        val manager = car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
                        this.manager = manager
                        manager?.let {
                            update(it.currentCarUxRestrictions)
                            it.registerListener(::update)
                        }
                        worker.execute { followGear(car) }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "driving restrictions not available: story text is always shown", e)
            } catch (e: LinkageError) {
                Log.w(TAG, "driving restrictions not available: story text is always shown", e)
            }
        }
    }

    // registerCallback, not subscribePropertyEvents: the latter is in the API 35 stubs Roadio compiles against, but not
    // in the car service of Pleos Connect (Android 14).
    @Suppress("DEPRECATION")
    private fun followGear(car: Car) {
        try {
            val properties = car.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager ?: return
            val callback = gearCallback ?: object : CarPropertyManager.CarPropertyEventCallback {
                override fun onChangeEvent(value: CarPropertyValue<*>) {
                    (value.value as? Int)?.let { gear -> main.post { onGear(gear) } }
                }

                override fun onErrorEvent(propertyId: Int, areaId: Int) {
                    Log.w(TAG, "the car's gear could not be read")
                }
            }.also { gearCallback = it }
            this.properties = properties
            properties.registerCallback(callback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        } catch (e: SecurityException) {
            Log.w(TAG, "the car's gear is not allowed: stories do not wait while reversing", e)
        } catch (e: RuntimeException) {
            Log.w(TAG, "the car's gear is not available", e)
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
        // On the worker, after a subscription that may still be waiting there.
        worker.execute {
            val properties = properties
            val callback = gearCallback
            if (properties != null && callback != null) runCatching { properties.unregisterCallback(callback) }
            this.properties = null
        }
        runCatching { manager?.unregisterListener() }
        runCatching { car?.disconnect() }
        manager = null
        car = null
    }

    companion object {
        const val GEAR_PERMISSION = "android.car.permission.CAR_POWERTRAIN"
        private const val TAG = "Roadio"
        private const val LIMITS_TEXT =
            CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH or CarUxRestrictions.UX_RESTRICTIONS_LIMIT_CONTENT
    }
}
