package com.marker.locus.composables

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
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.marker.locus.ActiveContact
import com.marker.locus.AllUserData
import com.marker.locus.ContactLocusInfo
import com.marker.locus.LatLngConvertor
import com.marker.locus.R
import com.marker.locus.request.NotificationData
import com.marker.locus.request.PushNotification
import com.marker.locus.request.RetrofitInstance
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ContactCard(locus : ContactLocusInfo,
                delete : MutableState<ContactLocusInfo?>,
                myData : MutableState<AllUserData>,
                activeContacts : SnapshotStateMap<String, ActiveContact>
) {
    var mode by remember {
        mutableStateOf(false)
    }
    val snaplis : MutableState<ListenerRegistration?> = remember { mutableStateOf(null) }
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
                    Log.d("lexa", locus.publicName + myData.value.privateData.userName)
                    val temp = ActiveContact(
                        doc = locus.publicName + myData.value.privateData.userName,
                        picture = locus.profilePicture
                    )
                    val listener = Firebase.firestore.collection("locator")
                        .document(locus.publicName + myData.value.privateData.userName)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null) {
                                val res = snapshot.toObject(LatLngConvertor::class.java)
                                if (res != null) {
                                    Log.d("latlan", res.longitude.toString())
                                    temp.location = LatLng(res.latitude, res.longitude)
                                }
                            }
                        }
                    temp.listener = listener
                    activeContacts[locus.publicName + myData.value.privateData.userName] = temp
                }
                else {
                    activeContacts[locus.publicName + myData.value.privateData.userName]?.listener?.remove()
                    activeContacts.remove(locus.publicName + myData.value.privateData.userName)
                    Firebase.firestore.collection("locator")
                        .document(locus.publicName + myData.value.privateData.userName)
                        .delete()
                }
            }
        ) {
            Icon(
                painter = painterResource(id =
                if (mode)
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