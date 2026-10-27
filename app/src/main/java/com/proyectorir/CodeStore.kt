package com.proyectorir

import android.content.Context

/** Los 15 botones del control original. */
enum class Btn(val label: String) {
    POWER("Encendido"),
    MUTE("Silencio"),
    SETTINGS("Ajustes / brillo"),
    SOURCE("Fuente"),
    MOUSE("Mouse"),
    UP("Arriba"),
    DOWN("Abajo"),
    LEFT("Izquierda"),
    RIGHT("Derecha"),
    OK("OK"),
    HOME("Inicio"),
    MENU("Menú"),
    BACK("Atrás"),
    VOL_DOWN("Volumen −"),
    VOL_UP("Volumen +"),
}

/** Guarda en el teléfono el código asignado a cada botón. */
class CodeStore(context: Context) {
    private val prefs = context.getSharedPreferences("codes", Context.MODE_PRIVATE)

    fun get(b: Btn): IrCode? {
        val s = prefs.getString(b.name, null) ?: return null
        val t = s.split(":")
        if (t.size != 3) return null
        val a = t[0].toIntOrNull(16) ?: return null
        val c = t[1].toIntOrNull(16) ?: return null
        return IrCode(a, c, t[2] == "1")
    }

    fun put(b: Btn, code: IrCode) {
        val s = "%X:%X:%d".format(code.address, code.command, if (code.extended) 1 else 0)
        prefs.edit().putString(b.name, s).apply()
    }

    fun clear(b: Btn) {
        prefs.edit().remove(b.name).apply()
    }
}
