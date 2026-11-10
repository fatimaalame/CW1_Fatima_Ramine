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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // super = class parente
        setContent {
            MaterialTheme {
                BurgerWorldApp() //appel
            }
        }
    }
}

@Composable //annotation qui décrit l'écran
fun BurgerWorldApp () {
    //SensorManager to access phone sensors
    val sensorManager = applicationSensorManager()
    val lightSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }
    val proximitySensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }
    val temperatureSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    }

    //here we got the values that are affichées à l'écran
    // Get sensors (may be null on some devices ) & "?" declares the type as nullable
    var lightValue by remember { mutableStateOf<Float?>(null) }
    var proximityValue by remember { mutableStateOf<Float?>(null) }
    var temperatureValue by remember { mutableStateOf<Float?>(null) }

    val lightThreshold = 200f //seuil

    val sensorListener = remember {
        object : SensorEventListener {

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                when (event.sensor.type) {
                    Sensor.TYPE_LIGHT -> {
                        lightValue = event.values[0]
                    }

                    Sensor.TYPE_PROXIMITY -> {
                        proximityValue = event.values[0]
                    }

                    Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                        temperatureValue = event.values[0]
                    }
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int
            ) { //nothging to do for this protoype
            }
        }
    }
    //dispoEffect to make it start listening when screen on and stop when off + Register/unregister a single listener
    //We use DisposableEffect, for side effects that need to be cleaned up after the keys change or if the composable leaves the Composition
    // More information about side effects on https://developer.android.com/develop/ui/compose/side-effects

    DisposableEffect(sensorManager) {
        lightSensor?.let {
            sensorManager.registerListener(
                sensorListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        proximitySensor?.let {
            sensorManager.registerListener(
                sensorListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        temperatureSensor?.let {
            sensorManager.registerListener(
                sensorListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    //implementing the diff burger states
    val burgerState = when {
        lightValue != null && lightValue!! < lightThreshold ->
            BurgerState.SLEEPING

        (temperatureValue ?: Float.NEGATIVE_INFINITY) >= 35f ->
            BurgerState.MELTING

        proximityValue != null && proximityValue!! <= 5f ->
            BurgerState.SURPRISED

        else ->
            BurgerState.AWAKE
    }

    BurgerWorldScreen(
        burgerState = burgerState,
        lightValue = lightValue,
        proximityValue = proximityValue,
        temperatureValue = temperatureValue
    )
}

@Composable
private fun applicationSensorManager(): SensorManager {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember (context) {
        context.getSystemService(SensorManager::class.java)
    }
}

private enum class BurgerState {
    AWAKE,
    SLEEPING,
    SURPRISED,
    MELTING
}

// UI
// For more information about layout using composable, see Lecture 3
// https://developer.android.com/courses/pathways/android-basics-compose-unit-1-pathway-3

@Composable
private fun BurgerWorldScreen (
    burgerState: BurgerState,
    lightValue: Float ?,
    proximityValue: Float?,
    temperatureValue: Float?
) {

    Box(
        modifier = Modifier.fillMaxSize()
    )  {
       //for the background selon l'état du burger
        Image (
           painter = painterResource(
               when (burgerState) {
                   BurgerState.SLEEPING -> R.drawable.restaurant_night
                   BurgerState.MELTING -> R.drawable.restaurant_hot
                   BurgerState.SURPRISED -> R.drawable.restaurant_frightened
                   else -> R.drawable.restaurant_background
               }
           ),
           contentDescription = null,
           contentScale = ContentScale.Crop,
           modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start=18.dp,end=18.dp, top=36.dp,bottom=12.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
           ) {
                Image ( //logo Uni
                    painter = painterResource(R.drawable.logo_unige),
                    contentDescription = "logo de l'uni",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(width=140.dp,height=60.dp)
                )

                Box (
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y=12.dp)
                ) {
                    Text (
                        text = "CW1 RamFat",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
           }

            Spacer (modifier = Modifier.height(18.dp))
            val burgerMessage = when(burgerState) {
                BurgerState.AWAKE -> "Mike is awake"
                BurgerState.SLEEPING -> "Mike is sleeping"
                BurgerState.SURPRISED -> "Mike is afraid"
                BurgerState.MELTING -> "Mike is melting"
            }

            AnimatedContent(
                targetState=burgerState,
                label = "transition du burger"
            ) { state ->
                val image = when (state) {
                    BurgerState.AWAKE -> R.drawable.burger_awake
                    BurgerState.SLEEPING -> R.drawable.burger_sleeping
                    BurgerState.SURPRISED -> R.drawable.burger_surprised
                    BurgerState.MELTING -> R.drawable.hot_sun
                }
                Image(
                    painter = painterResource(image),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(210.dp)
                )
            }

            Text(
                text = burgerMessage,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Text (
                    text = "MESURES",
                    color = Color.Black,
                    fontSize = 22.sp,
                )

                SensorLine (
                    label = "Light ",
                    value = formatSensorValue(lightValue, "lx")
                )
                SensorLine (
                    label = "Proximity",
                    value = formatSensorValue(proximityValue, "cm")
                )
                SensorLine(
                    label = "Temperature ",
                    value = formatSensorValue(temperatureValue, "degree (C)")
                )
            }
        }
    }
}

@Composable
private fun SensorLine(
    label:String,
    value: String
){
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text (
            text = "$label =",
            color = Color.Black,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        )
        Text (
            text = value,
            color = Color.Black,
            fontSize = 17.sp,
        )
    }
}

private fun formatSensorValue (
    value: Float?,
    unit:String
):String{
    return if (value == null) {
        "indisponible"
    } else {
        String.format(Locale.US, "%1f %s", value, unit)
    }
}