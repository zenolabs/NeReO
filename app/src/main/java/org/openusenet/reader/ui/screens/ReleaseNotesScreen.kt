/*
 * OpenUsenet — Copyright (C) 2026 OpenUsenet contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Schermata "Novità versione": il changelog della sola versione corrente,
 * letto da ReleaseNotes. Complementare a Informazioni (che dà versione e
 * autore) e al README_IT.txt / README_EN.txt (che tengono lo storico completo, fuori dall'app).
 */
package org.openusenet.reader.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import org.openusenet.reader.ui.icone.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.openusenet.reader.Branding
import org.openusenet.reader.R
import org.openusenet.reader.ReleaseNotes
import org.openusenet.reader.ui.VersioneVista
import org.openusenet.reader.ui.indietroSicuro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseNotesScreen(nav: NavHostController) {
    // 1.5.8 (piano B1): aprire le Novita', dalla striscia o dal menu, segna la
    // versione come vista e la striscia «Aggiornato a» non ricompare.
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { VersioneVista.segnaVista(ctx) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.releasenotes_title)) },
                navigationIcon = {
                    IconButton(onClick = { nav.indietroSicuro() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                "${Branding.APP_NAME} ${ReleaseNotes.VERSION}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.rn_whats_changed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            // 1.0: le note arrivano dalle risorse (tradotte come il resto
            // dell'interfaccia). ReleaseNotes espone solo gli ID, perché essendo
            // un `object` non-composable non può risolverli da sé: la risoluzione
            // avviene qui.
            ReleaseNotes.sections.forEach { (titoloRes, vociRes) ->
                Text(
                    stringResource(titoloRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
                vociRes.forEach { voceRes ->
                    Row(Modifier.padding(vertical = 3.dp)) {
                        Text("•  ", style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(voceRes), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.rn_full_history),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
