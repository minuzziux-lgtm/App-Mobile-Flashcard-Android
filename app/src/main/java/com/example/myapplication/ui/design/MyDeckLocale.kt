package com.example.myapplication.ui.design

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Preferência local de idioma; novas telas devem usar text() ou recursos traduzidos. */
object MyDeckLocale {
    var language by mutableStateOf("pt")
        private set

    fun load(context: Context) {
        language = context.getSharedPreferences("mydeck_ui", Context.MODE_PRIVATE)
            .getString("language", "pt")?.takeIf { it == "en" } ?: "pt"
    }
    fun toggle(context: Context) {
        language = if (language == "pt") "en" else "pt"
        context.getSharedPreferences("mydeck_ui", Context.MODE_PRIVATE)
            .edit().putString("language", language).apply()
    }
    fun text(pt: String, en: String): String = if (language == "en") en else pt
}
