package com.visualstudioex3.threads

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.visualstudioex3.threads.ui.theme.ThreadsTheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

enum class LogMessageType(val logPriority: Int) {
    Info(Log.INFO),
    Warning(Log.WARN),
    Error(Log.ERROR),
}

val handler = MyHandler()

class MyHandler : Handler(Looper.getMainLooper()) {
    override fun handleMessage(message: Message) {
        Log.println(
            message.what,
            "HANDLER_MESSAGE",
            message.obj.toString()
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThreadsTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                        innerPadding ->
                    ContentLayout(this, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ContentLayout(context: Context, modifier: Modifier = Modifier) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShowHeader()
        ShowToastAfter3SecondsButton(context)
        LogCountDownButton()
        LogMessagesGroupButtons()
        CountDownCoroutineButton()
        DrawAnimatedCircularProgress()
    }
}

@Preview(showBackground = true)
@Composable
fun ContentLayoutPreview() {
    ThreadsTheme {
        ContentLayout(
            LocalContext.current,
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Composable
fun DrawText(
    text: String,
    style: TextStyle = MaterialTheme.typography.headlineMedium
) {
    return Text(
        text = text,
        modifier = Modifier.padding(bottom = 16.dp),
        style = style
    )
}

@Composable
fun DrawButton(
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit,
    label: String,
    enabled: Boolean = true,

    ) {
    return Button(onClick, modifier, enabled) {
        Text(
            text = label,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = LocalTextStyle.current.fontSize
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ShowHeader() {
    DrawText("Mensajes, hilos y corrutinas en Jetpack Compose")
    HorizontalDivider(Modifier.padding(bottom = 16.dp))
}

@Composable
fun ShowToastAfter3SecondsButton(context: Context) {
    val buttonLabel = "Mostrar toast a los 3 segundos"

    var enabled by remember { mutableStateOf(true) }
    var label by remember { mutableStateOf(buttonLabel) }

    DrawButton(
        onClick = {
            enabled = false
            label = "Toast programado..."

            handler.postDelayed(
                {
                    showToast(context, "¡Saludos, programas!")

                    enabled = true
                    label = buttonLabel
                },
                3000L
            )
        },
        label = label,
        enabled = enabled
    )
}

@Composable
fun LogCountDownButton() {
    val buttonLabel = "Cuenta atras de 3 segundos en la consola de depuracion"

    var enabled by remember { mutableStateOf(true) }
    var label by remember { mutableStateOf(buttonLabel) }

    DrawButton(
        onClick = {
            Thread {
                fun updateText(text: String) {
                    label = text
                    Log.d("COUNTER", label)
                }

                var counter = 3

                enabled = false

                updateText("Iniciando cuenta atras...")

                while (counter >= 0) {
                    Thread.sleep(1000L)
                    updateText("Contando $counter...")

                    counter--
                }

                enabled = true
                label = buttonLabel
            }.start()
        },
        label = label,
        enabled = enabled
    )
}

@Composable
fun LogMessagesGroupButtons() {
    Spacer(Modifier.padding(4.dp))

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            DrawText(
                "Enviar mensajes a la consola de depuracion",
                MaterialTheme.typography.titleMedium
            )

            Row(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(4.dp)
            ) {
                DrawButton(
                    onClick = {
                        createMessageThread(
                            LogMessageType.Info,
                            "Mensaje de informacion."
                        ).start()
                    },
                    label = "Informacion",
                    modifier = Modifier.weight(1f)
                )
                DrawButton(
                    onClick = {
                        createMessageThread(
                            LogMessageType.Warning,
                            "Mensaje de aviso."
                        ).start()
                    },
                    label = "Aviso",
                    modifier = Modifier.weight(1f)
                )
                DrawButton(
                    onClick = {
                        createMessageThread(
                            LogMessageType.Error,
                            "Mensaje de error."
                        ).start()
                    },
                    label = "Error",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(Modifier.padding(4.dp))
}

@Composable
fun CountDownCoroutineButton() {
    val buttonLabel = "Cuenta atras de 3 segundos en la pantalla"

    var inProgress by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf(buttonLabel)}
    var counterText by remember { mutableStateOf("¡Comenzamos!") }

    DrawButton(
        onClick = { inProgress = true },
        label = label,
        enabled = !inProgress
    )

    if (inProgress)
        Text(
            counterText,
            Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

    LaunchedEffect(inProgress) {
        if (inProgress) {
            var counter = 3

            label = "En progreso..."

            while (counter >= 0) {
                delay(1000L.milliseconds)

                counterText = counter.toString()
                counter--
            }

            inProgress = false
            label = buttonLabel
        }
    }
}

@Composable
fun DrawAnimatedCircularProgress() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT)
        .show()
    Log.d("TOAST", text)
}

fun createMessageThread(type: LogMessageType, text: String): Thread {
    return Thread {
        handler.sendMessage(
            Message.obtain(
                handler,
                type.logPriority,
                text
            )
        )
    }
}
