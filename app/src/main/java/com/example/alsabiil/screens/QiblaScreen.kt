package com.example.alsabiil.screens

import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alsabiil.R
import com.example.alsabiil.utils.LocationService
import com.example.alsabiil.utils.PrayerCalculations
import kotlin.math.*
import kotlinx.coroutines.launch

@Composable
fun QiblaScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sensorManager = remember { context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager }
    
    val locatingText = stringResource(R.string.locating)
    val locationUnavailableText = stringResource(R.string.location_unavailable)

    var qiblaDirection by remember { mutableStateOf(0f) }
    var locationName by remember { mutableStateOf(locatingText) }
    var declination by remember { mutableStateOf(0f) }
    val rotationAnim = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(Unit) {
        val loc = LocationService.getCachedLocation(context)
            ?: LocationService.getCurrentLocation(context)
        
        loc?.let {
            val calc = PrayerCalculations()
            qiblaDirection = calc.getQiblaDirection(it.latitude, it.longitude).toFloat()
            locationName = LocationService.getCityNameWithCache(context, it.latitude, it.longitude)
            val geoField = GeomagneticField(it.latitude.toFloat(), it.longitude.toFloat(), it.altitude.toFloat(), System.currentTimeMillis())
            declination = geoField.declination
        } ?: run {
            locationName = locationUnavailableText
        }
    }

    val sensorEventListener = remember(declination) {
        object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientation = FloatArray(3)
            private var lastAccelerometer = FloatArray(3)
            private var lastMagnetometer = FloatArray(3)
            private var hasAccelerometer = false
            private var hasMagnetometer = false
            private val alpha = 0.15f // Smoothing factor for raw data

            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        for (i in 0..2) {
                            lastAccelerometer[i] = lastAccelerometer[i] + alpha * (event.values[i] - lastAccelerometer[i])
                        }
                        hasAccelerometer = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        for (i in 0..2) {
                            lastMagnetometer[i] = lastMagnetometer[i] + alpha * (event.values[i] - lastMagnetometer[i])
                        }
                        hasMagnetometer = true
                    }
                }

                if (hasAccelerometer && hasMagnetometer) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        val trueAzimuth = (azimuth + declination + 360) % 360
                        
                        val targetRotation = -trueAzimuth
                        scope.launch {
                            // Smoothly handle the 0/360 wrap-around
                            var delta = (targetRotation - rotationAnim.value) % 360f
                            if (delta > 180f) delta -= 360f
                            else if (delta < -180f) delta += 360f
                            
                            rotationAnim.animateTo(
                                targetValue = rotationAnim.value + delta,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        sensorManager.registerListener(sensorEventListener, accel, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorEventListener, mag, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFCF2)) // Mushaf background color
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.qibla_direction),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black.copy(alpha = 0.6f)
            )
            Text(
                text = locationName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Box(
            modifier = Modifier
                .size(320.dp)
                .background(Color.White, CircleShape) // Compass background is now White
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                rotate(rotationAnim.value, pivot = center) {
                    for (i in 0 until 72) {
                        val angle = i * 5f
                        val isMajor = angle % 30 == 0f
                        val isCardinal = angle % 90 == 0f

                        val tickLength = if (isCardinal) 25.dp.toPx() else if (isMajor) 15.dp.toPx() else 8.dp.toPx()
                        val strokeWidth = if (isCardinal) 3f else 1.5f
                        val tickColor = if (isCardinal && angle == 0f) Color.Red else Color.LightGray

                        rotate(angle, pivot = center) {
                            drawLine(
                                color = tickColor,
                                start = Offset(center.x, center.y - radius),
                                end = Offset(center.x, center.y - radius + tickLength),
                                strokeWidth = strokeWidth
                            )
                        }
                    }

                    val labels = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
                    labels.forEach { (text, angle) ->
                        rotate(angle, pivot = center) {
                            val textLayout = textMeasurer.measure(
                                text,
                                style = TextStyle(
                                    color = if (text == "N") Color.Red else Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayout,
                                topLeft = Offset(center.x - textLayout.size.width / 2, center.y - radius + 30.dp.toPx())
                            )
                        }
                    }

                    if (qiblaDirection != 0f) {
                        rotate(qiblaDirection, pivot = center) {
                            val arrowPath = Path().apply {
                                moveTo(center.x, center.y - radius + 5.dp.toPx())
                                lineTo(center.x - 15.dp.toPx(), center.y - radius + 40.dp.toPx())
                                lineTo(center.x + 15.dp.toPx(), center.y - radius + 40.dp.toPx())
                                close()
                            }
                            drawPath(
                                path = arrowPath,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                val staticNeedle = Path().apply {
                    moveTo(center.x, center.y - radius - 10.dp.toPx())
                    lineTo(center.x - 8.dp.toPx(), center.y - radius + 5.dp.toPx())
                    lineTo(center.x + 8.dp.toPx(), center.y - radius + 5.dp.toPx())
                    close()
                }
                drawPath(staticNeedle, color = Color.Black)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val heading = ((-rotationAnim.value % 360) + 360) % 360
            Text(
                text = "${heading.toInt()}°",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            val isFacingQibla = abs(heading - qiblaDirection) < 5
            Text(
                text = if (isFacingQibla) stringResource(R.string.facing_qibla) else stringResource(R.string.rotate_device),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isFacingQibla) Color(0xFF059669) else Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
