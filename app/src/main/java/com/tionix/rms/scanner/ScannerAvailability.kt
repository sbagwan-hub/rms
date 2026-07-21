package com.tionix.rms.scanner

import android.content.Context
import android.content.pm.PackageManager

/**
 * Detects whether this device actually has the Honeywell DataCollection
 * service (i.e. a real hardware imager). On normal phones/emulators the
 * service is absent → the app falls back to CAMERA scanning (ML Kit).
 */
object ScannerAvailability {

    private const val HONEYWELL_DCS_PACKAGE = "com.intermec.datacollectionservice"

    enum class Mode { HONEYWELL_IMAGER, CAMERA }

    fun detect(context: Context): Mode = try {
        context.packageManager.getPackageInfo(HONEYWELL_DCS_PACKAGE, 0)
        Mode.HONEYWELL_IMAGER
    } catch (e: PackageManager.NameNotFoundException) {
        Mode.CAMERA
    }
}
