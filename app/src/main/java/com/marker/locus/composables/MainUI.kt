package com.marker.locus.composables

import MainScreen
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marker.locus.ActiveContact
import com.marker.locus.AllUserData
import com.marker.locus.ContactLocusInfo
import com.marker.locus.MainActivity
import com.marker.locus.MainDB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI(
    contacts: SnapshotStateList<ContactLocusInfo>,
    activeContacts: SnapshotStateMap<String, ActiveContact>,
    myData: MutableState<AllUserData>,
    onSignOut: () -> Unit,
    context: Context,
    database: MainDB
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState()
    )
    BottomSheetScaffold (
        sheetContent = {
            Footer(contacts = contacts, activeContacts = activeContacts, myData, context, database)
        },
        scaffoldState = scaffoldState,
        content = {
            MainScreen(
                activeContacts,
                myData,
                onSignOut,
                context
            )
        },
        sheetPeekHeight = 90.dp,
        sheetShape = RoundedCornerShape(25.dp),
        sheetDragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(15.dp)
                        .width(40.dp)
                        .clip(CircleShape),
                    thickness = 4.dp,
                    color = MaterialTheme.colorScheme.surfaceTint
                    )
                Text(
                    text = "My Locuses",
                    fontFamily = FontFamily.Default,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(5.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 30.sp
                )
            }
        }
    )
}