package com.marker.locus

import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.marker.locus.signin.UserData
import kotlinx.coroutines.tasks.await

class AllUserData(
    locusUser : UserData
) {
    private val googleData = locusUser
    var publicData : PublicLocusInfo = PublicLocusInfo()
    var privateData : PrivateLocusInfo = PrivateLocusInfo(mutableListOf(), "")

    suspend fun loadData(){
        publicData.profilePicture = googleData.profilePictureUrl.toString()
        publicData.userName = googleData.username.toString()
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("AAAAAAA", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            publicData.receiveToken = task.result
        })
        Firebase.firestore.collection("Private Locus")
            .document(googleData.userId)
            .get().addOnSuccessListener {
                if (it.exists()) {
                    Log.d("load", googleData.userId)
                    val res = it.toObject(PrivateConvertor::class.java)
                    if (res != null) {
                        privateData.contacts = res.contacts.toMutableList()
                        privateData.userName = res.userName
                    }
                }
            }.addOnFailureListener {
                createPrivateData()
            }
            .await()
    }

    private fun createPrivateData() {
        Firebase.firestore
            .collection("Private Locus")
            .add(googleData.userId)
        updatePrivateData()
    }
    private fun createPublicData() {
        Firebase.firestore
            .collection("Public Locus").add(privateData.userName)
        publicData.userName = googleData.username.toString()
        publicData.profilePicture = googleData.profilePictureUrl.toString()
        updatePublicData()
    }
    fun updatePrivateData() {
        Firebase.firestore
            .collection("Private Locus").document(googleData.userId)
            .set(privateData)
    }
    fun updatePublicData() {
        publicData.userName = googleData.username.toString()
        publicData.profilePicture = googleData.profilePictureUrl.toString()
        Firebase.firestore
            .collection("Public Locus").document(privateData.userName)
            .set(publicData).addOnFailureListener { createPublicData() }
    }
    fun getContacts() : SnapshotStateList<ContactLocusInfo> {
        val resultList = SnapshotStateList<ContactLocusInfo>()
        for (i in privateData.contacts) {
            Firebase.firestore
                .collection("Public Locus")
                .document(i)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val res = it.toObject(PublicLocusInfo::class.java)
                        if (res != null) {
                            val newLocus = ContactLocusInfo(res.profilePicture, res.userName, i, res.receiveToken)
                            resultList.add(newLocus)
                        }
                    }
                }
        }
        return resultList
    }
}

data class ActiveContact(
    val picture : String = "",
    val doc : String = "",
    var listener : ListenerRegistration? = null,
    var location : LatLng = LatLng(.0, .0)
)

data class LatLngConvertor(
    val latitude : Double = 0.0,
    val longitude : Double = 0.0
)