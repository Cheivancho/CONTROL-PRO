package com.proyectorir

/** Código IR en protocolo NEC. */
data class IrCode(
    val address: Int,
    val command: Int,
    /** true = NEC extendido (dirección de 16 bits, sin byte invertido). */
    val extended: Boolean = false,
)

/**
 * Convierte un código NEC en el patrón de tiempos (microsegundos, alternando
 * pulso/pausa) que espera ConsumerIrManager.transmit(). Portadora: 38 kHz.
 */
object NecEncoder {
    const val CARRIER_HZ = 38000

    private const val HEADER_MARK = 9000
    private const val HEADER_SPACE = 4500
    private const val MARK = 560
    private const val SPACE_ZERO = 560
    private const val SPACE_ONE = 1690

    fun encode(address: Int, command: Int, extended: Boolean = false): IntArray {
        val bytes = if (extended) {
            intArrayOf(address and 0xFF, (address shr 8) and 0xFF, command and 0xFF, command.inv() and 0xFF)
        } else {
            intArrayOf(address and 0xFF, address.inv() and 0xFF, command and 0xFF, command.inv() and 0xFF)
        }

        val out = ArrayList<Int>(67)
        out += HEADER_MARK
        out += HEADER_SPACE
        for (b in bytes) {
            for (i in 0..7) { // NEC envía primero el bit menos significativo
                out += MARK
                out += if (((b shr i) and 1) == 1) SPACE_ONE else SPACE_ZERO
            }
        }
        out += MARK // bit de parada
        return out.toIntArray()
    }
}
