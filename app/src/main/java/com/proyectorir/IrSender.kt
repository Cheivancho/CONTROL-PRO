package com.proyectorir

import android.content.Context
import android.hardware.ConsumerIrManager

class IrSender(context: Context) {
    private val manager =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    /** El Xiaomi 11 Lite 5G NE trae emisor IR, así que debería dar true. */
    val available: Boolean
        get() = manager?.hasIrEmitter() == true

    fun send(code: IrCode): Boolean {
        val m = manager ?: return false
        if (!m.hasIrEmitter()) return false
        return try {
            m.transmit(NecEncoder.CARRIER_HZ, NecEncoder.encode(code.address, code.command, code.extended))
            true
        } catch (e: Exception) {
            false
        }
    }
}
