package com.example.myapplication

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.demo.LocalMyDeckStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Não mistura dados persistidos de contas diferentes no mesmo aparelho. */
@RunWith(AndroidJUnit4::class)
class PerAccountLocalPersistenceTest {
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun isolatedAndRestoredAcrossAccountNamespaces() {
        val userA = "test-isolation-account-a"
        val userB = "test-isolation-account-b"
        val first = LocalMyDeckStore(context, namespace = userA)
        val second = LocalMyDeckStore(context, namespace = userB)
        // Evita depender do estado remanescente de execuções anteriores do teste.
        val uniqueName = "Turma-a-" + System.nanoTime()
        val added = first.createClass(uniqueName)
        assertTrue(second.classes.none { it.id == added.id })
        assertEquals(added.id,
            LocalMyDeckStore(context, namespace = userA).classes.first { it.id == added.id }.id
        )
        assertTrue(LocalMyDeckStore(context, namespace = userB).classes.none { it.id == added.id })
    }
}
