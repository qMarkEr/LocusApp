package com.marker.locus.Composables

import android.content.Context
import androidx.compose.material.BottomSheetScaffoldDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.marker.locus.AllUserData
import com.marker.locus.SignIn.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI(activeContacts : SnapshotStateList<String>,
           myData : MutableState<AllUserData>,
           onSignOut: () -> Unit,
           context: Context
    ) {
    BottomSheetScaffold (
        sheetContent = {
            Footer(lst = activeContacts)
        },
        content = {
            MainScreen(
                myData,
                onSignOut,
                context
            )
        },
        sheetPeekHeight = BottomSheetScaffoldDefaults.SheetPeekHeight,
    )
}