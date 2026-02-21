package com.example.alsabiil.utils

import kotlin.math.*

class PrayerCalculations {
    fun getQiblaDirection(latitude: Double, longitude: Double): Double {
        val phiK = Math.toRadians(21.4225)
        val lambdaK = Math.toRadians(39.8262)
        val phi = Math.toRadians(latitude)
        val lambda = Math.toRadians(longitude)

        val numerator = sin(lambdaK - lambda)
        val denominator = cos(phi) * tan(phiK) - sin(phi) * cos(lambdaK - lambda)
        
        var qibla = atan2(numerator, denominator)
        qibla = Math.toDegrees(qibla)
        
        return (qibla + 360) % 360
    }
}
