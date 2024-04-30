package com.marker.locus

import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.marker.locus.composables.md5
import com.marker.locus.signin.UserData
import kotlinx.coroutines.tasks.await
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AllUserData(
    locusUser : UserData,
) {
    val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("ECDH", "SC")
    private val ecsp = ECGenParameterSpec("secp256r1")
    lateinit var myKeyPair : KeyPair
    private val googleData = locusUser
    var publicData : PublicLocusInfo = PublicLocusInfo()
    var privateData : PrivateLocusInfo = PrivateLocusInfo(mutableListOf(), "", mutableListOf())

    suspend fun loadData(){
        keyGen.initialize(ecsp)
        myKeyPair = keyGen.generateKeyPair()
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
                        privateData.activeContacts = res.activeContacts.toMutableList()
                    }
                }
            }.addOnFailureListener {
                createPrivateData()
            }
            .await()
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun postKeys() {
        Firebase.firestore.collection("keyStore")
            .document(privateData.userName)
            .set(
                mapOf(
                    "key" to Base64.encode(myKeyPair.public.encoded)
                )
            )
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
    @OptIn(ExperimentalEncodingApi::class)
    fun getActiveContacts() : SnapshotStateMap<String, ActiveContact> {
        val resultList = SnapshotStateMap<String, ActiveContact>()
        for (i in privateData.activeContacts) {
            val docName = md5(i + privateData.userName)
            CryptoManager.loadKey(docName)
            Firebase.firestore
                .collection("Public Locus")
                .document(i)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val resLocus = it.toObject(PublicLocusInfo::class.java)
                        if (resLocus != null) {
                            val listener = Firebase.firestore.collection("locator")
                                .document(docName)
                                .addSnapshotListener { snapshot, _ ->
                                    if (snapshot != null) {
                                        val res = snapshot.toObject(LatLngConvertor::class.java)
                                        if (res != null) {
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
                                                resultList[docName] =
                                                    ActiveContact(
                                                        doc = docName,
                                                        picture = resLocus.profilePicture,
                                                        location = LatLng(lat.toDouble(), long.toDouble())
                                                    )
                                            }
                                        } else {
                                            CryptoManager.sharedSecret.remove(docName)
                                            CryptoManager.deleteKey(docName)
                                            resultList.remove(docName)
                                        }
                                    }
                                }
                            resultList[docName]
                                ?.listener = listener
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
    val latitude : String = "",
    val longitude : String = ""
)