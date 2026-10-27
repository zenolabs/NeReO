/*
 * OpenUsenet — Copyright (C) 2026 OpenUsenet contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.openusenet.reader.update

/*
 * 1.5.8 — LA STRISCIA «AGGIORNATO A <versione> — NOVITA'» (idea di Marco,
 * piano B1 del 26/10/2026). Kotlin puro, senza un solo import, come
 * FinestraControllo.kt: la decisione si prova in CoreTests.
 *
 * Dove sta la «versione vista»: nelle preferenze `manutenzione`, che il backup
 * §4b NON copia (copia solo `settings` e `identity`) e che Android non
 * ripristina (allowBackup="false"). Cosi' un ripristino non la riporta
 * indietro e la striscia non ricompare.
 *
 * La prima installazione NON si riconosce dall'assenza della chiave: la 1.5.8
 * tacerebbe proprio con chi aggiorna dalla 1.5.7, che la chiave non l'ha mai
 * avuta. Si riconosce da PackageInfo (firstInstallTime == lastUpdateTime),
 * calcolato dal chiamante.
 */

/**
 * true se [nuova] e' una versione strettamente maggiore di [vecchia].
 * Confronto NUMERICO segmento per segmento (1.2.10 > 1.2.9, che il confronto
 * tra stringhe sbaglierebbe). I segmenti mancanti valgono 0 (1.2 == 1.2.0).
 * Robusto a spazi e a segmenti non numerici (→ 0). Era il corpo di
 * UpdateChecker.isNewer, spostato qui per usarlo anche dalla striscia senza
 * farne una seconda copia.
 */
fun versionePiuNuova(nuova: String, vecchia: String): Boolean {
    val a = segmentiVersione(nuova)
    val b = segmentiVersione(vecchia)
    val n = maxOf(a.size, b.size)
    for (i in 0 until n) {
        val x = a.getOrElse(i) { 0 }
        val y = b.getOrElse(i) { 0 }
        if (x != y) return x > y
    }
    return false
}

private fun segmentiVersione(v: String): List<Int> =
    v.trim().split('.').map { it.trim().toIntOrNull() ?: 0 }

/**
 * Mostrare la striscia «Aggiornato a [attuale]»?
 * - mai alla prima installazione;
 * - si' se non c'e' ancora una versione vista (chi aggiorna dalla 1.5.7 o
 *   prima, quando la chiave non esisteva);
 * - si' solo se [attuale] e' PIU' NUOVA di quella vista: tornare a una versione
 *   vecchia non la fa comparire.
 */
fun deveMostrareAggiornato(attuale: String, vista: String?, primaInstallazione: Boolean): Boolean {
    if (primaInstallazione) return false
    if (vista.isNullOrBlank()) return true
    return versionePiuNuova(attuale, vista)
}
