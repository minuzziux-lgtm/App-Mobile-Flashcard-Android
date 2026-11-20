package com.example.myapplication.ui.design


/** Idioma unico da interface: portugues brasileiro. */
object MyDeckLocale {
    const val language = "pt"
    fun load(context: android.content.Context) = Unit
    fun text(pt: String, en: String): String = pt
}
