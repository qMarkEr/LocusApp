package com.marker.locus.composables

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.marker.locus.ActiveContact
import com.marker.locus.AllUserData
import com.marker.locus.ContactLocusInfo
import com.marker.locus.CryptoManager
import com.marker.locus.KeyExtractor
import com.marker.locus.LatLngConvertor
import com.marker.locus.R
import com.marker.locus.request.NotificationData
import com.marker.locus.request.PushNotification
import com.marker.locus.request.RetrofitInstance
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@SuppressLint("CommitPrefEdits")
@OptIn(ExperimentalEncodingApi::class)
@Composable
fun ContactCard(locus : ContactLocusInfo,
                delete : MutableState<ContactLocusInfo?>,
                myData : MutableState<AllUserData>,
                activeContacts : SnapshotStateMap<String, ActiveContact>
) {
    var mode by remember {
        mutableStateOf(myData.value.privateData.activeContacts.contains(locus.publicName))
    }
    var share by remember {
        mutableStateOf(myData.value.privateData.activeContacts.contains(locus.publicName))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = locus.profilePicture,
            contentDescription = "wtf",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .padding(15.dp)
                .clip(CircleShape)
                .clickable {
                    delete.value = locus
                }
        )
        Box (modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 0.dp, vertical = 20.dp)) {
            Text(
                text = locus.userName,
                modifier = Modifier.align(Alignment.TopStart),
                textAlign = TextAlign.Center,

                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Default
            )
            Text(text = "Username: ${locus.publicName}",
                modifier = Modifier
                    .align(Alignment.BottomStart),
                textAlign = TextAlign.Center,

                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Default
            )
        }
        Spacer(
            Modifier
                .weight(2f)
                .fillMaxHeight()
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(70.dp)
                .padding(15.dp),
            onClick = {
                mode = !mode
                val docName = md5(locus.publicName + myData.value.privateData.userName)
                if (mode) {
                    PushNotification(
                        NotificationData(
                            "Locus request",
                            myData.value.privateData.userName
                        ),
                        locus.receiveToken
                    ).also { noti ->
                        sendNotification(noti)
                    }
                    val listener = Firebase.firestore.collection("locator")
                        .document(md5(locus.publicName + myData.value.privateData.userName))
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null) {
                                val res = snapshot.toObject(LatLngConvertor::class.java)
                                if (res != null) {
                                    if (!CryptoManager.sharedSecret.keys.contains(docName)) {
                                        Firebase.firestore.collection("keyStore")
                                            .document(locus.publicName)
                                            .get()
                                            .addOnSuccessListener {
                                                val reskey = it.toObject(KeyExtractor::class.java)
                                                if (reskey != null) {
                                                    val key = KeyFactory.getInstance("ECDH")
                                                        .generatePublic(X509EncodedKeySpec(Base64.decode(reskey.key)))
                                                    val ka = KeyAgreement.getInstance("ECDH", "SC")
                                                    ka.init(myData.value.myKeyPair.private)
                                                    ka.doPhase(key, true)
                                                    val ss = ka.generateSecret()
                                                    Log.d("KEYS", Base64.encode(ss))
                                                    CryptoManager.sharedSecret[docName] = SecretKeySpec(ss, 0, ss.size, "AES")
                                                    CryptoManager.addKey(docName, Base64.encode(ss))
                                                }
                                            }
                                    } else {
                                        myData.value.myKeyPair = myData.value.keyGen.genKeyPair()
                                        myData.value.postKeys()
                                    }
                                    if ((res.latitude != "" && res.longitude != "") &&
                                        CryptoManager.sharedSecret[docName] != null) {
                                        val lat = CryptoManager.aesDecrypt(
                                            Base64.decode(res.latitude),
                                            docName
                                        ).toString(Charsets.UTF_8)

                                        val long = CryptoManager.aesDecrypt(
                                            Base64.decode(res.longitude),
                                            docName
                                        ).toString(Charsets.UTF_8)
                                        activeContacts[docName] =
                                            ActiveContact(
                                                doc = docName,
                                                picture = locus.profilePicture,
                                                location = LatLng(lat.toDouble(), long.toDouble())
                                            )
                                        mode = true
                                        share = true
                                        if (!myData.value.privateData.activeContacts.contains(locus.publicName)) {
                                            myData.value.privateData.activeContacts.add(locus.publicName)
                                            myData.value.updatePrivateData()
                                        }
                                    }
                                } else {
                                    share = false
                                    mode = false
                                    activeContacts.remove(docName)
                                    CryptoManager.sharedSecret.remove(docName)
                                    CryptoManager.deleteKey(docName)
                                    myData.value.privateData.activeContacts.remove(locus.publicName)
                                    myData.value.updatePrivateData()
                                }
                            }
                        }
                    activeContacts[md5(locus.publicName + myData.value.privateData.userName)]
                        ?.listener = listener
                }
                else {
                    activeContacts[docName]?.listener?.remove()
                    activeContacts.remove(docName)
                    Firebase.firestore.collection("locator")
                        .document(md5(locus.publicName + myData.value.privateData.userName))
                        .delete()
                    share = false
                    myData.value.privateData.activeContacts.remove(locus.publicName)
                    myData.value.updatePrivateData()
                    CryptoManager.sharedSecret.remove(docName)
                    CryptoManager.deleteKey(docName)
                }
            }
        ) {
            Firebase.firestore.collection("locator")
                .document(md5(locus.publicName + myData.value.privateData.userName))
                .get().addOnSuccessListener {
                    share = it.exists()
                    mode = it.exists()
                }
            Icon(
                painter = painterResource(id =
                if (share)
                    R.drawable.cancel_locating
                else
                    R.drawable.locate
                ),
                contentDescription = "main action"
            )
        }
    }
}

private fun sendNotification(notification: PushNotification) =
    CoroutineScope(Dispatchers.IO +
            CoroutineExceptionHandler{_, throwable -> throwable.printStackTrace()})
        .launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if(response.isSuccessful) {
                    Log.d("AAAAAA", "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e("AAAAAA", response.errorBody().toString())
                }
            } catch(e: Exception) {
                Log.e("AAAAAA", e.toString())
            }
        }

fun md5(input:String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}