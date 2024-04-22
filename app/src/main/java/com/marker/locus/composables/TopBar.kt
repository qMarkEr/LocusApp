package com.marker.locus.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopBar(name : String) {
    TopAppBar (
        navigationIcon = {
            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "ava", modifier = Modifier.padding(10.dp))
        },
        actions = {
            IconButton(
                onClick = { /*TODO*/ },
            ) {
                Icon(imageVector = Icons.Default.Settings , contentDescription = "log out")
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "add",
                    modifier = Modifier.size(18.dp)
                )
            }

        },
        elevation = 0.dp,
        backgroundColor = Color.Transparent.copy(alpha = 0.0f),
        title = { Text(text = name, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.fillMaxWidth().padding(10.dp)) }
    )
}