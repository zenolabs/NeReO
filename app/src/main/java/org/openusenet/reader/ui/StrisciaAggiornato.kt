/*
 * OpenUsenet — Copyright (C) 2026 OpenUsenet contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * 1.5.8 — LA STRISCIA «AGGIORNATO A <versione> — NOVITA'» (idea di Marco,
 * piano B1 del 26/10/2026).
 *
 * UN SOLO componente, montato in DUE punti: in cima all'elenco dei gruppi di
 * GroupsScreen (telefono) e in cima alla colonna gruppi di ThreePaneHome
 * (schermi larghi, box TV). La decisione sta in update/VersioneVista.kt
 * (pura, provata in CoreTests); qui ci sono solo le preferenze e l'aspetto.
 *
 * Quando la versione diventa «vista» (B1): quando si apre la schermata
 * Novita' (dalla striscia O dal menu: lo segna ReleaseNotesScreen) oppure si
 * preme la ✕. Se la striscia viene ignorata, ricompare all'avvio successivo.
 *
 * Telecomando (Roxy): il testo e' `clickable` e la ✕ e' un IconButton, quindi
 * tutti e due ricevono il focus col tasto giu' e si premono con OK. Niente
 * gestione del focus scritta a mano: quella che vale per il resto dell'elenco.
 */
package org.openusenet.reader.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.openusenet.reader.R
import org.openusenet.reader.ReleaseNotes
import org.openusenet.reader.ui.icone.Close
import org.openusenet.reader.update.deveMostrareAggiornato

object VersioneVista {
    /** Le preferenze delle operazioni «una volta sola»: il backup §4b non le copia. */
    private const val PREFS = "manutenzione"
    private const val CHIAVE = "aggiornato_versione_vista"

    // Calcolato una volta per processo, alla prima richiesta. Variabile semplice
    // e non stato di Compose: si legge durante la composizione, e scriverci uno
    // stato li' farebbe ricomporre in un giro.
    private var calcolato = false
    private var daMostrare = false

    // Questo invece e' stato di Compose: la ✕ o l'apertura delle Novita' lo
    // cambiano da un clic, e la striscia sparisce subito in tutti e due i punti.
    private val segnata = mutableStateOf(false)

    fun visibile(ctx: Context): Boolean {
        if (!calcolato) {
            val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val vista = p.getString(CHIAVE, null)
            val prima = primaInstallazione(ctx)
            // installazione pulita: si parte da «gia' vista», cosi' il prossimo
            // aggiornamento trova una versione con cui confrontarsi
            if (prima && vista == null) p.edit().putString(CHIAVE, ReleaseNotes.VERSION).apply()
            daMostrare = deveMostrareAggiornato(ReleaseNotes.VERSION, vista, prima)
            calcolato = true
        }
        return daMostrare && !segnata.value
    }

    fun segnaVista(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(CHIAVE, ReleaseNotes.VERSION).apply()
        segnata.value = true
    }

    /**
     * Alla prima installazione Android scrive lo stesso istante in
     * firstInstallTime e lastUpdateTime; ogni aggiornamento sposta solo il
     * secondo. ⚠️ Da confermare sul telefono (collaudo della 1.5.8).
     */
    private fun primaInstallazione(ctx: Context): Boolean {
        val pi = runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0) }.getOrNull()
            ?: return false
        return pi.firstInstallTime == pi.lastUpdateTime
    }
}

@Composable
fun StrisciaAggiornato(nav: NavHostController) {
    val ctx = LocalContext.current
    if (!VersioneVista.visibile(ctx)) return
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.aggiornato_striscia, ReleaseNotes.VERSION),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    // la versione la segna ReleaseNotesScreen all'apertura
                    .clickable { nav.navigate(Routes.RELEASE_NOTES) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
            IconButton(onClick = { VersioneVista.segnaVista(ctx) }) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close))
            }
        }
    }
}
