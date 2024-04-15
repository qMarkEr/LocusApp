package com.marker.locus

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.marker.locus.ui.theme.styleDark
import com.marker.locus.ui.theme.styleLight
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await


@SuppressLint("MissingPermission")
@Composable
fun MainScreen(userData: UserData?,
                       onSignOut: () -> Unit,
                       context: Context) {

    val locationClient = DefaultLocationClient(
        context,
        LocationServices.getFusedLocationProviderClient(context)
    )
    val id = "kiw84ujwe24";
    var showMenu by remember {
        mutableStateOf(false);
    }
    var myCurrentLocation by remember {
        mutableStateOf(LatLng(.0, .0))
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(myCurrentLocation, 15f)
    }

    val isLastLocationKnown = remember {
        mutableStateOf(true)
    }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect (key1 = null) {
            val temp = locationClient.getLastLocation().await()
            if (temp != null) {
                myCurrentLocation = LatLng(temp.latitude, temp.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(myCurrentLocation, 15f)
                Log.d("AAAAAAAAAAAAAAAAA", "${temp.latitude}")
            } else {
                isLastLocationKnown.value = false
            }
        }
        LaunchedEffect (key1 = null) {
            locationClient.getLocationUpdates(500)
                .catch { e -> e.printStackTrace() }
                .onEach { location ->
                    val lat = location.latitude
                    val long = location.longitude
                    myCurrentLocation = LatLng(lat, long)
                }
                .launchIn(this)
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapStyleOptions = MapStyleOptions(if (isSystemInDarkTheme()) styleDark else styleLight)),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = false)
        ) {
            if (isLastLocationKnown.value) {
                Marker(
                    state = MarkerState(position = myCurrentLocation)
                )
            }
        }
        Column (modifier = Modifier
            .align(Alignment.TopCenter)
            .windowInsetsPadding(WindowInsets.statusBars)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                if (userData != null) {
                    AsyncImage(
                        model = userData.profilePictureUrl,
                        contentDescription = "wtf",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(90.dp)
                            .padding(15.dp)
                            .shadow(
                                shape = CircleShape,
                                elevation = 20.dp,
                                ambientColor = Color.Black
                            )
                            .clip(CircleShape)
                            .clickable {
                                cameraPositionState.position =
                                    CameraPosition.fromLatLngZoom(myCurrentLocation, 15f)
                            }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(15.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (userData != null) {
                        userData.username?.let {
                            Text(
                                text = it,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 5.dp)
                                    .align(Alignment.End),
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset.Zero,
                                        blurRadius = 50f
                                    )
                                )
                            )
                        }
                    }
                    Text(
                        text = "ID: $id",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(horizontal = 20.dp),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset.Zero,
                                blurRadius = 30f
                            )
                        )
                    )
                }
            }
            ElevatedButton(onClick = { showMenu = !showMenu },
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .size(40.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(10.dp),
                contentPadding = PaddingValues(1.dp),
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "aa",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            ElevatedButton(
                onClick = { clipboardManager.setText(AnnotatedString(id)) },
                modifier = Modifier
                    .padding(15.dp)
                    .size(40.dp),
                contentPadding = PaddingValues(1.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(10.dp)

            ) {
                Icon(imageVector = Icons.Default.Share,
                    contentDescription = "aa",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (showMenu) {
                AlertDialog(onDismissRequest = { showMenu = !showMenu },
                    confirmButton = {
                        TextButton(onClick = { onSignOut() }) {
                            Text(text = "Yeah")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMenu = !showMenu }) {
                            Text(text = "Nah")
                        }
                    },
                    title = {
                            Text(text = "Log out?")
                    },
                    text = {
                        Text(text = "Are you sure?")
                    }
                )
            }
        }
    }
}
