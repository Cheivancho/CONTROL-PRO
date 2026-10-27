package com.proyectorir

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sender = IrSender(this)
        val store = CodeStore(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    RemoteScreen(sender, store)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pantalla del control
// ---------------------------------------------------------------------------

@Composable
fun RemoteScreen(sender: IrSender, store: CodeStore) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var configMode by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Btn?>(null) }
    var version by remember { mutableIntStateOf(0) } // fuerza recomposición al guardar

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    fun press(b: Btn) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (configMode) {
            editing = b
            return
        }
        val code = store.get(b)
        when {
            code == null -> toast("Sin código para «${b.label}». Activá Configurar.")
            !sender.send(code) -> toast("No se pudo emitir IR")
        }
    }

    // marca verde en modo configurar = botón ya tiene código
    fun marked(b: Btn): Boolean {
        @Suppress("UNUSED_EXPRESSION") version
        return configMode && store.get(b) != null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Control proyector", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Configurar", fontSize = 14.sp)
                Spacer(Modifier.size(8.dp))
                Switch(checked = configMode, onCheckedChange = { configMode = it })
            }
        }

        if (!sender.available) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Este teléfono no reporta emisor infrarrojo. No se va a poder enviar nada.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
            )
        }
        if (configMode) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Tocá un botón para asignarle un código. Verde = ya tiene código.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Fila 1: encendido y silencio
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Key(Icons.Filled.PowerSettingsNew, "Encendido", { press(Btn.POWER) },
                container = Color(0xFFE53935), marked = marked(Btn.POWER))
            Key(Icons.Filled.VolumeOff, "Silencio", { press(Btn.MUTE) }, marked = marked(Btn.MUTE))
        }

        Spacer(Modifier.height(16.dp))

        // Fila 2: ajustes, fuente, mouse
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Key(Icons.Filled.Settings, "Ajustes", { press(Btn.SETTINGS) }, marked = marked(Btn.SETTINGS))
            Key(Icons.AutoMirrored.Filled.Input, "Fuente", { press(Btn.SOURCE) }, marked = marked(Btn.SOURCE))
            Key(Icons.Filled.Mouse, "Mouse", { press(Btn.MOUSE) }, marked = marked(Btn.MOUSE))
        }

        Spacer(Modifier.height(28.dp))

        // Cruceta
        DPad(::press, ::marked)

        Spacer(Modifier.height(28.dp))

        // Fila 3: inicio, menú, atrás
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Key(Icons.Filled.Home, "Inicio", { press(Btn.HOME) }, marked = marked(Btn.HOME))
            Key(Icons.Filled.Menu, "Menú", { press(Btn.MENU) }, marked = marked(Btn.MENU))
            Key(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", { press(Btn.BACK) }, marked = marked(Btn.BACK))
        }

        Spacer(Modifier.height(20.dp))

        // Volumen
        Row(Modifier.fillMaxWidth(0.7f), horizontalArrangement = Arrangement.SpaceBetween) {
            Key(Icons.Filled.Remove, "Volumen −", { press(Btn.VOL_DOWN) }, marked = marked(Btn.VOL_DOWN))
            Key(Icons.Filled.Add, "Volumen +", { press(Btn.VOL_UP) }, marked = marked(Btn.VOL_UP))
        }
    }

    editing?.let { b ->
        CodeDialog(
            btn = b,
            initial = store.get(b),
            sender = sender,
            onSave = { code ->
                store.put(b, code)
                version++
                editing = null
                toast("Guardado: ${b.label}")
            },
            onClear = {
                store.clear(b)
                version++
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun DPad(press: (Btn) -> Unit, marked: (Btn) -> Boolean) {
    Box(
        modifier = Modifier
            .size(240.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.align(Alignment.TopCenter).padding(top = 8.dp)) {
            Key(Icons.Filled.KeyboardArrowUp, "Arriba", { press(Btn.UP) }, size = 56.dp, marked = marked(Btn.UP))
        }
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) {
            Key(Icons.Filled.KeyboardArrowDown, "Abajo", { press(Btn.DOWN) }, size = 56.dp, marked = marked(Btn.DOWN))
        }
        Box(Modifier.align(Alignment.CenterStart).padding(start = 8.dp)) {
            Key(Icons.Filled.KeyboardArrowLeft, "Izquierda", { press(Btn.LEFT) }, size = 56.dp, marked = marked(Btn.LEFT))
        }
        Box(Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)) {
            Key(Icons.Filled.KeyboardArrowRight, "Derecha", { press(Btn.RIGHT) }, size = 56.dp, marked = marked(Btn.RIGHT))
        }
        FilledIconButton(
            onClick = { press(Btn.OK) },
            modifier = Modifier
                .size(88.dp)
                .then(if (marked(Btn.OK)) Modifier.border(2.dp, MarkColor, CircleShape) else Modifier),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text("OK", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private val MarkColor = Color(0xFF4CAF50)

@Composable
private fun Key(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    size: Dp = 64.dp,
    container: Color = MaterialTheme.colorScheme.surfaceVariant,
    marked: Boolean = false,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .then(if (marked) Modifier.border(2.dp, MarkColor, CircleShape) else Modifier),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = container,
            contentColor = if (container == Color(0xFFE53935)) Color.White
            else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(icon, contentDescription = description)
    }
}

// ---------------------------------------------------------------------------
// Diálogo para asignar / probar / escanear códigos
// ---------------------------------------------------------------------------

@Composable
private fun CodeDialog(
    btn: Btn,
    initial: IrCode?,
    sender: IrSender,
    onSave: (IrCode) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var addr by remember { mutableStateOf(initial?.address?.let { "%02X".format(it) } ?: "00") }
    var cmd by remember { mutableStateOf(initial?.command?.let { "%02X".format(it) } ?: "00") }
    var ext by remember { mutableStateOf(initial?.extended ?: false) }
    var scanning by remember { mutableStateOf(false) }

    fun parse(): IrCode? {
        val a = addr.trim().toIntOrNull(16) ?: return null
        val c = cmd.trim().toIntOrNull(16) ?: return null
        if (a !in 0..(if (ext) 0xFFFF else 0xFF)) return null
        if (c !in 0..0xFF) return null
        return IrCode(a, c, ext)
    }

    fun step(delta: Int) {
        val c = (cmd.trim().toIntOrNull(16) ?: 0) + delta
        if (c !in 0..0xFF) return
        cmd = "%02X".format(c)
        parse()?.let { sender.send(it) }
    }

    // Escaneo: manda cmd, cmd+1, cmd+2... uno cada 700 ms. Se detiene con «Detener».
    LaunchedEffect(scanning) {
        if (!scanning) return@LaunchedEffect
        var c = cmd.trim().toIntOrNull(16) ?: 0
        while (c <= 0xFF) {
            cmd = "%02X".format(c)
            parse()?.let { sender.send(it) }
            delay(700)
            c++
        }
        scanning = false
    }

    AlertDialog(
        onDismissRequest = { scanning = false; onDismiss() },
        title = { Text(btn.label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = addr,
                        onValueChange = { addr = it.uppercase().take(4) },
                        label = { Text("Dirección (hex)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = cmd,
                        onValueChange = { cmd = it.uppercase().take(2) },
                        label = { Text("Comando (hex)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = ext, onCheckedChange = { ext = it })
                    Text("NEC extendido (dirección 16 bits)", fontSize = 13.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { parse()?.let { sender.send(it) } }, enabled = !scanning) {
                        Text("Probar")
                    }
                    OutlinedButton(onClick = { step(-1) }, enabled = !scanning) { Text("◀") }
                    OutlinedButton(onClick = { step(+1) }, enabled = !scanning) { Text("▶") }
                }
                Button(onClick = { scanning = !scanning }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (scanning) "Detener escaneo" else "Escanear comandos desde ${cmd.ifBlank { "00" }}")
                }
                Text(
                    "Apuntá al proyector. Cuando reaccione, tocá «Detener» y guardá: " +
                        "el comando que ves es el último enviado (usá ◀ si te pasaste).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { scanning = false; parse()?.let(onSave) },
                enabled = parse() != null,
            ) { Text("Guardar") }
        },
        dismissButton = {
            Row {
                if (initial != null) {
                    TextButton(onClick = { scanning = false; onClear() }) { Text("Borrar") }
                }
                TextButton(onClick = { scanning = false; onDismiss() }) { Text("Cancelar") }
            }
        },
    )
}
