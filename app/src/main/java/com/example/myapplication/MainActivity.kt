package com.example.myapplication

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // This is what will navigate through different pages.
                val navController=rememberNavController()

                // Text to speech feature, limited due to the Chinese language's use of tones.
                val context = LocalContext.current
                val tts = remember {
                    TextToSpeech(context, null).apply {
                        language = Locale.SIMPLIFIED_CHINESE
                    }
                }
                // Clean up when app closed
                DisposableEffect(Unit) {
                    onDispose {
                        tts.stop()
                        tts.shutdown()
                    }
                }

                // Passing tts into Basic Words will allow me to use it there, with other pages I
                // could do the same.
                NavHost(navController=navController, startDestination = "home"){
                    composable("home") {Home(navController)}
                    composable("tones") {Tones(navController)}
                    composable("words") {BasicWords(navController, tts) }
                }
            }
        }
    }
}

@Composable
fun Home(navController: NavController)
{
    // A little styling can be done with Column, modified to have padding and arranged vertically
    // Admittedly, style is not my forte and not a lot of focus will be here at this stage.
    Column(
        modifier=Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement= Arrangement.Center
    ){
        Text(text = "Chinese Language Jump-Starter", style= MaterialTheme.typography.titleMedium)
        Spacer(modifier=Modifier.height(16.dp))

        // Two buttons to go from one page or another
        Button(onClick = {
            navController.navigate("tones")})
        {
            Text("Learn Mandarin Tones")
        }
        Spacer(modifier=Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("words")})
        {
            Text("Practice Words")
        }

    }
}
@Composable
fun Tones(navController: NavController) {
    // There are 5 tones in the Mandarin Chinese Language, text to speech did not distinguish them,
    // so I will manually create a few sound files to play for each tone. I will only do "ma" to
    // show an example of its functioning.
    val tones = listOf(
        "mā" to R.raw.ma1, "má" to R.raw.ma2, "mǎ" to R.raw.ma3, "mà" to R.raw.ma4, "ma" to R.raw.ma)

    val context=LocalContext.current
    fun playSound(resId: Int) {
        val mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer.setOnCompletionListener {it.release()}
        mediaPlayer.start()
    }
    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment= Alignment.CenterHorizontally
    ) {
        Text(text = "The Five Tones", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        // Here I will create the buttons for each tone in the list, the buttons
        // will show the label, but speak the spoken, I do this because the TTS
        // struggles with tones and needed help
        tones.forEach { (label, sound) ->

            Button(
                onClick = {
                    playSound(sound)
                },
                modifier = Modifier
                    .padding(vertical = 4.dp)
            ) {
                Text(label)
            }
            // A little explanation added to the buttons
            Text(
                text = when (label) {
                    "mā" -> "High, flat tone"
                    "má" -> "Rising tone"
                    "mǎ" -> "Dipping tone"
                    "mà" -> "Falling tone"
                    "ma" -> "Neutral tone"
                    else -> ""
                },
            )
            Spacer(modifier=Modifier.height(30.dp))
        }

        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

// I will create word items consisting of pinyin (english pronunciation),
// chinese, english, and a phonetic (to help the tts)
data class WordItem(
    val pinyin: String,
    val chinese: String,
    val english: String,
    val phonetic: String
)
@Composable
fun BasicWords(navController: NavController,tts: TextToSpeech) {
    // I will use the list of words in WordData, more can be added for a more dictionary like
    // experience.
    val words = WordData.basicWords
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Basic Vocab", style = MaterialTheme.typography.titleMedium)
        // LazyColumn allows for the page to be scrollable and handle lists of much higher length.
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(words) { word ->
                    Button(
                        onClick = {
                            tts.speak(
                                word.phonetic,TextToSpeech.QUEUE_FLUSH,null,null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(word.pinyin + "     ")
                        Text(word.chinese + "     ")
                        Text(word.english)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
