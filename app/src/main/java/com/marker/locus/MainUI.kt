package com.marker.locus

import android.content.Context
import androidx.compose.material.BottomSheetScaffoldDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.marker.locus.Footer
import com.marker.locus.MainScreen
import com.marker.locus.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI(activeContacts : SnapshotStateList<String>,
           userData: UserData?,
           onSignOut: () -> Unit,
           context: Context
    ) {
    BottomSheetScaffold (
        sheetContent = {
            Footer(lst = activeContacts)
        },
        content = {
            MainScreen(
                userData,
                onSignOut,
                context
            )
        },
        sheetPeekHeight = BottomSheetScaffoldDefaults.SheetPeekHeight,
    )
}