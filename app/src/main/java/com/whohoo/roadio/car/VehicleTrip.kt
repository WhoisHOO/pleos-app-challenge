package com.whohoo.roadio.car

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.Odometer
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.whohoo.roadio.core.CarMeters

/**
 * The car's own trip meter and odometer, from Pleos Connect's Vehicle SDK (Odometer, permission [PERMISSION], granted at
 * install), for the summary at the end of a drive. [read] answers on the main thread with what the car reported, or
 * null when it reported nothing: on phones, without the Vehicle Service, or when the car does not have these meters.
 *
 * The SDK has no connection callback, and a call made before its service connects can stay unanswered for good, so
 * every read has its own time limit. Its errors arrive as plain exceptions (the SDK's own exception class is renamed in
 * the shipped library), so they are only logged.
 */
class VehicleTrip(context: Context) {

    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var vehicle: Vehicle? = null

    // Connected as soon as Roadio starts, long before the first read at the start of a drive: a read waits for the
    // connection, and the first one would otherwise spend its time limit on it.
    init {
        odometer()
    }

    private fun odometer(): Odometer? {
        if (appContext.checkSelfPermission(PERMISSION) != PackageManager.PERMISSION_GRANTED) return null
        return try {
            val vehicle = vehicle ?: Vehicle(appContext).also {
                it.initialize()
                vehicle = it
            }
            vehicle.getOdometer()
        } catch (e: RuntimeException) {
            Log.w(TAG, "the car's meters are not available", e)
            null
        } catch (e: LinkageError) {
            Log.w(TAG, "the car's meters are not available", e)
            null
        }
    }

    fun read(onResult: (CarMeters?) -> Unit) {
        val odometer = odometer()
        if (odometer == null) {
            onResult(null)
            return
        }
        val request = Request(onResult)
        main.postDelayed(request.timeout, TIMEOUT_MS)
        try {
            odometer.getTripCurrentDrivingDistance(
                { metres -> main.post { request.trip(metres) } },
                { e -> failed("trip meter", e); main.post { request.trip(null) } },
            )
            odometer.getCurrentDistanceDriven(
                { km -> main.post { request.odometer(km) } },
                { e -> failed("odometer", e); main.post { request.odometer(null) } },
            )
        } catch (e: RuntimeException) {
            failed("meters", e)
            main.post { request.finish() }
        }
    }

    private fun failed(what: String, e: Exception) {
        Log.w(TAG, "the car's $what did not answer: ${e.message}")
    }

    fun release() {
        runCatching { vehicle?.release() }
        vehicle = null
    }

    private inner class Request(private val onResult: (CarMeters?) -> Unit) {
        val timeout = Runnable { finish() }
        private var tripM: Double? = null
        private var odometerKm: Double? = null
        private var answers = 0
        private var done = false

        fun trip(metres: Float?) {
            tripM = metres?.toDouble()?.takeIf { it.isFinite() && it >= 0.0 }
            answered()
        }

        fun odometer(km: Float?) {
            odometerKm = km?.toDouble()?.takeIf { it.isFinite() && it >= 0.0 }
            answered()
        }

        private fun answered() {
            answers += 1
            if (answers == 2) finish()
        }

        fun finish() {
            if (done) return
            done = true
            main.removeCallbacks(timeout)
            Log.i(TAG, "the car's meters: trip ${tripM} m, odometer ${odometerKm} km")
            onResult(if (tripM == null && odometerKm == null) null else CarMeters(tripM, odometerKm))
        }
    }

    companion object {
        const val PERMISSION = "pleos.car.permission.CAR_DRIVE"
        private const val TAG = "Roadio"
        private const val TIMEOUT_MS = 3_000L
    }
}
