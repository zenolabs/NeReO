/*
 * OpenUsenet — Copyright (C) 2026 OpenUsenet contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * 1.2.5 (Bingo3331): controllo "c'è una versione più recente su SourceForge?".
 *
 * NeReO è pubblicato su https://sourceforge.net/projects/nereo/ con un file per
 * ogni release, dentro una cartella col numero di versione (es. /files/1.2.4/).
 * SourceForge espone un endpoint JSON pubblico, pensato apposta per i pulsanti
 * "scarica l'ultima": best_release.json. Ne leggiamo `release.filename`, che
 * contiene il percorso della cartella (es. "/1.2.4/NeReO_1.2.4_src.zip"): da lì
 * si ricava il numero di versione, a prescindere da quale file sia il predefinito.
 *
 * DNA di NeReO: nessuna telemetria. Questa è UNA sola GET a un URL pubblico, senza
 * alcun dato dell'utente. L'app NON scarica né installa nulla da sola: si limita a
 * dire "c'è la X" e a offrire il link alla pagina delle release.
 */
package org.openusenet.reader.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

    /** Pagina delle release, da aprire nel browser per scaricare a mano. */
    const val RELEASES_URL = "https://sourceforge.net/projects/nereo/files/"

    private const val BEST_RELEASE_URL =
        "https://sourceforge.net/projects/nereo/best_release.json"

    private const val TIMEOUT_MS = 10_000

    /** Estrae il primo numero di versione (1, 1.2, 1.2.4, …) da una stringa. */
    private val VERSIONE = Regex("""\d+(?:\.\d+)+""")

    /**
     * Interroga SourceForge e restituisce l'ultima versione pubblicata (es.
     * "1.2.4"), oppure null se non raggiungibile / risposta inattesa. Silenzioso:
     * qualunque errore di rete diventa null, non un'eccezione (offline è normale).
     */
    suspend fun latestVersion(): String? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL(BEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json")
            }
            try {
                if (conn.responseCode != 200) return@runCatching null
                val testo = conn.inputStream.bufferedReader().use { it.readText() }
                val filename = JSONObject(testo)
                    .optJSONObject("release")
                    ?.optString("filename")
                    ?.takeIf { it.isNotBlank() }
                    ?: return@runCatching null
                VERSIONE.find(filename)?.value
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }

    /**
     * true se [latest] è una versione strettamente maggiore di [current].
     * 1.5.8: il corpo sta in [versionePiuNuova] (VersioneVista.kt, puro), che
     * serve anche alla striscia «Aggiornato a».
     */
    fun isNewer(latest: String, current: String): Boolean = versionePiuNuova(latest, current)

    /** Vedi [intervalloControlloMs] in FinestraControllo.kt (1.5.4: spostata li', pura). */
    fun intervalMs(mode: String): Long? = intervalloControlloMs(mode)

    /** Vedi [deveControllareAggiornamenti] in FinestraControllo.kt. */
    fun deveControllare(mode: String, ultimoMs: Long, oraMs: Long, giornoUltimo: Long, giornoOggi: Long): Boolean =
        deveControllareAggiornamenti(mode, ultimoMs, oraMs, giornoUltimo, giornoOggi)
}
