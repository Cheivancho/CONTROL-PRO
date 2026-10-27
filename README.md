# Control Proyector (IR)

App Android (Kotlin + Jetpack Compose) que replica el control remoto del proyector
usando el emisor infrarrojo del teléfono (`ConsumerIrManager`, permiso `TRANSMIT_IR`).

## Cómo abrirlo
1. Android Studio (Koala o más nuevo) → **Open** → carpeta `ProyectorIR`.
2. Esperar el *Gradle sync* (descarga Gradle 8.7 y dependencias).
3. Conectar el Xiaomi por USB (depuración USB activada) y darle **Run**.

## Cómo cargar los códigos (primer uso)
1. Activar el interruptor **Configurar** (arriba a la derecha).
2. Tocar un botón, por ejemplo **Encendido**.
3. Dejar el celular apuntando al proyector y tocar **Escanear comandos**.
   Envía los comandos NEC 00, 01, 02… uno cada 0,7 s con la dirección indicada.
4. Cuando el proyector reaccione, tocar **Detener escaneo** y luego **Guardar**.
   Si el comando guardado quedó uno adelante, usar **◀** para volver y probar.
5. Repetir con cada botón. Los botones con código se marcan en verde.

La dirección NEC es lo primero a averiguar: si el escaneo con `00` no hace nada,
probar otras direcciones comunes (`00`, `01`, `04`, `10`, `20`, `40`, `7F`, `80`,
`FE`) o capturar el control original con un receptor IR (TSOP1838 + Arduino/ESP32).
Algunos proyectores usan NEC extendido (tildar la casilla y usar 4 dígitos hex).

## Archivos
- `NecEncoder.kt` – convierte dirección/comando NEC en el patrón de pulsos (38 kHz).
- `IrSender.kt` – envía el patrón con `ConsumerIrManager`.
- `CodeStore.kt` – lista de botones y guardado de códigos en el teléfono.
- `MainActivity.kt` – pantalla del control (misma disposición que el original) y diálogo de configuración.
