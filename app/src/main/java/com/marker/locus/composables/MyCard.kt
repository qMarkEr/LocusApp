import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.marker.locus.AllUserData
import com.marker.locus.location.DefaultLocationClient
import com.marker.locus.R
import com.marker.locus.request.FirebaseService
import com.marker.locus.ui.theme.styleDark
import com.marker.locus.ui.theme.styleLight
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MainScreen(
    userData: MutableState<AllUserData>,
    onSignOut: () -> Unit,
    context: Context
) {

    val locationClient = DefaultLocationClient(
        context,
        LocationServices.getFusedLocationProviderClient(context)
    )
    var showMenu by remember {
        mutableStateOf(false)
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
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    if (FirebaseService.showDialog.value) {
        AlertDialog(
            onDismissRequest = {FirebaseService.showDialog.value = false },
            confirmButton = {  },
            title = {
                Text(
                    text = "GAAAANG"
                )
            }
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect (key1 = true) {
            val temp = locationClient.getLastLocation().await()
            if (temp != null) {
                myCurrentLocation = LatLng(temp.latitude, temp.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(myCurrentLocation, 15f)
            } else {
                isLastLocationKnown.value = false
            }
        }
        LaunchedEffect (key1 = true) {
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

        }
        if (isLastLocationKnown.value) {
            Box(
                modifier = Modifier
                    .offset { myCurrentLocation.toPx(cameraPositionState) }
                    .offset(
                        (-30).dp,
                        (-30).dp
                    )
                    .size(60.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.borow_launcher_foreground),
                    contentDescription = "aaa",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
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
                AsyncImage(
                    model = userData.value.publicData.profilePicture,
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
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newCameraPosition(
                                        CameraPosition(myCurrentLocation, 15f, 0f, 0f)
                                    ),
                                    durationMs = 700
                                )
                            }
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(15.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    userData.value.publicData.userName.let {
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
                    Text(
                        text = "ID: ${userData.value.privateData.userName}",
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
            ElevatedButton(
                onClick = { showMenu = !showMenu },
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
                onClick = { clipboardManager.setText(AnnotatedString(userData.value.privateData.userName)) },
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
@Stable
fun LatLng.toPx(cameraPositionState : CameraPositionState): IntOffset {
    cameraPositionState.position
    return cameraPositionState.projection
        ?.toScreenLocation(this)
        ?.let { point ->
            IntOffset(point.x, point.y)
        } ?: IntOffset.Zero
}
