package com.marker.locus

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.identity.Identity
import com.marker.locus.ui.theme.LocusTheme
import com.marker.locus.signin.SignInUI
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.marker.locus.composables.MainUI
import com.marker.locus.request.FirebaseService
import com.marker.locus.signin.GoogleAuthUiClient
import com.marker.locus.signin.SignInViewModel
import kotlinx.coroutines.launch
class MainActivity : ComponentActivity() {
    private var contacts : SnapshotStateList<ContactLocusInfo> = SnapshotStateList()
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        enableEdgeToEdge(
            SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            LocusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionLauncher()
                }
            }
        }
    }

    private fun areLocationPermissionsAlreadyGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun openApplicationSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)).also {
            startActivity(it)
        }
    }

    private fun decideCurrentPermissionStatus(locationPermissionsGranted: Boolean,
                                              shouldShowPermissionRationale: Boolean): String {
        return if (locationPermissionsGranted) "Granted"
        else if (shouldShowPermissionRationale) "Rejected"
        else "Denied"
    }

    @Composable
    private fun SignInController() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "sign_in") {
            lateinit var data: MutableState<AllUserData>
            composable("sign_in") {
                val viewModel = viewModel<SignInViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(key1 = Unit) {
                    val siu = googleAuthUiClient.getSignedInUser()
                    if (siu != null) {
                        data = mutableStateOf(AllUserData(siu))
                        data.value.loadData()
                        contacts = data.value.getContacts()
                        if (data.value.privateData.userName != "") {
                            navController.navigate("profile")
                        } else {
                            navController.navigate("nickname")
                        }
                    }
                }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = { result ->
                        if (result.resultCode == RESULT_OK) {
                            lifecycleScope.launch {
                                val signInResult = googleAuthUiClient.signInWithIntent(
                                    intent = result.data ?: return@launch
                                )
                                viewModel.onSignInResult(signInResult)
                            }
                        }
                    }
                )
                LaunchedEffect(key1 = state.isSignInSuccessful) {
                    if (state.isSignInSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Sign in successful",
                            Toast.LENGTH_LONG
                        ).show()
                        val siu = googleAuthUiClient.getSignedInUser()
                        if (siu != null) {
                            data = mutableStateOf(AllUserData(siu))
                            data.value.loadData()
                            data.value.updatePrivateData()
                            contacts = data.value.getContacts()
                        }
                        navController.navigate("nickname")
                        viewModel.resetState()
                    }
                }
                if (googleAuthUiClient.getSignedInUser() == null) {
                    SignInUI(
                        state = state,
                        onSignInClick = {
                            lifecycleScope.launch {
                                val signInIntentSender = googleAuthUiClient.signIn()
                                launcher.launch(
                                    IntentSenderRequest.Builder(
                                        signInIntentSender ?: return@launch
                                    ).build()
                                )
                            }
                        }
                    )
                }
            }
            composable("nickname") {
                PublicNameDialog(myData = data, navController)
            }
            composable("profile") {
                MainUI(
                    activeContacts = contacts,
                    myData = data,
                    onSignOut = {
                        lifecycleScope.launch {
                            googleAuthUiClient.signOut()
                            Toast.makeText(
                                applicationContext,
                                "Signed out",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate("sign_in")
                        }
                    },
                    applicationContext
                )
            }
        }
    }

    @Composable
    fun PublicNameDialog(myData: MutableState<AllUserData>, navController: NavHostController) {
        if (myData.value.privateData.userName != "") {
            navController.navigate("profile")
        }
        var name by remember {
            mutableStateOf("")
        }
        var isError by remember {
            mutableStateOf(false)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(360.dp)
                    .height(500.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = "One more thing",
                        fontWeight = FontWeight.ExtraLight,
                        fontFamily = FontFamily.Default,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontSize = 50.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .width(150.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(20.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    ElevatedCard(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column {
                            Text(
                                modifier = Modifier
                                    .padding(30.dp)
                                    .align(Alignment.CenterHorizontally),
                                fontFamily = FontFamily.Default,
                                text = "Enter your unique indetifier",
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                label = { Text("Indetifier", fontSize = 15.sp) },
                                modifier = Modifier.padding(horizontal = 20.dp),
                                shape = CircleShape,
                                value = name,
                                onValueChange = {
                                    name = it
                                    isError = false
                                },
                                trailingIcon = {
                                    if (isError)
                                        Icon(
                                            Icons.Filled.Warning,
                                            "error",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                },
                                singleLine = true,
                                isError = isError,
                            )
                            if (isError) {
                                Text(
                                    text = "Not unique",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.displaySmall,
                                    modifier = Modifier.padding(start = 30.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {

                                TextButton(
                                    onClick = {
                                        lifecycleScope.launch {
                                            googleAuthUiClient.signOut()
                                            Toast.makeText(
                                                applicationContext,
                                                "Signed out",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            navController.navigate("sign_in")
                                        }
                                    },
                                    modifier = Modifier.padding(10.dp),
                                    shape = CircleShape,
                                ) {
                                    Text(text = "Go back", fontSize = 17.sp)
                                }
                                Button(
                                    onClick = {
                                        Firebase.firestore.collection("Public Locus")
                                            .document(name).get()
                                            .addOnSuccessListener {
                                                if (!it.exists()) {
                                                    myData.value.privateData.userName = name
                                                    myData.value.updatePrivateData()
                                                    myData.value.updatePublicData()
                                                    navController.navigate("profile")
                                                } else {
                                                    isError = true
                                                }
                                            }
                                    },
                                    modifier = Modifier.padding(10.dp),
                                    shape = CircleShape,
                                ) {
                                    Text(text = "Proceed", fontSize = 17.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Composable
    fun PermissionLauncher() {
        var locationPermissionsGranted by remember { mutableStateOf(areLocationPermissionsAlreadyGranted()) }
        var shouldShowPermissionRationale by remember {
            mutableStateOf(
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }

        var shouldDirectUserToApplicationSettings by remember {
            mutableStateOf(false)
        }

        var currentPermissionsStatus by remember {
            mutableStateOf(decideCurrentPermissionStatus(locationPermissionsGranted, shouldShowPermissionRationale))
        }

        val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissions ->
                locationPermissionsGranted = permissions.values.reduce { acc, isPermissionGranted ->
                    acc && isPermissionGranted
                }

                if (!locationPermissionsGranted) {
                    shouldShowPermissionRationale =
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                shouldDirectUserToApplicationSettings = !shouldShowPermissionRationale && !locationPermissionsGranted
                currentPermissionsStatus = decideCurrentPermissionStatus(locationPermissionsGranted, shouldShowPermissionRationale)
            })

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(
            key1 = lifecycleOwner,
            effect = {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START &&
                        !locationPermissionsGranted &&
                        !shouldShowPermissionRationale) {
                        locationPermissionLauncher.launch(locationPermissions)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
        )

        if (areLocationPermissionsAlreadyGranted())
            SignInController()

        if (shouldShowPermissionRationale) {
            AlertDialog(
                onDismissRequest = {
                    Toast.makeText(
                        applicationContext,
                        "App will not work for you",
                        Toast.LENGTH_LONG
                    ).show()
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            locationPermissionLauncher.launch(locationPermissions)
                        }
                    ) {
                        Text(text = "Grant access")
                    }
                },
                title = {
                    Text(text = "Location permission are necessary")
                },
                text = {
                    Text(text = "Just say yes")
                }
            )
        }
        if (shouldDirectUserToApplicationSettings) {
            AlertDialog(
                onDismissRequest = {
                    Toast.makeText(
                        applicationContext,
                        "App will not work for you",
                        Toast.LENGTH_LONG
                    ).show()
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openApplicationSettings()
                        }
                    ) {
                        Text(text = "Go to Settings")
                    }
                },
                title = {
                    Text(text = "Location permission are necessary")
                },
                text = {
                    Text(text = "Go to settings to give permission")
                }
            )
        }

    }
}