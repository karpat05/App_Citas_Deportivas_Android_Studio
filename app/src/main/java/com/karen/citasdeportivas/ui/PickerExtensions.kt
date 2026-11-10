package com.karen.citasdeportivas.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.EditText
import java.util.Calendar
import java.util.Locale

/** Convierte un EditText en un selector de hora. Guarda el valor como HH:MM (ej. 09:30). */
fun EditText.setupTimePicker() {
    isFocusable = false
    isClickable = true
    setOnClickListener {
        val parts = text.toString().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(
            context,
            { _, h, m -> setText(String.format(Locale.US, "%02d:%02d", h, m)) },
            hour,
            minute,
            true
        ).show()
    }
}

/**
 * Convierte un EditText en un selector de fecha. Guarda el valor como AAAA-MM-DD.
 * Con blockPast = true el calendario no deja elegir días anteriores a hoy.
 */
fun EditText.setupDatePicker(blockPast: Boolean = false) {
    isFocusable = false
    isClickable = true
    setOnClickListener {
        val cal = Calendar.getInstance()
        var year = cal.get(Calendar.YEAR)
        var month = cal.get(Calendar.MONTH)
        var day = cal.get(Calendar.DAY_OF_MONTH)

        val parts = text.toString().split("-")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull()
            val m = parts[1].toIntOrNull()
            val d = parts[2].toIntOrNull()
            if (y != null && m != null && d != null && m in 1..12) {
                year = y
                month = m - 1
                day = d
            }
        }

        val dialog = DatePickerDialog(
            context,
            { _, y, m, d -> setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)) },
            year,
            month,
            day
        )
        if (blockPast) dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        dialog.show()
    }
}