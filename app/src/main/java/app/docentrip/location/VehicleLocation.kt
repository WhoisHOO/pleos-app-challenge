package app.docentrip.location

import ai.pleos.playground.location.fused.FusedLocation
import ai.pleos.playground.location.fused.constant.ErrorCode
import ai.pleos.playground.location.fused.exception.LocationException
import ai.pleos.playground.location.fused.listener.FusedLocationListener
import ai.pleos.playground.location.fused.model.LocationData
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import app.docentrip.core.GeoPoint

/**
 * The car's position from Pleos Connect's FusedLocation SDK, about once a second. Needs the runtime permission
 * [PERMISSION], and nothing from Android's own location permissions. Devices without the Pleos location service (phones,
 * plain emulators) are not [isSupported]. [onFix] and [onProblem] are called on the main thread.
 */
class VehicleLocation(
    context: Context,
    private val onFix: (point: GeoPoint, takenMillis: Long) -> Unit,
    private val onProblem: (Problem) -> Unit,
) {

    enum class Problem {
        /** The location permission was taken back. */
        PERMISSION,

        /** Location is switched off in the car's privacy settings. */
        DISABLED,

        /** The car's location service failed. */
        SERVICE,
    }

    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var fused: FusedLocation? = null

    val isSupported: Boolean = runCatching { FusedLocation(appContext).isSupported() }.getOrDefault(false)

    val hasPermission: Boolean
        get() = appContext.checkSelfPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED

    val isRunning: Boolean
        get() = fused != null

    private val listener = object : FusedLocationListener {
        override fun onChanged(location: LocationData?) {
            location ?: return
            val point = GeoPoint(location.latitude, location.longitude)
            // When the fix was taken: fixes held back while the app was frozen arrive together.
            val taken = location.timestampMillis.takeIf { it > 0 } ?: System.currentTimeMillis()
            main.post { if (fused != null) onFix(point, taken) }
        }

        // The service sends its error code as the message of a plain Exception (for example "800002" when location is
        // off in the car's privacy settings), about once a second for as long as the problem lasts.
        override fun onError(exception: Exception) {
            val id = exception.message?.trim()?.toIntOrNull()
            val code = (exception as? LocationException)?.errorCode ?: ErrorCode.entries.firstOrNull { it.id == id }
            val problem = when (code) {
                ErrorCode.ERROR_SECURITY_PERMISSION -> Problem.PERMISSION
                ErrorCode.ERROR_LOCATION_DISABLED -> Problem.DISABLED
                else -> Problem.SERVICE
            }
            Log.w(TAG, "vehicle location: $problem (${exception.message})")
            main.post { if (fused != null) onProblem(problem) }
        }
    }

    fun start() {
        if (fused != null) return
        try {
            val location = FusedLocation(appContext)
            fused = location
            location.initialize()
            location.registerFusedLocationCallback(listener)
            Log.i(TAG, "vehicle location started")
        } catch (e: SecurityException) {
            stop()
            onProblem(Problem.PERMISSION)
        } catch (e: RuntimeException) {
            Log.w(TAG, "vehicle location could not start", e)
            stop()
            onProblem(Problem.SERVICE)
        }
    }

    fun stop() {
        val location = fused ?: return
        fused = null
        runCatching { location.unregisterFusedLocationCallback(listener) }
        runCatching { location.release() }
    }

    companion object {
        const val PERMISSION = "pleos.car.permission.FUSED_LOCATION"
        private const val TAG = "Docentrip"
    }
}
