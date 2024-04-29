package com.marker.locus.composables

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.marker.locus.ActiveContact
import com.marker.locus.AllUserData
import com.marker.locus.ContactLocusInfo
import com.marker.locus.CryptoManager
import com.marker.locus.KeyExtractor
import com.marker.locus.MainActivity
import com.marker.locus.MainDB
import com.marker.locus.PublicLocusInfo
import com.marker.locus.location.LocationService
import com.marker.locus.request.FirebaseService
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun Footer(
    contacts: SnapshotStateList<ContactLocusInfo>,
    activeContacts: SnapshotStateMap<String, ActiveContact>,
    userData: MutableState<AllUserData>,
    context: Context,
    database: MainDB
) {
    val show = remember {
        mutableStateOf(false)
    }
    val acceptedRequest = remember {
        mutableStateOf("")
    }

    val showDelete : MutableState<ContactLocusInfo?> = mutableStateOf(null)
    if (FirebaseService.showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                FirebaseService.showDialog.value = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        acceptedRequest.value = FirebaseService.sender
                        LocationService.doc.add(md5(userData.value.privateData.userName + FirebaseService.sender))
                        com.google.firebase.ktx.Firebase.firestore
                            .collection("locator")
                            .document(LocationService.doc.last())
                            .set(mapOf(
                                "latitude" to "",
                                "longitude" to ""
                            ))
                        Intent(context, LocationService::class.java).apply {
                            action = LocationService.ACTION_START
                            context.startService(this)
                        }
                        com.google.firebase.ktx.Firebase.firestore.collection("keyStore")
                            .document(FirebaseService.sender)
                            .get()
                            .addOnSuccessListener {
                                val res = it.toObject(KeyExtractor::class.java)
                                if (res != null) {
                                    val key = KeyFactory.getInstance("ECDH")
                                        .generatePublic(X509EncodedKeySpec(Base64.decode(res.key)))
                                    val ka = KeyAgreement.getInstance("ECDH", "SC")
                                    ka.init(userData.value.myKeyPair.private)
                                    ka.doPhase(key, true)
                                    val ss = ka.generateSecret()
                                    CryptoManager.sharedSecret[md5(userData.value.privateData.userName + FirebaseService.sender)] = SecretKeySpec(ss, 0, ss.size, "AES")
                                    Log.d("KEYS", Base64.encode(ka.generateSecret()))
                                    userData.value.myKeyPair = userData.value.keyGen.genKeyPair()
                                    userData.value.postKeys()
                                }
                            }
                        FirebaseService.showDialog.value = false
                    }
                ) {
                    Text(text = "Accept")
                }
            },
            title = {
                Text(
                    text = "Location request"
                )
            }
        )
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(400.dp)) {
        TextButton(
            onClick = {
                show.value = true
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "add",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add new")
        }
        AddUserLauncher(show, contacts, userData)
        DeleteUserLauncher(showDelete, contacts, userData)
        LazyColumn {
            items(contacts.size) {
                ContactCard(contacts[it], showDelete, userData, activeContacts, database)
            }
        }
    }
}

@Composable
fun AddUserLauncher(show : MutableState<Boolean>, lst : SnapshotStateList<ContactLocusInfo>?, myData : MutableState<AllUserData>) {
    if (show.value) {
        var isError by remember {
            mutableStateOf(false)
        }
        var name by remember {
            mutableStateOf("")
        }
        Dialog(
            onDismissRequest = { show.value = false },
        ) {
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
                        text = "Enter your friend's identifier",
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        label = { Text("Identifier", fontSize = 15.sp) },
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
                            text = "Some weird identifier",
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
                                show.value = false
                            },
                            modifier = Modifier.padding(10.dp),
                            shape = CircleShape,
                        ) {
                            Text(text = "Cancel", fontSize = 17.sp)
                        }
                        Button(
                            onClick = {
                                Firebase.firestore
                                    .collection("Public Locus")
                                    .document(name.trim())
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        if (doc.exists()){
                                            val res = doc.toObject(PublicLocusInfo::class.java)
                                            if (res != null
                                                && !myData.value.privateData.contacts.contains(name.trim())
                                                && name.trim() != myData.value.privateData.userName)
                                            {
                                                val newLocus = ContactLocusInfo(res.profilePicture, res.userName, name.trim(), res.receiveToken)
                                                lst?.add(newLocus)
                                                myData.value.privateData.contacts.add(name.trim())
                                                myData.value.updatePrivateData()
                                                show.value = false
                                            } else
                                                isError = true
                                        } else
                                            isError = true
                                    }
                                    .addOnFailureListener {
                                        isError = true
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

@Composable
fun DeleteUserLauncher(show : MutableState<ContactLocusInfo?>, lst : SnapshotStateList<ContactLocusInfo>?, myData : MutableState<AllUserData>) {
    if (show.value != null) {
        AlertDialog(
            onDismissRequest = { 
                show.value = null
            }, 
            confirmButton = {
                TextButton(onClick = {
                    myData.value.privateData.contacts.remove(show.value!!.publicName)
                    lst?.remove(show.value!!)
                    myData.value.updatePrivateData()
                    show.value = null
                }) {
                    Text(text = "Yeah")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    show.value = null
                }) {
                    Text(text = "Nah")
                }
            },
            title = {
                Text(text = "Want to delete this locus?")
            }
        )
    }
}