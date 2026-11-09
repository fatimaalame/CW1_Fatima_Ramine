package com.example.myfirstsensorapp

import android.content.Context  //to access android services (celui des capteurs aussi)
import android.hardware.Sensor //to représent a sensor and identify its type
import android.hardware.SensorEvent //répresenter une nvl measure d'un sensor
import android.hardware.SensorEventListener //define how to receive elements des sensors
import android.hardware.SensorManager //trouver les capteurs et startstop sensors
import android.os.Bundle //utiliser conteneur ds lequel android peut transmettre état saved
import android.util.Log //écrire message de diagnostic + visible dans Logcat
import androidx.activity.ComponentActivity //use une class android as a base to our screen
import androidx.activity.compose.setContent //define le contenu de l'écran avec jetpack
import androidx.compose.foundation.layout.* //use the tools as Box, Columm, padding,...
import androidx.compose.material3.Text //afficher text avec composant Text
import androidx.compose.runtime.* //utiliser Composable, remember, les états + effets
import androidx.compose.ui.Alignment //Choisir alignement des elements
import androidx.compose.ui.Modifier //define their size, marges, et d'autres proprio
import androidx.compose.ui.platform.LocalContext //obtenir context androit depuis une fonction compose
import androidx.compose.ui.unit.dp //use les unités de taille et déplacement dp
import androidx.compose.ui.unit.sp //use les unités de texte sp
import com.example.myfirstsensorapp.ui.theme.MyFirstSensorAppTheme //use the defined theme for our project
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.util.Locale
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.AnimatedContent


//We create the mainactivity as extension of CompenentActivity similar to previous exercise with Jetpack compose

///// ECRAN PRINCIPAL
class MainActivity : ComponentActivity() { //concept d'héritage
    override fun onCreate(savedInstanceState: Bundle?) { //fun=function et onCreate= nom imposé par android
        //bundle ou null (absence de val)
        super.onCreate(savedInstanceState) // super = class parente
        setContent { MyFirstSensorAppTheme {  SensorsScreen() }}
    }

}

@Composable //fonction qui décrit l'écran
fun SensorsScreen() { //fun=declare function
    val context = LocalContext.current
    // "Remember" stores the value across recompositions of a composable function.
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Get sensors (may be null on some devices )
    // "?" declares the type as nullable
    val lightSensor : Sensor? = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }
    val proximitySensor : Sensor? = remember { sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) }
    val pressureSensor: Sensor? = remember { sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) }

    // Values to show
    // "by" property delegation. we access the variable as light, instead of light.value
    // "mutableStateOf" This is a state variable — if it changes, the sytem recomposes any UI elements that depend on it.
    var light by remember { mutableStateOf<Float?>(null) }
    var proximity by remember { mutableStateOf<Float?>(null) }
    var pressure by remember { mutableStateOf<Float?>(null) }

    // Register/unregister a single listener
    // We use DisposableEffect, for side effects that need to be cleaned up after the keys change or if the composable leaves the Composition
    // More information about side effects on https://developer.android.com/develop/ui/compose/side-effects
    DisposableEffect(lightSensor, proximitySensor, pressureSensor) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_LIGHT -> light = event.values.firstOrNull()
                    Sensor.TYPE_PROXIMITY -> proximity = event.values.firstOrNull()
                    Sensor.TYPE_PRESSURE -> pressure = event.values.firstOrNull()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }


        //register the listener in SensorManager
        fun reg(s: Sensor?, name: String) {
            if (s != null){
                sensorManager.registerListener(
                    listener,
                    s,
                    SensorManager.SENSOR_DELAY_UI
                )
                Log.d("Sensors", "$name sensor registered")
            } else {
                Log.w("Sensors", "$name sensor not available on this device")
            }
        }

        reg(lightSensor, "Light Sensor")
        reg(proximitySensor, "Proximity Sensor")
        reg(pressureSensor, "Pressure Sensor")

        onDispose { sensorManager.unregisterListener(listener) }
    }

    // UI
    // For more information about layout using composable, see Lecture 3
    // https://developer.android.com/courses/pathways/android-basics-compose-unit-1-pathway-3
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Sensors", fontSize = 22.sp)
            Text("Lumière: $light lx", fontSize = 18.sp)
            if (light != null && light!! < 50) {
                Text("Le burger dort")
            } else {
                Text("Le burger est réveillé")
            }
            //using Elvis operator and formating the string. More stable interface,
            // no null information appear on the screen.
            //Text("Light: ${light?.let { String.format("%.1f lx", it) } ?: "-- lx"}", fontSize = 18.sp)
            Text("Proximity: $proximity-- cm", fontSize = 18.sp)
            //Text("Proximity: ${proximity?.let { String.format("%.1f cm", it) } ?: "-- cm"}", fontSize = 18.sp)
            Text("Pressure: $pressure? hPa", fontSize = 18.sp)
            //Text("Pressure: ${pressure?.let { String.format("%.1f hPa", it) } ?: "-- hPa"}", fontSize = 18.sp)
        }
    }
}