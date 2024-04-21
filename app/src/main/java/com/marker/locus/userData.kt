package com.marker.locus

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.marker.locus.SignIn.UserData
import kotlinx.coroutines.tasks.await

class AllUserData(
    locusUser : UserData
) {
    private val googleData = locusUser
    var publicData : PublicLocusInfo = PublicLocusInfo("" ,"")
    var privateData : PrivateLocusInfo = PrivateLocusInfo(mutableListOf(), "")

    suspend fun loadData(){
        publicData.profilePicture = googleData.profilePictureUrl.toString()
        publicData.userName = googleData.username.toString()
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
                            val newLocus = ContactLocusInfo(res.profilePicture, res.userName, i)
                            resultList.add(newLocus)
                        }
                    }
                }
        }
        return resultList
    }
}