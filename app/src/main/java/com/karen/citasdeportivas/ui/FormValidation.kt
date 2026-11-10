package com.karen.citasdeportivas.ui

import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Texto del campo sin espacios al inicio ni al final. */
fun EditText.value(): String = text.toString().trim()

/** Marca el campo con el error (icono rojo) y devuelve el mismo mensaje para mostrarlo en el formulario. */
fun EditText.fail(message: String): String {
    error = message
    return message
}

/** Quita las marcas de error de los campos indicados. */
fun clearFieldErrors(vararg fields: EditText) {
    fields.forEach { it.error = null }
}

/** Fecha de hoy con el formato de la API (AAAA-MM-DD). */
fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

/** Las fechas AAAA-MM-DD se pueden comparar como texto: "2026-11-08" < "2026-11-09". */
fun isPastDate(date: String): Boolean = date < todayString()